package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for product catalog browsing and search.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getByCategory(@PathVariable Long categoryId,
                                           @RequestParam(defaultValue = "20") int limit,
                                           @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(productService.searchByCategory(categoryId, limit, offset));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q,
                                    @RequestParam(defaultValue = "20") int limit,
                                    @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(productService.search(q, limit, offset));
    }

    @GetMapping("/price-range")
    public ResponseEntity<?> byPriceRange(@RequestParam BigDecimal min,
                                          @RequestParam BigDecimal max,
                                          @RequestParam(defaultValue = "20") int limit,
                                          @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(productService.getByPriceRange(min, max, limit, offset));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<?> topRated(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(productService.getTopRated(limit));
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<?> getStock(@PathVariable Long productId) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("productId", productId);
        resp.put("totalStock", productService.getTotalStock(productId));
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{productId}/discounted-price")
    public ResponseEntity<?> discountedPrice(@PathVariable Long productId,
                                             @RequestParam String code) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("productId", productId);
        resp.put("discountCode", code);
        resp.put("discountedPrice", productService.getDiscountedPrice(productId, code));
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{productId}/price")
    public ResponseEntity<?> updatePrice(@PathVariable Long productId,
                                         @RequestBody Map<String, Object> body) {
        BigDecimal price = new BigDecimal(body.get("price").toString());
        productService.updatePrice(productId, price);
        return ResponseEntity.ok(ApiResponse.ok("Price updated"));
    }
}
