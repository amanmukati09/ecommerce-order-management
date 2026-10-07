package com.ecommerce.exception;

/**
 * Raised when an operation cannot be fulfilled because available stock is
 * insufficient. Translated from a stored-procedure SIGNAL carrying the
 * INSUFFICIENT_STOCK prefix.
 */
public class InsufficientStockException extends EcommerceException {

    public InsufficientStockException(String message) {
        super("INSUFFICIENT_STOCK", message);
    }
}
