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
import jakarta.transaction.Transactional;
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

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty())
            throw new APIException("No carts exist");

        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class)).toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();

        return cartDTOS;
    }

    @Override
    public CartDTO getCartForLoggedInUser() {
        Cart cart = cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if (cart == null)
            throw new APIException("No cart found for logged in user");

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems().stream()
                .map(cartItem -> {
                    ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(cartItem.getQuantity());
                    return productDTO;
                })
                .toList();

        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, int change) {
        // Validations
        Cart cart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (cart == null)
            throw new APIException("No cart exists for logged in user");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "product_id", productId));

        if (product.getQuantity() == 0) {
            throw new APIException("Product" + product.getProductName() + " is not available");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (cartItem == null)
            throw new APIException("Product" + product.getProductName() + " does not exist in the cart");
        if (cartItem.getQuantity() == 0 && change == -1)
            throw new APIException("Quantity of product " + product.getProductName() + " already 0");
        // Update price in case there was a price change
        cartItem.setProductPrice(product.getSpecialPrice());

        // Update quantity based on change
        cartItem.setQuantity(cartItem.getQuantity() + change);

        // Update total discount based on changes in price and quantity
        cartItem.setDiscount(product.getDiscount() * cartItem.getQuantity());

        // Update total price of the cart
        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * change));

        // If item updated has quantity one in the cart, and it is now 0, just delete the item
        // Otherwise, save the updated cart item
        if (cartItem.getQuantity() == 0) {
            cart.getCartItems().remove(cartItem);
            cartItem.setCart(null);
        } else {
            cartItemRepository.save(cartItem);
        }
        cart = cartRepository.save(cart);


        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(item -> {
                    ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
                    productDTO.setQuantity(item.getQuantity());
                    return productDTO;
                })
                .toList();

        cartDTO.setProducts(products);


        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long productId) {
        Cart cart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (cart == null)
            throw new APIException("No cart found for logged in user");

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (cartItem == null)
            throw new APIException("Product does not exists in cart");

        // Update cart total price
        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice() * cartItem.getQuantity());

        // Delete cart item entry from in-memory collection and DB
        cart.getCartItems().remove(cartItem);
        cartItem.setCart(null);
        cartRepository.save(cart);

        return "Product deleted from cart successfully";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        // Validation
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null)
            throw new APIException("Product " + product.getProductName() + " not found in the cart");

        // Get price of product in cart, and remove old product price from cart total
        double cartPrice = cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity());

        // Apply new product price to cart item
        cartItem.setProductPrice(product.getSpecialPrice());

        // Re-add the price back to the cart's total price after applying new product price
        cart.setTotalPrice(cartPrice
                + cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem = cartItemRepository.save(cartItem);
        cart = cartRepository.save(cart);
    }

    @Override
    public void deleteProductFromAllCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new APIException("No cart found with ID: " + cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null)
            throw new APIException("Product does not exists in cart with ID: " + cartId);

        // Update cart total price
        cart.setTotalPrice(cart.getTotalPrice() - cartItem.getProductPrice() * cartItem.getQuantity());

        // Delete cart item entry from in-memory collection and DB
        cart.getCartItems().remove(cartItem);
        cartItem.setCart(null);
        cartRepository.save(cart);
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
