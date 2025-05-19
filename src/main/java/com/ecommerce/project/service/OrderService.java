package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.OrderDTO;
import jakarta.transaction.Transactional;

/**
 * Service interface for handling operations related to orders.
 * Provides a method to create orders for logged-in user with payment details.
 */
public interface OrderService {
    /**
     * Places an order for the given user based on the provided address, payment method, and payment gateway details.
     * This method processes the order by associating it with the user and payment information,
     * and returns an object containing the details of the created order.
     *
     * @param user the user placing the order
     * @param addressId the unique identifier of the address where the order will be delivered
     * @param paymentMethod the method of payment chosen by the user (e.g., credit card, PayPal)
     * @param pgName the name of the payment gateway used for processing the payment
     * @param pgPaymentId the unique identifier of the payment as provided by the payment gateway
     * @param pgStatus the status of the payment as reported by the payment gateway (e.g., success, failure)
     * @param pgResponseMessage a message or response provided by the payment gateway regarding the payment status
     * @return an OrderDTO object containing the details of the placed order, including order items, total amount,
     *         payment information, and delivery address
     */
    @Transactional
    OrderDTO placeOrder(User user, Long addressId, String paymentMethod, String pgName, String pgPaymentId,
                        String pgStatus, String pgResponseMessage);
}
