package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for product reviews.
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> submit(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        Long customerId = Long.valueOf(body.get("customerId").toString());
        int rating = Integer.parseInt(body.get("rating").toString());
        String title = (String) body.get("title");
        String reviewBody = (String) body.get("body");

        Long reviewId = reviewService.submitReview(productId, customerId, rating, title, reviewBody);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review submitted").withMeta("reviewId", reviewId));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getByProduct(@PathVariable Long productId,
                                          @RequestParam(defaultValue = "20") int limit,
                                          @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId, limit, offset));
    }
}
