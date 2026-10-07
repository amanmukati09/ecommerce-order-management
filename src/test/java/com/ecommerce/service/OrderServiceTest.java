package com.ecommerce.service;

import com.ecommerce.dao.jdbc.OrderProcedureDao;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.OrderView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService using Mockito.
 */
public class OrderServiceTest {

    @Mock
    private OrderProcedureDao orderProcedureDao;

    @InjectMocks
    private OrderService orderService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPlaceOrder() {
        when(orderProcedureDao.placeOrder(1L, 1L, 1L, "WELCOME10", "addr", BigDecimal.TEN))
                .thenReturn(500L);
        Long id = orderService.placeOrder(1L, 1L, 1L, "WELCOME10", "addr", BigDecimal.TEN);
        assertEquals(Long.valueOf(500L), id);
    }

    @Test(expected = InsufficientStockException.class)
    public void testPlaceOrderInsufficientStock() {
        when(orderProcedureDao.placeOrder(anyLong(), anyLong(), any(), any(), any(), any()))
                .thenThrow(new InsufficientStockException("not enough stock"));
        orderService.placeOrder(1L, 1L, 1L, null, "addr", BigDecimal.ZERO);
    }

    @Test
    public void testGetOrder() {
        OrderView o = new OrderView();
        o.setOrderId(10L);
        o.setStatus("PLACED");
        when(orderProcedureDao.getOrderById(10L)).thenReturn(o);
        OrderView result = orderService.getOrder(10L);
        assertEquals("PLACED", result.getStatus());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetOrderNotFound() {
        when(orderProcedureDao.getOrderById(999L)).thenReturn(null);
        orderService.getOrder(999L);
    }

    @Test
    public void testCancelOrder() {
        orderService.cancelOrder(5L, "changed mind");
        verify(orderProcedureDao).cancelOrder(5L, "changed mind");
    }

    @Test
    public void testUpdateStatus() {
        orderService.updateStatus(5L, "PAID", "admin", "note");
        verify(orderProcedureDao).updateOrderStatus(5L, "PAID", "admin", "note");
    }
}
