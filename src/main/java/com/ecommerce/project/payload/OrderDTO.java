package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String userEmail;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime orderDateTime;
    private PaymentDTO payment;
    private Double totalAmount;
    private String orderStatus;
    private AddressDTO address;
}
