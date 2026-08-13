package com.cart_service.service;

import com.cart_service.dto.AddToCartRequest;
import com.cart_service.entity.Cart;
import com.cart_service.entity.CartItem;
import com.cart_service.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart addToCart(String uuid, String username, AddToCartRequest request) {
        Cart cart = null;

        // 1. Try to find existing cart by username first
        if (username != null && !username.isBlank()) {
            Optional<Cart> userCart = cartRepository.findByUsername(username);
            if (userCart.isPresent()) {
                cart = userCart.get();
            }
        }

        // 2. If no user cart, look up by guest UUID
        if (cart == null && uuid != null && !uuid.isBlank()) {
            Optional<Cart> uuidCart = cartRepository.findByUuid(uuid);
            if (uuidCart.isPresent()) {
                cart = uuidCart.get();
            }
        }

        // 3. Create a new cart if none exists
        if (cart == null) {
            cart = new Cart();
            cart.setUuid(UUID.randomUUID().toString());
        }

        // 4. Attach logged-in username to cart if available
        if (username != null && !username.isBlank()) {
            cart.setUsername(username);
        }

        // 5. Add or update item quantity
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setBrandId(request.getBrandId());
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(request.getPrice());
            newItem.setCart(cart);

            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Optional<Cart> getCart(String uuid, String username) {
        if (username != null && !username.isBlank()) {
            Optional<Cart> userCart = cartRepository.findByUsername(username);
            if (userCart.isPresent()) return userCart;
        }
        if (uuid != null && !uuid.isBlank()) {
            return cartRepository.findByUuid(uuid);
        }
        return Optional.empty();
    }

    public boolean clearCart(String uuid, String username) {
        Optional<Cart> cart = getCart(uuid, username);
        if (cart.isPresent()) {
            cartRepository.delete(cart.get());
            return true;
        }
        return false;
    }
}