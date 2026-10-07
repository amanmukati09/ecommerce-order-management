package com.ecommerce.controller;

import com.ecommerce.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for discount codes.
 */
@RestController
@RequestMapping("/discounts")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        BigDecimal orderAmount = new BigDecimal(body.get("orderAmount").toString());
        return ResponseEntity.ok(discountService.applyDiscount(code, orderAmount));
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        Map<String, Object> d = discountService.getByCode(code);
        if (d == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(d);
    }

    @GetMapping("/active")
    public ResponseEntity<?> active() {
        return ResponseEntity.ok(discountService.getActiveDiscounts());
    }
}
