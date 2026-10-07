package com.ecommerce.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the legacy warehouse-region branching logic.
 */
public class WarehouseRegionRouterTest {

    @Test
    public void testPreferredWarehouse() {
        assertEquals(1L, WarehouseRegionRouter.preferredWarehouseForRegion("NY"));
        assertEquals(1L, WarehouseRegionRouter.preferredWarehouseForRegion("EAST"));
        assertEquals(2L, WarehouseRegionRouter.preferredWarehouseForRegion("CA"));
        assertEquals(2L, WarehouseRegionRouter.preferredWarehouseForRegion("WEST"));
        assertEquals(4L, WarehouseRegionRouter.preferredWarehouseForRegion("GA"));
        assertEquals(4L, WarehouseRegionRouter.preferredWarehouseForRegion("SOUTH"));
        assertEquals(3L, WarehouseRegionRouter.preferredWarehouseForRegion("TX"));
        assertEquals(3L, WarehouseRegionRouter.preferredWarehouseForRegion(null));
    }

    @Test
    public void testEstimatedDeliveryDays() {
        assertEquals(2, WarehouseRegionRouter.estimatedDeliveryDays("NY"));
        assertEquals(2, WarehouseRegionRouter.estimatedDeliveryDays("CA"));
        assertEquals(3, WarehouseRegionRouter.estimatedDeliveryDays("TX"));
        assertEquals(5, WarehouseRegionRouter.estimatedDeliveryDays("MT"));
        assertEquals(5, WarehouseRegionRouter.estimatedDeliveryDays(null));
    }

    @Test
    public void testFreeShippingThreshold() {
        assertEquals(50.00, WarehouseRegionRouter.freeShippingThreshold("CA"), 0.001);
        assertEquals(50.00, WarehouseRegionRouter.freeShippingThreshold("NY"), 0.001);
        assertEquals(75.00, WarehouseRegionRouter.freeShippingThreshold("TX"), 0.001);
        assertEquals(75.00, WarehouseRegionRouter.freeShippingThreshold(null), 0.001);
    }
}
