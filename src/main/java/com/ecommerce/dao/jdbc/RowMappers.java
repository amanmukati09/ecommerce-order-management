package com.ecommerce.dao.jdbc;

import com.ecommerce.model.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Central registry of RowMapper instances used by the Spring JDBC stored
 * procedure DAOs. Each mapper is null-safe against columns a particular
 * procedure may not include in its result set.
 */
public final class RowMappers {

    private RowMappers() {
    }

    private static Set<String> columnLabels(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        Set<String> labels = new HashSet<>();
        for (int i = 1; i <= count; i++) {
            labels.add(meta.getColumnLabel(i).toLowerCase());
        }
        return labels;
    }

    public static final RowMapper<ProductView> PRODUCT = (rs, rowNum) -> {
        Set<String> cols = columnLabels(rs);
        ProductView p = new ProductView();
        p.setProductId(rs.getLong("product_id"));
        p.setSku(rs.getString("sku"));
        p.setName(rs.getString("name"));
        if (cols.contains("description")) p.setDescription(rs.getString("description"));
        if (cols.contains("category_id")) p.setCategoryId(rs.getLong("category_id"));
        p.setPrice(rs.getBigDecimal("price"));
        if (cols.contains("cost")) p.setCost(rs.getBigDecimal("cost"));
        if (cols.contains("weight_kg")) p.setWeightKg(rs.getBigDecimal("weight_kg"));
        if (cols.contains("currency")) p.setCurrency(rs.getString("currency"));
        if (cols.contains("rating_avg")) p.setRatingAvg(rs.getBigDecimal("rating_avg"));
        if (cols.contains("rating_count")) p.setRatingCount(rs.getInt("rating_count"));
        if (cols.contains("status")) p.setStatus(rs.getString("status"));
        if (cols.contains("created_at")) p.setCreatedAt(rs.getTimestamp("created_at"));
        if (cols.contains("updated_at")) p.setUpdatedAt(rs.getTimestamp("updated_at"));
        if (cols.contains("category_name")) p.setCategoryName(rs.getString("category_name"));
        return p;
    };

