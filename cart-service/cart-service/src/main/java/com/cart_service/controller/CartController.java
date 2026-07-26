package com.cart_service.controller;

import com.cart_service.dto.AddToCartRequest;
import com.cart_service.entity.Cart;
import com.cart_service.repository.CartRepository;
import com.cart_service.service.CartService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;
    private final CartRepository cartRepository;

    public CartController(CartService cartService, CartRepository cartRepository) {
        this.cartService = cartService;
        this.cartRepository = cartRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestHeader(value = "X-CART-ID", required = false) String uuid,
            @RequestHeader(value = "X-Logged-In-User") String username, // INJECTED BY GATEWAY
            @RequestHeader(value = "X-User-Role") String role,          // INJECTED BY GATEWAY
            @RequestBody AddToCartRequest request
    ) {
        /*
         * FUTURE UPGRADE: Since you now have the exact 'username' from the Gateway,
         * you can update cartService.addToCart() to save the username to the Cart table in your database.
         * That way, if a user logs out and logs back in, their cart is still there!
         */
        Cart cart = cartService.addToCart(uuid, request);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CART-ID", cart.getUuid());

        return ResponseEntity.ok()
                .headers(headers)
                .body("Product added to cart for user: " + username);
    }

    @GetMapping("/{uuid}")
    public Cart getCart(
            @PathVariable String uuid,
            @RequestHeader(value = "X-Logged-In-User") String username, // INJECTED BY GATEWAY
            @RequestHeader(value = "X-User-Role") String role           // INJECTED BY GATEWAY
    ) {
        return cartRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    @DeleteMapping("/{uuid}/clear")
    public ResponseEntity<String> clearCart(
            @PathVariable String uuid,
            @RequestHeader(value = "X-Logged-In-User") String username, // INJECTED BY GATEWAY
            @RequestHeader(value = "X-User-Role") String role           // INJECTED BY GATEWAY
    ) {
        Cart cart = cartRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartRepository.delete(cart);
        return ResponseEntity.ok("Cart cleared successfully for user: " + username);
    }
}