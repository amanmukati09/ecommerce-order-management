package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for shipments.
 */
@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    @Autowired
    private ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<?> createShipment(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long warehouseId = body.get("warehouseId") != null
                ? Long.valueOf(body.get("warehouseId").toString()) : null;
        String carrier = (String) body.getOrDefault("carrier", "UPS");
        String tracking = (String) body.get("trackingNumber");
        BigDecimal cost = body.get("shippingCost") != null
                ? new BigDecimal(body.get("shippingCost").toString()) : BigDecimal.ZERO;

        Long shipmentId = shipmentService.createShipment(orderId, warehouseId, carrier, tracking, cost);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Shipment created").withMeta("shipmentId", shipmentId));
    }

    @PutMapping("/{shipmentId}/deliver")
    public ResponseEntity<?> markDelivered(@PathVariable Long shipmentId) {
        shipmentService.markDelivered(shipmentId);
        return ResponseEntity.ok(ApiResponse.ok("Shipment marked delivered"));
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<?> getShipment(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(shipmentService.getShipment(shipmentId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentsByOrder(orderId));
    }
}
