package com.ecommerce.util;

/**
 * Helper encapsulating warehouse-region branching logic.
 *
 * NOTE (legacy smell): simulates multi-region fulfilment quirks where behaviour
 * (preferred warehouse, delivery estimate, free-shipping threshold) is driven by
 * hard-coded conditionals on the region string. A modernized system would move
 * this to a configuration/rules table, but it is kept here as a representative
 * legacy pattern.
 */
public final class WarehouseRegionRouter {

    private WarehouseRegionRouter() {
    }

    /**
     * Returns the preferred warehouse id for a shipping region, based on legacy
     * region-to-warehouse mapping rules.
     */
    public static long preferredWarehouseForRegion(String region) {
        if (region == null) {
            return 3L; // Central default
        }
        String r = region.toUpperCase();
        if (r.equals("NY") || r.equals("MA") || r.equals("PA") || r.equals("DC")
                || r.equals("MD") || r.equals("EAST")) {
            return 1L; // East Coast DC
        } else if (r.equals("CA") || r.equals("OR") || r.equals("WA") || r.equals("NV")
                || r.equals("AZ") || r.equals("WEST")) {
            return 2L; // West Coast DC
        } else if (r.equals("GA") || r.equals("FL") || r.equals("TN") || r.equals("NC")
                || r.equals("SOUTH")) {
            return 4L; // South DC
        } else {
            return 3L; // Central DC
        }
    }

    /** Estimated delivery days based on legacy region rules. */
    public static int estimatedDeliveryDays(String region) {
        if (region == null) {
            return 5;
        }
        String r = region.toUpperCase();
        if (r.equals("NY") || r.equals("CA") || r.equals("EAST") || r.equals("WEST")) {
            return 2; // major metro hubs
        } else if (r.equals("TX") || r.equals("CENTRAL")) {
            return 3;
        } else {
            return 5;
        }
    }

    /** Free-shipping threshold varies by region (legacy promo rules). */
    public static double freeShippingThreshold(String region) {
        if (region == null) {
            return 75.00;
        }
        String r = region.toUpperCase();
        if (r.equals("CA") || r.equals("NY")) {
            return 50.00; // lower threshold in premium markets
        }
        return 75.00;
    }
}
