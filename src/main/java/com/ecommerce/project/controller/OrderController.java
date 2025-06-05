package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.security.service.AuthUtil;
import com.ecommerce.project.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final AuthUtil authUtil;

    public OrderController(OrderService orderService, AuthUtil authUtil) {
        this.orderService = orderService;
        this.authUtil = authUtil;
    }

    @PostMapping("/users/order")
    public ResponseEntity<OrderDTO> orderProducts(@RequestBody OrderRequestDTO orderRequestDTO) {
        User user = authUtil.loggedInUser();
        OrderDTO orderDTO = orderService.placeOrder(
                user,
                orderRequestDTO.getAddressId(),
                orderRequestDTO.getPaymentMethod(),
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );

        return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
    }
}
