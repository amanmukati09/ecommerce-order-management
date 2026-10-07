package com.ecommerce.service;

import com.ecommerce.dao.jdbc.DiscountProcedureDao;
import com.ecommerce.exception.DiscountInvalidException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DiscountService using Mockito.
 */
public class DiscountServiceTest {

    @Mock
    private DiscountProcedureDao discountProcedureDao;

    @InjectMocks
    private DiscountService discountService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testApplyDiscount() {
        when(discountProcedureDao.validateDiscount("WELCOME10", new BigDecimal("100.00")))
                .thenReturn(new BigDecimal("10.00"));
        Map<String, Object> result = discountService.applyDiscount("WELCOME10", new BigDecimal("100.00"));
        assertEquals(new BigDecimal("10.00"), result.get("discountAmount"));
        assertEquals(new BigDecimal("90.00"), result.get("finalAmount"));
    }

    @Test
    public void testApplyDiscountPropagatesInvalid() {
        when(discountProcedureDao.validateDiscount(eq("EXPIRED5"), any()))
                .thenThrow(new DiscountInvalidException("discount code is expired"));
        try {
            discountService.applyDiscount("EXPIRED5", new BigDecimal("100.00"));
            fail("Expected DiscountInvalidException");
        } catch (DiscountInvalidException ex) {
            assertEquals("DISCOUNT_INVALID", ex.getErrorCode());
        }
    }

    @Test
    public void testIsValid() {
        when(discountProcedureDao.isValid("WELCOME10")).thenReturn(true);
        assertTrue(discountService.isValid("WELCOME10"));
        when(discountProcedureDao.isValid("EXPIRED5")).thenReturn(false);
        assertFalse(discountService.isValid("EXPIRED5"));
    }
}
