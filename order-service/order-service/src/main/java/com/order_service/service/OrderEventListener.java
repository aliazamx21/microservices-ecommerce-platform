package com.order_service.service; // Make sure this matches your folder structure in order-service

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderEventListener {

    private final OrderService orderService;

    public OrderEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    // --- THE SAGA PATTERN: CONSUME PAYMENT EVENT ---
    @KafkaListener(topics = "payment-completed", groupId = "ecommerce-group")
    public void handlePaymentCompletedEvent(String orderIdStr) {
        System.out.println("Kafka Listener received payment-completed event for Order ID: " + orderIdStr);

        try {
            long orderId = Long.parseLong(orderIdStr);

            // Trigger your existing method to update the database
            boolean isUpdated = orderService.markOrderStatus(orderId);

            if (isUpdated) {
                System.out.println("SAGA Pattern Complete: Order " + orderId + " marked as COMPLETED.");
            }
        } catch (Exception e) {
            System.err.println("Error processing payment-completed event: " + e.getMessage());
        }
    }
}