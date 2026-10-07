package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for cart operations.
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = Integer.parseInt(body.getOrDefault("quantity", "1").toString());
        Long cartId = cartService.addToCart(customerId, productId, quantity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Item added to cart").withMeta("cartId", cartId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCart(@PathVariable Long customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Long cartItemId,
                                            @RequestBody Map<String, Object> body) {
        int quantity = Integer.parseInt(body.get("quantity").toString());
        cartService.updateItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated"));
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long cartItemId) {
        cartService.removeItem(cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Cart item removed"));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<?> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok(ApiResponse.ok("Cart cleared"));
    }
}
