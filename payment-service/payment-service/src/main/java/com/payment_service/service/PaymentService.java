package com.payment_service.service;

import com.payment_service.client.OrderClient;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private OrderClient orderClient;
    public Session createCheckoutSession(Long orderId, Long amount){

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8085/success?orderId=" + orderId)
                        .setCancelUrl("http://localhost:8085/cancel")
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1l)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("inr")
                                                        .setUnitAmount(amount)
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Order Payment" + orderId) // Required field
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();

        // This actually calls the Stripe API to generate the session URL
       try {
           return Session.create(params);
       } catch ( Exception e) {
           throw new RuntimeException("Error creating Stripe Session", e);
       }
    }

    public boolean markOrderAsPaid(Long orderId) {
        boolean status = orderClient.updateOrderStatus(orderId);
        return status;
    }
}