    public static final RowMapper<OrderView> ORDER = (rs, rowNum) -> {
        Set<String> cols = columnLabels(rs);
        OrderView o = new OrderView();
        o.setOrderId(rs.getLong("order_id"));
        o.setOrderRef(rs.getString("order_ref"));
        o.setCustomerId(rs.getLong("customer_id"));
        if (cols.contains("warehouse_id")) {
            long w = rs.getLong("warehouse_id");
            o.setWarehouseId(rs.wasNull() ? null : w);
        }
        if (cols.contains("discount_id")) {
            long d = rs.getLong("discount_id");
            o.setDiscountId(rs.wasNull() ? null : d);
        }
        o.setStatus(rs.getString("status"));
        if (cols.contains("subtotal")) o.setSubtotal(rs.getBigDecimal("subtotal"));
        if (cols.contains("discount_amount")) o.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        if (cols.contains("tax_amount")) o.setTaxAmount(rs.getBigDecimal("tax_amount"));
        if (cols.contains("shipping_amount")) o.setShippingAmount(rs.getBigDecimal("shipping_amount"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        if (cols.contains("currency")) o.setCurrency(rs.getString("currency"));
        if (cols.contains("shipping_address")) o.setShippingAddress(rs.getString("shipping_address"));
        if (cols.contains("placed_at")) o.setPlacedAt(rs.getTimestamp("placed_at"));
        if (cols.contains("updated_at")) o.setUpdatedAt(rs.getTimestamp("updated_at"));
        if (cols.contains("first_name")) o.setFirstName(rs.getString("first_name"));
        if (cols.contains("last_name")) o.setLastName(rs.getString("last_name"));
        if (cols.contains("email")) o.setEmail(rs.getString("email"));
        if (cols.contains("item_count")) o.setItemCount(rs.getInt("item_count"));
        return o;
    };

    public static final RowMapper<OrderItemView> ORDER_ITEM = (rs, rowNum) -> {
        OrderItemView i = new OrderItemView();
        i.setOrderItemId(rs.getLong("order_item_id"));
        i.setOrderId(rs.getLong("order_id"));
        i.setProductId(rs.getLong("product_id"));
        i.setSku(rs.getString("sku"));
        i.setProductName(rs.getString("product_name"));
        i.setQuantity(rs.getInt("quantity"));
        i.setUnitPrice(rs.getBigDecimal("unit_price"));
        i.setLineTotal(rs.getBigDecimal("line_total"));
        i.setCreatedAt(rs.getTimestamp("created_at"));
        return i;
    };

    public static final RowMapper<CartItemView> CART_ITEM = (rs, rowNum) -> {
        CartItemView c = new CartItemView();
        c.setCartItemId(rs.getLong("cart_item_id"));
        c.setCartId(rs.getLong("cart_id"));
        c.setProductId(rs.getLong("product_id"));
        c.setQuantity(rs.getInt("quantity"));
        c.setUnitPrice(rs.getBigDecimal("unit_price"));
        c.setLineTotal(rs.getBigDecimal("line_total"));
        c.setSku(rs.getString("sku"));
        c.setProductName(rs.getString("product_name"));
        c.setProductStatus(rs.getString("product_status"));
        return c;
    };

    public static final RowMapper<PaymentView> PAYMENT = (rs, rowNum) -> {
        Set<String> cols = columnLabels(rs);
        PaymentView p = new PaymentView();
        p.setPaymentId(rs.getLong("payment_id"));
        p.setPaymentRef(rs.getString("payment_ref"));
        p.setOrderId(rs.getLong("order_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        if (cols.contains("currency")) p.setCurrency(rs.getString("currency"));
        p.setMethod(rs.getString("method"));
        p.setStatus(rs.getString("status"));
        if (cols.contains("transaction_id")) p.setTransactionId(rs.getString("transaction_id"));
        if (cols.contains("created_at")) p.setCreatedAt(rs.getTimestamp("created_at"));
        if (cols.contains("updated_at")) p.setUpdatedAt(rs.getTimestamp("updated_at"));
        return p;
    };

    public static final RowMapper<ShipmentView> SHIPMENT = (rs, rowNum) -> {
        ShipmentView s = new ShipmentView();
        s.setShipmentId(rs.getLong("shipment_id"));
        s.setShipmentRef(rs.getString("shipment_ref"));
        s.setOrderId(rs.getLong("order_id"));
        long w = rs.getLong("warehouse_id");
        s.setWarehouseId(rs.wasNull() ? null : w);
        s.setCarrier(rs.getString("carrier"));
        s.setTrackingNumber(rs.getString("tracking_number"));
        s.setStatus(rs.getString("status"));
        s.setShippingCost(rs.getBigDecimal("shipping_cost"));
        s.setShippedAt(rs.getTimestamp("shipped_at"));
        s.setDeliveredAt(rs.getTimestamp("delivered_at"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    };

    public static final RowMapper<LowStockView> LOW_STOCK = (rs, rowNum) -> {
        LowStockView l = new LowStockView();
        l.setInventoryId(rs.getLong("inventory_id"));
        l.setProductId(rs.getLong("product_id"));
        l.setWarehouseId(rs.getLong("warehouse_id"));
        l.setQuantity(rs.getInt("quantity"));
        l.setReorderLevel(rs.getInt("reorder_level"));
        l.setReorderQty(rs.getInt("reorder_qty"));
        l.setSku(rs.getString("sku"));
        l.setProductName(rs.getString("product_name"));
        l.setPrice(rs.getBigDecimal("price"));
        l.setWarehouseCode(rs.getString("warehouse_code"));
        l.setWarehouseName(rs.getString("warehouse_name"));
        l.setRegion(rs.getString("region"));
        l.setShortfall(rs.getInt("shortfall"));
        return l;
    };

    public static final RowMapper<ReviewView> REVIEW = (rs, rowNum) -> {
        Set<String> cols = columnLabels(rs);
        ReviewView r = new ReviewView();
        r.setReviewId(rs.getLong("review_id"));
        r.setProductId(rs.getLong("product_id"));
        r.setCustomerId(rs.getLong("customer_id"));
        r.setRating(rs.getInt("rating"));
        r.setTitle(rs.getString("title"));
        r.setBody(rs.getString("body"));
        r.setVerifiedPurchase(rs.getBoolean("verified_purchase"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        if (cols.contains("first_name")) r.setFirstName(rs.getString("first_name"));
        if (cols.contains("last_name")) r.setLastName(rs.getString("last_name"));
        return r;
    };
}
