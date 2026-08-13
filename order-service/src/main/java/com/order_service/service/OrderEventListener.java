package com.order_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    private final OrderService orderService;

    public OrderEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    // --- THE SAGA PATTERN: CONSUME PAYMENT EVENT ---
    @KafkaListener(topics = "payment-completed", groupId = "ecommerce-group")
    public void handlePaymentCompletedEvent(String orderIdStr) {
        logger.info("Kafka Listener received payment-completed event for Order ID: {}", orderIdStr);

        try {
            long orderId = Long.parseLong(orderIdStr);

            // Trigger your method to update the database
            boolean isUpdated = orderService.markOrderStatus(orderId);

            if (isUpdated) {
                logger.info("SAGA Pattern Complete: Order {} marked as COMPLETED.", orderId);
            } else {
                logger.info("Order {} was already COMPLETED or not found. Ignoring duplicate event.", orderId);
            }
        } catch (Exception e) {
            logger.error("Error processing payment-completed event for Order ID {}: {}", orderIdStr, e.getMessage());
        }
    }
}