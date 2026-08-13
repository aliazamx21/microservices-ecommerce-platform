package com.payment_service.service;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Session createCheckoutSession(Long orderId, Long amount) {
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setClientReferenceId(String.valueOf(orderId)) // Links Stripe Session to Order ID
                        .setSuccessUrl(frontendUrl + "/payment/success?orderId=" + orderId)
                        .setCancelUrl(frontendUrl + "/payment/cancel?orderId=" + orderId)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("inr")
                                                        .setUnitAmount(amount)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Order Payment #" + orderId)
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
            logger.error("Error creating Stripe Session for orderId {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Error creating Stripe Session", e);
        }
    }

    public boolean markOrderAsPaid(Long orderId) {
        try {
            // Publish payment completion event to Kafka (Order Service listens for this)
            kafkaTemplate.send("payment-completed", String.valueOf(orderId), String.valueOf(orderId));
            logger.info("Published payment-completed event to Kafka for Order ID: {}", orderId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to publish payment-completed event to Kafka: {}", e.getMessage());
            return false;
        }
    }
}