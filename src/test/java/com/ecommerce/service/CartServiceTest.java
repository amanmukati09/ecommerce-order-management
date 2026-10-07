package com.ecommerce.service;

import com.ecommerce.dao.jdbc.CartProcedureDao;
import com.ecommerce.exception.InvalidAmountException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartService using Mockito.
 */
public class CartServiceTest {

    @Mock
    private CartProcedureDao cartProcedureDao;

    @InjectMocks
    private CartService cartService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddToCart() {
        when(cartProcedureDao.getOrCreateCart(1L)).thenReturn(10L);
        when(cartProcedureDao.addToCart(10L, 5L, 2)).thenReturn(100L);
        Long cartId = cartService.addToCart(1L, 5L, 2);
        assertEquals(Long.valueOf(10L), cartId);
        verify(cartProcedureDao).addToCart(10L, 5L, 2);
    }

    @Test(expected = InvalidAmountException.class)
    public void testAddToCartZeroQuantity() {
        cartService.addToCart(1L, 5L, 0);
    }

    @Test(expected = InvalidAmountException.class)
    public void testAddToCartNegativeQuantity() {
        cartService.addToCart(1L, 5L, -3);
    }

    @Test
    public void testClearCart() {
        cartService.clearCart(10L);
        verify(cartProcedureDao).clearCart(10L);
    }
}
