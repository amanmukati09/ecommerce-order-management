package com.ecommerce.exception;

/**
 * Raised when an amount or quantity is invalid for the requested operation
 * (non-positive quantity, empty cart, negative price, etc). Translated from a
 * stored-procedure SIGNAL carrying the INVALID_AMOUNT prefix.
 */
public class InvalidAmountException extends EcommerceException {

    public InvalidAmountException(String message) {
        super("INVALID_AMOUNT", message);
    }
}
