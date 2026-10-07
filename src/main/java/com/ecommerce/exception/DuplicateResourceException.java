package com.ecommerce.exception;

/**
 * Thrown when attempting to create an entity that violates a uniqueness
 * constraint (duplicate SKU, email, customer reference, etc).
 */
public class DuplicateResourceException extends EcommerceException {

    private final String field;

    public DuplicateResourceException(String field, Object value) {
        super("DUPLICATE", "Duplicate " + field + ": " + value);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
