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
            @RequestHeader(value = "X-CART-ID", required = false) String cartUuid,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody(required = false) CheckoutRequest request
    ) {
        if ((cartUuid == null || cartUuid.isEmpty()) && (username == null || username.isEmpty())) {
            return ResponseEntity.badRequest().body("Must provide either Cart ID or be logged in.");
        }

        Long userId = (request != null) ? request.getUserId() : null;

        // Place the order via OrderService, passing the username context
        Order order = orderService.placeOrder(cartUuid, username, userId);

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

        if (role == null || !role.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }

        boolean status = orderService.markOrderStatus(orderId);
        return ResponseEntity.ok(status);
    }
}