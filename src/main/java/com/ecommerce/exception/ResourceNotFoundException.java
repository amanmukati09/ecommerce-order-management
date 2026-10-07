package com.ecommerce.exception;

/**
 * Raised when a referenced resource (product, order, customer, etc) does not
 * exist. Translated from a stored-procedure SIGNAL carrying the NOT_FOUND
 * prefix, or thrown directly by the Hibernate DAO layer.
 */
public class ResourceNotFoundException extends EcommerceException {

    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resourceType, Object id) {
        super("NOT_FOUND", resourceType + " not found with id: " + id);
    }
}
