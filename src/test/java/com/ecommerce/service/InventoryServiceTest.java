package com.ecommerce.service;

import com.ecommerce.dao.jdbc.InventoryProcedureDao;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.model.LowStockView;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryService using Mockito.
 */
public class InventoryServiceTest {

    @Mock
    private InventoryProcedureDao inventoryProcedureDao;

    @InjectMocks
    private InventoryService inventoryService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLowStockReport() {
        LowStockView v = new LowStockView();
        v.setProductId(33L);
        v.setQuantity(2);
        v.setShortfall(8);
        when(inventoryProcedureDao.getLowStockReport(10)).thenReturn(Arrays.asList(v));
        List<LowStockView> report = inventoryService.getLowStockReport(10);
        assertEquals(1, report.size());
        assertEquals(Integer.valueOf(8), report.get(0).getShortfall());
    }

    @Test
    public void testAdjustInventory() {
        inventoryService.adjustInventory(1L, 1L, -5);
        verify(inventoryProcedureDao).adjustInventory(1L, 1L, -5);
    }

    @Test
    public void testReserveStockPropagatesInsufficient() {
        doThrow(new InsufficientStockException("not enough available stock"))
                .when(inventoryProcedureDao).reserveStock(33L, 1L, 9999);
        try {
            inventoryService.reserveStock(33L, 1L, 9999);
            fail("Expected InsufficientStockException");
        } catch (InsufficientStockException ex) {
            assertEquals("INSUFFICIENT_STOCK", ex.getErrorCode());
        }
    }

    @Test
    public void testReleaseStock() {
        inventoryService.releaseStock(1L, 1L, 3);
        verify(inventoryProcedureDao).releaseStock(1L, 1L, 3);
    }
}
