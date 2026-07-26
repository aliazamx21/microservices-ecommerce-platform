package com.order_service.client;

import com.order_service.dto.CartResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service", url = "http://localhost:8083") // e.g., http://localhost:8080
public interface CartFeignClient {

    // Fetches the cart details using the UUID header
    @GetMapping("/api/v1/cart/{uuid}")
    CartResponseDTO getCart(@PathVariable String uuid);

    // Clears the cart after the order is placed
    @DeleteMapping("/api/v1/cart/{uuid}/clear")
    void clearCart(@PathVariable String uuid);
}
