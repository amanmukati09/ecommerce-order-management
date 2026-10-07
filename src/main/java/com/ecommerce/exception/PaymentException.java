package com.ecommerce.exception;

/**
 * Raised when a payment operation is invalid (amount less than order total,
 * refunding a non-captured payment, etc). Translated from a stored-procedure
 * SIGNAL carrying the PAYMENT_ERROR prefix.
 */
public class PaymentException extends EcommerceException {

    public PaymentException(String message) {
        super("PAYMENT_ERROR", message);
    }
}
