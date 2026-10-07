package com.ecommerce.exception;

import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Unit tests for the SIGNAL-to-typed-exception translation.
 */
public class SqlErrorTranslatorTest {

    @Test
    public void testInsufficientStock() {
        Exception root = new SQLException("INSUFFICIENT_STOCK: not enough stock");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertTrue(result instanceof InsufficientStockException);
        assertEquals("INSUFFICIENT_STOCK", result.getErrorCode());
        assertEquals("not enough stock", result.getMessage());
    }

    @Test
    public void testDiscountInvalid() {
        Exception root = new SQLException("DISCOUNT_INVALID: discount code is expired");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertTrue(result instanceof DiscountInvalidException);
    }

    @Test
    public void testInvalidStatus() {
        Exception root = new SQLException("INVALID_STATUS: illegal order status transition");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertTrue(result instanceof InvalidStatusTransitionException);
    }

    @Test
    public void testNotFound() {
        Exception root = new SQLException("NOT_FOUND: order does not exist");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertTrue(result instanceof ResourceNotFoundException);
    }

    @Test
    public void testPaymentError() {
        Exception root = new SQLException("PAYMENT_ERROR: payment less than total");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertTrue(result instanceof PaymentException);
    }

    @Test
    public void testInvalidAmount() {
        Exception root = new SQLException("INVALID_AMOUNT: cart is empty");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertTrue(result instanceof InvalidAmountException);
    }

    @Test
    public void testNestedCause() {
        Exception root = new SQLException("INSUFFICIENT_STOCK: out of stock");
        Exception wrapper = new RuntimeException("wrapper", root);
        Exception outer = new RuntimeException("outer", wrapper);
        EcommerceException result = SqlErrorTranslator.translate(outer);
        assertTrue(result instanceof InsufficientStockException);
    }

    @Test
    public void testUnknownBecomesGeneric() {
        Exception root = new SQLException("Some unexpected error");
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertEquals("DB_ERROR", result.getErrorCode());
    }

    @Test
    public void testNullMessage() {
        Exception root = new RuntimeException();
        EcommerceException result = SqlErrorTranslator.translate(root);
        assertEquals("DB_ERROR", result.getErrorCode());
    }
}
