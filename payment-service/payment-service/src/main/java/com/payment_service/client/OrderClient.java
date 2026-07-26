package com.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "order-servie" , url = "http://localhost:8084")
public interface OrderClient {
   @PutMapping("/api/v1/order/{orderId}")
    public boolean updateOrderStatus(
            @PathVariable("orderId") long  orderId) ;
}
