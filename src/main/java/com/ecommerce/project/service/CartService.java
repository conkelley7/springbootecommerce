package com.ecommerce.project.service;

import com.ecommerce.project.payload.CartDTO;

import java.util.List;

/**
 * Service interface for managing shopping cart operations.
 * Provides methods to add, retrieve, update, and delete items in shopping carts.
 */
public interface CartService {
    /**
     * Adds a specified product to the shopping cart with the given quantity.
     * Updates the cart with the provided product and recalculates the total price.
     *
     * @param productId the unique identifier of the product to be added to the cart
     * @param quantity the quantity of the product to add to the cart
     * @return the updated CartDTO containing the details of the cart after the product is added
     */
    CartDTO addProductToCart(Long productId, Integer quantity);

    /**
     * Retrieves a list of all shopping carts.
     *
     * @return a list of CartDTO objects representing all the shopping carts
     */
    List<CartDTO> getAllCarts();

    /**
     * Retrieves the shopping cart for the currently logged-in user.
     *
     * @return the CartDTO object containing the details of the logged-in user's shopping cart,
     *         including the cart ID, total price, and list of products.
     */
    CartDTO getCartForLoggedInUser();

    /**
     * Updates the quantity of a specific product in the shopping cart.
     * This method adjusts the product quantity based on the specified change and recalculates the cart's total price.
     *
     * @param productId the unique identifier of the product whose quantity is to be updated
     * @param change the integer value indicating the change in quantity. Positive values increase the quantity,
     *               while negative values decrease it.
     * @return the updated CartDTO containing the updated cart details, including the product list and total price
     */
    CartDTO updateProductQuantityInCart(Long productId, int change);

    /**
     * Deletes a product from the shopping cart based on the product's unique identifier.
     * This method removes the specified product and updates the cart accordingly.
     *
     * @param productId the unique identifier of the product to be removed from the cart
     * @return a message indicating the result of the deletion operation, such as success or failure details
     */
    String deleteProductFromCart(Long productId);

    /**
     * Updates the association between a product and a cart based on the given cart ID and product ID.
     * This method is used to modify the contents of one or more shopping carts with a specific product.
     *
     * @param cartId the unique identifier of the cart in which the product association needs to be updated
     * @param productId the unique identifier of the product to be updated in the cart
     */
    void updateProductInCarts(Long cartId, Long productId);

    /**
     * Removes the specified product from all shopping carts in the system.
     * This method iterates through all carts and ensures that the product
     * with the given identifier is no longer associated with any cart.
     *
     * @param cartId the unique identifier of the cart from which the product was associated
     * @param productId the unique identifier of the product to be removed from all carts
     */
    void deleteProductFromAllCarts(Long cartId, Long productId);
}
