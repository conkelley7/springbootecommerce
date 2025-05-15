package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.security.service.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart, or create a new one
        Cart cart = findOrCreateCart();

        // Retrieve product details
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));

        // Perform validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exits in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException("Product" + product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Only " + product.getQuantity() + " of " + product.getProductName() + " available");
        }

        // Create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // Save cart item
        cartItemRepository.save(newCartItem);

        /*
        Optionally, we could reduce the quantity of the product in DB here. Otherwise, we can
        do it when the user checks out and places their order. I will just leave this here as a placeholder
        for now in case I would like to make changes in the future.
         */

        // Update cart total price and save to DB
        cart.setTotalPrice(cart.getTotalPrice() + product.getSpecialPrice() * quantity);

        cart = cartRepository.save(cart);

        // Convert to CartDTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        // Get all items already associated with cart
        List<CartItem> cartItems = cart.getCartItems();

        // Need to add most recent cart item to list as well as others that already exists
        // It does not fetch the newest cart item despite it being persisted in DB
        cartItems.add(newCartItem);

        // Prepare list of products to be added to the CartDTO
        // Mapping cart items to products and changing product quantity to cart item quantity
        List<ProductDTO> productDTOList = cartItems.stream().map(item -> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        }).toList();

        cartDTO.setProducts(productDTOList);

        // Return updated cart information
        return cartDTO;

    }

    private Cart findOrCreateCart() {
        Cart cart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (cart != null) {
            return cart;
        }

        Cart newCart = new Cart();
        newCart.setTotalPrice(0.00);
        newCart.setUser(authUtil.loggedInUser());

        return cartRepository.save(newCart);
    }
}
