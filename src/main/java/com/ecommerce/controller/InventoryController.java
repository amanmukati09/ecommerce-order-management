package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for inventory operations and the low-stock report.
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/low-stock")
    public ResponseEntity<?> lowStockReport(@RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(inventoryService.getLowStockReport(limit));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> byProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}/value")
    public ResponseEntity<?> warehouseValue(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getWarehouseStockValue(warehouseId));
    }

    @PostMapping("/adjust")
    public ResponseEntity<?> adjust(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long warehouseId = Long.valueOf(body.get("warehouseId").toString());
        int delta = Integer.parseInt(body.get("delta").toString());
        inventoryService.adjustInventory(productId, warehouseId, delta);
        return ResponseEntity.ok(ApiResponse.ok("Inventory adjusted"));
    }
}
