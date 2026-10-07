package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for orders: placement, status, lifecycle transitions.
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body) {
        Long customerId = Long.valueOf(body.get("customerId").toString());
        Long cartId = Long.valueOf(body.get("cartId").toString());
        Long warehouseId = body.get("warehouseId") != null
                ? Long.valueOf(body.get("warehouseId").toString()) : null;
        String discountCode = (String) body.get("discountCode");
        String shippingAddress = (String) body.get("shippingAddress");
        BigDecimal shippingAmount = body.get("shippingAmount") != null
                ? new BigDecimal(body.get("shippingAmount").toString()) : BigDecimal.ZERO;

        Long orderId = orderService.placeOrder(customerId, cartId, warehouseId, discountCode,
                shippingAddress, shippingAmount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed").withMeta("orderId", orderId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderWithItems(orderId));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderStatus(orderId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getByCustomer(@PathVariable Long customerId,
                                           @RequestParam(defaultValue = "20") int limit,
                                           @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId, limit, offset));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(@PathVariable String status,
                                         @RequestParam(defaultValue = "20") int limit,
                                         @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, limit, offset));
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(orderService.getRecentOrders(limit));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long orderId,
                                          @RequestBody Map<String, Object> body) {
        String newStatus = (String) body.get("status");
        String changedBy = (String) body.getOrDefault("changedBy", "API");
        String note = (String) body.get("note");
        orderService.updateStatus(orderId, newStatus, changedBy, note);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated"));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestBody(required = false) Map<String, Object> body) {
        String reason = body != null ? (String) body.get("reason") : "Cancelled by request";
        orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled"));
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<?> countByStatus() {
        return ResponseEntity.ok(orderService.countByStatus());
    }
}
