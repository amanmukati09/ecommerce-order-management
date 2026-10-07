package com.ecommerce.controller;

import com.ecommerce.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler translating typed e-commerce exceptions to HTTP
 * responses.
 *
 * NOTE (legacy smell): includes a broad catch-all handler for
 * java.lang.Exception mapping everything else to HTTP 500. Kept as a
 * representative legacy pattern.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleStock(InsufficientStockException ex) {
        logger.warn("Insufficient stock: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(DiscountInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleDiscount(DiscountInvalidException ex) {
        logger.warn("Discount invalid: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(InvalidStatusTransitionException ex) {
        logger.warn("Invalid status transition: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        logger.warn("Not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<Map<String, Object>> handlePayment(PaymentException ex) {
        logger.warn("Payment error: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidAmount(InvalidAmountException ex) {
        logger.warn("Invalid amount: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        logger.warn("Duplicate: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(EcommerceException.class)
    public ResponseEntity<Map<String, Object>> handleEcommerce(EcommerceException ex) {
        logger.warn("Ecommerce error: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        logger.warn("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    // Broad catch-all (legacy smell)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
        logger.error("Unhandled exception caught by global handler", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An internal error occurred");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("errorCode", code);
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}
