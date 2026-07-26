package com.payment_service.controller;

import com.payment_service.service.PaymentService;
import com.stripe.model.checkout.Session;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/checkout/{orderId}")
    public ResponseEntity<String> createPaymentSession(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-Logged-In-User") String username, // INJECTED BY GATEWAY
            @RequestHeader(value = "X-User-Role") String role           // INJECTED BY GATEWAY
    ) {

        System.out.println("Checkout initiated by user: " + username);

        Session session = paymentService.createCheckoutSession(orderId, 10000L);

        return ResponseEntity.ok(session.getUrl());
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(
            @RequestParam Long orderId,
            // Made required=false in case Stripe's automatic redirect strips headers
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    ) {

        boolean status = paymentService.markOrderAsPaid(orderId);

        if (status) {
            String customerName = (username != null) ? username : "Customer";
            return ResponseEntity.ok("Payment successful for orderId: " + orderId + ". Thank you, " + customerName + "!");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Payment processing failed for orderId: " + orderId);
        }
    }
}