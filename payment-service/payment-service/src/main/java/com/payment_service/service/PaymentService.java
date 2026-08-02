package com.payment_service.service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Session createCheckoutSession(Long orderId, Long amount){
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8085/success?orderId=" + orderId)
                        .setCancelUrl("http://localhost:8085/cancel")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("inr")
                                                        .setUnitAmount(amount)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Order Payment " + orderId) // Required field
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        try {
            return Session.create(params);
        } catch (Exception e) {
            throw new RuntimeException("Error creating Stripe Session", e);
        }
    }

    // --- THE SAGA PATTERN: PUBLISH EVENT INSTEAD OF FEIGN CALL ---
    public boolean markOrderAsPaid(Long orderId) {
        try {
            // Publish event to Kafka. The Order Service will listen for this.
            kafkaTemplate.send("payment-completed", String.valueOf(orderId), String.valueOf(orderId));
            System.out.println("Published payment-completed event to Kafka for Order ID: " + orderId);
            return true;
        } catch (Exception e) {
            System.err.println("Warning: Failed to publish to Kafka: " + e.getMessage());
            return false;
        }
    }
}