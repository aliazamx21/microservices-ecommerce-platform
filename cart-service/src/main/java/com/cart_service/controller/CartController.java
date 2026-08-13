package com.cart_service.controller;

import com.cart_service.dto.AddToCartRequest;
import com.cart_service.entity.Cart;
import com.cart_service.service.CartService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestHeader(value = "X-CART-ID", required = false) String uuid,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username,
            @RequestBody AddToCartRequest request
    ) {
        Cart cart = cartService.addToCart(uuid, username, request);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CART-ID", cart.getUuid());

        return ResponseEntity.ok()
                .headers(headers)
                .body(cart);
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(
            @RequestHeader(value = "X-CART-ID", required = false) String uuidHeader,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    ) {
        return cartService.getCart(uuidHeader, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Cart> getCartByUuid(
            @PathVariable String uuid,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    ) {
        return cartService.getCart(uuid, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            @RequestHeader(value = "X-CART-ID", required = false) String uuid,
            @RequestHeader(value = "X-Logged-In-User", required = false) String username
    ) {
        boolean cleared = cartService.clearCart(uuid, username);
        if (cleared) {
            return ResponseEntity.ok("Cart cleared successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cart not found");
    }
}