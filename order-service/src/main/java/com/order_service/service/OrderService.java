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
    private final CircuitBreakerFactory circuitBreakerFactory;

    public OrderService(OrderRepository orderRepository, CartFeignClient cartFeignClient,
                        KafkaTemplate<String, String> kafkaTemplate, CircuitBreakerFactory circuitBreakerFactory) {
        this.orderRepository = orderRepository;
        this.cartFeignClient = cartFeignClient;
        this.kafkaTemplate = kafkaTemplate;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Transactional
    public Order placeOrder(String cartUuid, String username, Long userId) {

        CartResponseDTO cart = circuitBreakerFactory.create("cartService").run(
                () -> cartFeignClient.getCart(cartUuid, username),
                throwable -> {
                    logger.error("Cart Service is currently unavailable. Fallback triggered. Error: {}", throwable.getMessage());
                    return null;
                }
        );

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is Empty or Cart Service is unavailable.");
        }

        Order order = new Order();
        order.setUserId(userId != null ? userId : cart.getUserId());
        order.setCartuuid(cartUuid);
        order.setStatus("CREATED");

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
        Order savedOrder = orderRepository.save(order);

        try {
            String eventPayload = String.format("{\"orderId\":%d, \"amount\":%f}", savedOrder.getId(), savedOrder.getTotalAmount());
            kafkaTemplate.send("order-created", String.valueOf(savedOrder.getId()), eventPayload);
            logger.info("Published OrderCreatedEvent to Kafka for Order ID: {}", savedOrder.getId());
        } catch (Exception e) {
            logger.error("Failed to publish to Kafka: {}", e.getMessage());
        }

        try {
            circuitBreakerFactory.create("cartServiceClear").run(
                    () -> { cartFeignClient.clearCart(cartUuid, username); return true; },
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

            if ("COMPLETED".equals(order.getStatus())) {
                return false;
            }

            order.setStatus("COMPLETED");
            orderRepository.save(order);
            return true;
        }
        return false;
    }
}