package com.order_service.client;

import com.order_service.dto.CartResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

// Removed hardcoded URL. Eureka will dynamically find "cart-service"
@FeignClient(name = "cart-service")
public interface CartFeignClient {

    @GetMapping("/api/v1/cart")
    CartResponseDTO getCart(
            @RequestHeader(value = "X-CART-ID", required = false) String uuid,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    );

    @DeleteMapping("/api/v1/cart/clear")
    void clearCart(
            @RequestHeader(value = "X-CART-ID", required = false) String uuid,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    );
}