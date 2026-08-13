package com.payment_service.controller;

import com.payment_service.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @Value("${stripe.api.webhook-secret:whsec_fallback_key}")
    private String webhookSecret;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<String> createPaymentSession(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "10000") Long amount,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    ) {
        logger.info("Checkout initiated for orderId: {} by user: {}", orderId, username);
        Session session = paymentService.createCheckoutSession(orderId, amount);
        return ResponseEntity.ok(session.getUrl());
    }

    /**
     * STRIPE WEBHOOK (Source of Truth)
     * Stripe calls this directly when a payment succeeds or fails.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            // Cryptographically verify the payload originated from Stripe
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.error("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing error");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

            if (session != null) {
                String orderIdStr = session.getClientReferenceId();
                if (orderIdStr != null) {
                    Long orderId = Long.parseLong(orderIdStr);
                    logger.info("Payment confirmed via Webhook for Order ID: {}", orderId);

                    // Emit Kafka Event to notify Order Service
                    paymentService.markOrderAsPaid(orderId);
                }
            }
        }

        return ResponseEntity.ok("Webhook received successfully");
    }
}