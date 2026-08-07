package com.order_service.service;

import com.order_service.client.CartFeignClient;
import com.order_service.dto.CartItemResponseDTO;
import com.order_service.dto.CartResponseDTO;
import com.order_service.entity.Order;
import com.order_service.entity.OrderItem;
import com.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartFeignClient cartFeignClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ADDED: Circuit Breaker Factory
    private final CircuitBreakerFactory circuitBreakerFactory;

    public OrderService(OrderRepository orderRepository, CartFeignClient cartFeignClient,
                        KafkaTemplate<String, String> kafkaTemplate, CircuitBreakerFactory circuitBreakerFactory) {
        this.orderRepository = orderRepository;
        this.cartFeignClient = cartFeignClient;
        this.kafkaTemplate = kafkaTemplate;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Transactional
    public Order placeOrder(String cartUuid, Long userId) {

        // --- ADDED CIRCUIT BREAKER ---
        // If the Cart Service is down, the Circuit Breaker opens and prevents a cascading failure.
        CartResponseDTO cart = circuitBreakerFactory.create("cartService").run(
                () -> cartFeignClient.getCart(cartUuid),
                throwable -> {
                    logger.error("Cart Service is currently unavailable. Fallback triggered. Error: {}", throwable.getMessage());
                    return null; // Return null to trigger the "Cart is Empty" exception below
                }
        );

        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is Empty or Cart Service is unavailable.");
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
            String eventPayload = String.format("{\"orderId\":%d, \"amount\":%f}", savedOrder.getId(), savedOrder.getTotalAmount());
            kafkaTemplate.send("order-created", String.valueOf(savedOrder.getId()), eventPayload);
            logger.info("Published OrderCreatedEvent to Kafka for Order ID: {}", savedOrder.getId());
        } catch (Exception e) {
            logger.error("Failed to publish to Kafka: {}", e.getMessage());
        }

        // Clear Cart in Cart Service via Feign Client
        try {
            circuitBreakerFactory.create("cartServiceClear").run(
                    () -> { cartFeignClient.clearCart(cartUuid); return true; },
                    throwable -> {
                        logger.warn("Order created, but failed to clear cart. Cart Service unavailable. Error: {}", throwable.getMessage());
                        return false;
                    }
            );
        } catch (Exception e) {
            logger.error("Unexpected error while clearing cart: {}", e.getMessage());
        }

        return savedOrder;
    }

    public boolean markOrderStatus(long id) {
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();

            // --- ADDED IDEMPOTENCY CHECK ---
            // If Kafka sends the same event twice, we don't want to process it again.
            if ("COMPLETED".equals(order.getStatus())) {
                return false; // Already completed, no update needed
            }

            order.setStatus("COMPLETED");
            orderRepository.save(order);
            return true;
        }
        return false;
    }
}