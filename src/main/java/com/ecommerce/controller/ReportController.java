package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * REST controller for returns and reporting.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/returns")
    public ResponseEntity<?> createReturn(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String reason = (String) body.get("reason");
        BigDecimal refundAmount = body.get("refundAmount") != null
                ? new BigDecimal(body.get("refundAmount").toString()) : BigDecimal.ZERO;
        Long returnId = reportService.createReturn(orderId, reason, refundAmount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Return created").withMeta("returnId", returnId));
    }

    @GetMapping("/sales")
    public ResponseEntity<?> salesReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
        return ResponseEntity.ok(reportService.getSalesReport(from, to));
    }

    @GetMapping("/top-customers")
    public ResponseEntity<?> topCustomers(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getTopCustomers(limit));
    }
}
