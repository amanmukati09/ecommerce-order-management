package com.ecommerce.controller;

import com.ecommerce.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for categories and customer lookups (Hibernate-backed).
 */
@RestController
@RequestMapping("/catalog")
public class CatalogController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(customerService.getAllCategories());
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> getCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(customerService.getCategory(categoryId));
    }

    @GetMapping("/categories/{parentId}/subcategories")
    public ResponseEntity<?> getSubcategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(customerService.getSubcategories(parentId));
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<?> getCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }
}
