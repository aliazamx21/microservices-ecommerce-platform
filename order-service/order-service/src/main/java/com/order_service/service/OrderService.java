package com.order_service.service;

import com.order_service.client.CartFeignClient;
import com.order_service.dto.CartItemResponseDTO;
import com.order_service.dto.CartResponseDTO;
import com.order_service.entity.Order;
import com.order_service.entity.OrderItem;
import com.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartFeignClient cartFeignClient;
    private final KafkaTemplate<String, String> kafkaTemplate; // Added for Kafka

    // Injected KafkaTemplate into the constructor
    public OrderService(OrderRepository orderRepository, CartFeignClient cartFeignClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.cartFeignClient = cartFeignClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order placeOrder(String cartUuid, Long userId) {

        // Fetch Cart via Feign Client
        CartResponseDTO cart = cartFeignClient.getCart(cartUuid);

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is Empty");
        }

        // Initialize Order
        Order order = new Order();
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

        // --- THE SAGA PATTERN: PUBLISH EVENT TO KAFKA ---
        try {
            // Create a simple JSON payload
            String eventPayload = String.format("{\"orderId\":%d, \"amount\":%f}", savedOrder.getId(), savedOrder.getTotalAmount());
            // Blast it to the Kafka topic
            kafkaTemplate.send("order-created", String.valueOf(savedOrder.getId()), eventPayload);
            System.out.println("Published OrderCreatedEvent to Kafka for Order ID: " + savedOrder.getId());
        } catch (Exception e) {
            System.err.println("Warning: Failed to publish to Kafka: " + e.getMessage());
        }

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