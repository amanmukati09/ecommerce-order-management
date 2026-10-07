package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.service.PaymentService;
import com.ecommerce.util.ReferenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * REST controller for payments.
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String method = (String) body.getOrDefault("method", "CARD");
        String txnId = body.get("transactionId") != null
                ? (String) body.get("transactionId") : ReferenceGenerator.transactionId();

        Long paymentId = paymentService.processPayment(orderId, amount, method, txnId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment captured").withMeta("paymentId", paymentId));
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<?> refund(@PathVariable Long paymentId) {
        boolean ok = paymentService.refundPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.ok("Payment refunded").withMeta("refunded", ok));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }
}
