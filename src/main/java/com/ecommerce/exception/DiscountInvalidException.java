package com.ecommerce.exception;

/**
 * Raised when a discount code is invalid, expired, inactive, over its usage
 * limit, or the order does not meet its minimum. Translated from a
 * stored-procedure SIGNAL carrying the DISCOUNT_INVALID prefix.
 */
public class DiscountInvalidException extends EcommerceException {

    public DiscountInvalidException(String message) {
        super("DISCOUNT_INVALID", message);
    }
}
