package com.ecommerce.service;

import com.ecommerce.dao.jdbc.PaymentProcedureDao;
import com.ecommerce.exception.InvalidAmountException;
import com.ecommerce.exception.PaymentException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService using Mockito.
 */
public class PaymentServiceTest {

    @Mock
    private PaymentProcedureDao paymentProcedureDao;

    @InjectMocks
    private PaymentService paymentService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testProcessPayment() {
        when(paymentProcedureDao.processPayment(1L, new BigDecimal("100.00"), "CARD", "TXN1"))
                .thenReturn(10L);
        Long id = paymentService.processPayment(1L, new BigDecimal("100.00"), "CARD", "TXN1");
        assertEquals(Long.valueOf(10L), id);
    }

    @Test(expected = InvalidAmountException.class)
    public void testProcessPaymentZeroAmount() {
        paymentService.processPayment(1L, BigDecimal.ZERO, "CARD", "TXN1");
    }

    @Test(expected = InvalidAmountException.class)
    public void testProcessPaymentNegativeAmount() {
        paymentService.processPayment(1L, new BigDecimal("-5.00"), "CARD", "TXN1");
    }

    @Test
    public void testProcessPaymentPropagatesPaymentError() {
        when(paymentProcedureDao.processPayment(anyLong(), any(), any(), any()))
                .thenThrow(new PaymentException("payment less than total"));
        try {
            paymentService.processPayment(1L, new BigDecimal("5.00"), "CARD", "TXN1");
            fail("Expected PaymentException");
        } catch (PaymentException ex) {
            assertEquals("PAYMENT_ERROR", ex.getErrorCode());
        }
    }

    @Test
    public void testRefundPayment() {
        when(paymentProcedureDao.refundPayment(5L)).thenReturn(true);
        assertTrue(paymentService.refundPayment(5L));
    }
}
