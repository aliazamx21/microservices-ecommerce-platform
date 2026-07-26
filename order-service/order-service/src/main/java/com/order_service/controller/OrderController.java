package com.order_service.controller;

import com.order_service.dto.CheckoutRequest;
import com.order_service.dto.OrderResponse;
import com.order_service.entity.Order;
import com.order_service.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(
            @RequestHeader(value = "X-CART-ID") String cartUuid,
            @RequestHeader(value = "X-Logged-In-User") String username,
            @RequestHeader(value = "X-User-Role") String role,
            @RequestBody(required = false) CheckoutRequest request
    ) {
        if (cartUuid == null || cartUuid.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Long userId = (request != null) ? request.getUserId() : null;

        // 1. Place the order via OrderService (which calls Cart Feign Client)
        Order order = orderService.placeOrder(cartUuid, userId);

        // 2. Return the response
        OrderResponse response = new OrderResponse(
                String.valueOf(order.getId()),
                order.getTotalAmount(),
                order.getStatus()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Boolean> updateOrderStatus(
            @PathVariable long orderId,
            @RequestHeader(value = "X-User-Role") String role) {

        // Only allow ADMIN to update order status
        if (!role.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }

        boolean status = orderService.markOrderStatus(orderId);
        return ResponseEntity.ok(status);
    }
}