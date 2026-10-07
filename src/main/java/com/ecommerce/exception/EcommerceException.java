package com.ecommerce.exception;

/**
 * Base type for all application-level e-commerce exceptions. Carries a stable
 * error code that controllers map to an HTTP status / JSON error payload.
 */
public class EcommerceException extends RuntimeException {

    private final String errorCode;

    public EcommerceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public EcommerceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
