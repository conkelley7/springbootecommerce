package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;

    public OrderServiceImpl(CartRepository cartRepository, AddressRepository addressRepository,
                            PaymentRepository paymentRepository, OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository, ProductRepository productRepository,
                            CartService cartService, ModelMapper modelMapper) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public OrderDTO placeOrder(User user, Long addressId, String paymentMethod, String pgName, String pgPaymentId,
                               String pgStatus, String pgResponseMessage) {

        // 1) Get user cart and address
        String userEmail = user.getEmail();
        Cart cart = cartRepository.findCartByEmail(userEmail);

        if (cart == null)
            throw new APIException(user.getUsername() + " has no active cart");

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new APIException("Address with ID '" + "' does not exist"));

        // 2) Create new order with cart details and payment information
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Accepted");
        order.setAddress(address);

        Payment payment = new Payment();
        payment.setPaymentMethod(paymentMethod);
        payment.setPgPaymentId(pgPaymentId);
        payment.setPgStatus(pgStatus);
        payment.setPgResponseMessage(pgResponseMessage);
        payment.setPgName(pgName);

        payment.setOrder(order);
        payment = paymentRepository.save(payment);

        order.setPayment(payment);

        order = orderRepository.save(order);

        // 3) Convert cart items to order items
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty())
            throw new APIException("Cart is empty");

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        List<CartItem> itemsToRemove = new ArrayList<>(cartItems);
        // 4) Update product stock and clear the cart
        itemsToRemove.forEach(cartItem -> {
            // Update product stock
            int quantity = cartItem.getQuantity();
            Product product = cartItem.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            // Remove item from the cart
            cartService.deleteProductFromCart(product.getProductId());
        });


        // 6) Send back the order summary
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);

        List<OrderItemDTO> orderItemDTOS = new ArrayList<>();
        orderItems.forEach(orderItem -> {
            orderItemDTOS.add(modelMapper.map(orderItem, OrderItemDTO.class));
        });

        orderDTO.setOrderItems(orderItemDTOS);

        return orderDTO;
    }
}
