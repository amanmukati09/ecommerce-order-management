package com.ecommerce.exception;

/**
 * Raised when an order (or product/customer) status transition is not allowed.
 * Translated from a stored-procedure SIGNAL carrying the INVALID_STATUS prefix.
 */
public class InvalidStatusTransitionException extends EcommerceException {

    public InvalidStatusTransitionException(String message) {
        super("INVALID_STATUS", message);
    }
}
