package com.order_service.service;

import com.order_service.client.CartFeignClient;
import com.order_service.dto.CartItemResponseDTO;
import com.order_service.dto.CartResponseDTO;
import com.order_service.entity.Order;
import com.order_service.entity.OrderItem;
import com.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartFeignClient cartFeignClient;

    public OrderService(OrderRepository orderRepository, CartFeignClient cartFeignClient) {
        this.orderRepository = orderRepository;
        this.cartFeignClient = cartFeignClient;
    }

    // 1. Added Long userId to the method signature
    @Transactional
    public Order placeOrder(String cartUuid, Long userId) {

        // Fetch Cart via Feign Client
        CartResponseDTO cart = cartFeignClient.getCart(cartUuid);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is Empty");
        }

        // Initialize Order
        Order order = new Order();

        // 2. Use the provided userId, or fallback to the cart's userId if guest
        order.setUserId(userId != null ? userId : cart.getUserId());

        order.setCartuuid(cartUuid);
        order.setStatus("CREATED");

        // Map CartItems to OrderItems and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItemResponseDTO cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setBrandId(cartItem.getBrandId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setOrder(order);

            order.getItems().add(orderItem);

            BigDecimal itemTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        order.setTotalAmount(totalAmount);

        // Save Order to Database
        Order savedOrder = orderRepository.save(order);

        // Clear Cart in Cart Service via Feign Client
        try {
            cartFeignClient.clearCart(cartUuid);
        } catch (Exception e) {
            System.err.println("Warning: Order created, but failed to clear cart: " + e.getMessage());
        }

        return savedOrder;
    }

    public boolean markOrderStatus(long id) {

        Order order = orderRepository.findById(id).get();
        order.setStatus("COMPLETED");
        Order savedOrder = orderRepository.save(order);
        if (order.getStatus().equals("COMPLETED")) {
            return true;
        } else{
            return false;
        }


    }
}