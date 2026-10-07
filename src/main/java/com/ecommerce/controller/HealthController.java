package com.ecommerce.controller;

import com.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health and info endpoint.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<?> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("application", "ecommerce-order-mgmt");
        status.put("version", "1.0.0");
        try {
            status.put("ordersByStatus", orderService.countByStatus());
            status.put("database", "CONNECTED");
        } catch (Exception e) {
            status.put("database", "ERROR");
            status.put("databaseError", e.getMessage());
        }
        return ResponseEntity.ok(status);
    }
}
