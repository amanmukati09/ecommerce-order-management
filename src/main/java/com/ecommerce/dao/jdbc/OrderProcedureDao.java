package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.OrderItemView;
import com.ecommerce.model.OrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC DAO for order lifecycle stored procedures, including the core
 * transactional sp_place_order. SQLExceptions from SIGNAL are translated to
 * typed EcommerceExceptions before leaving this layer.
 */
@Repository
public class OrderProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall placeOrderCall;
    private SimpleJdbcCall updateStatusCall;
    private SimpleJdbcCall cancelOrderCall;

    @PostConstruct
    public void init() {
        this.placeOrderCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_place_order")
                .declareParameters(
                        new SqlParameter("p_customer_id", Types.BIGINT),
                        new SqlParameter("p_cart_id", Types.BIGINT),
                        new SqlParameter("p_warehouse_id", Types.BIGINT),
                        new SqlParameter("p_discount_code", Types.VARCHAR),
                        new SqlParameter("p_shipping_address", Types.VARCHAR),
                        new SqlParameter("p_shipping_amount", Types.DECIMAL),
                        new SqlOutParameter("p_order_id", Types.BIGINT));

        this.updateStatusCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_update_order_status")
                .declareParameters(
                        new SqlParameter("p_order_id", Types.BIGINT),
                        new SqlParameter("p_new_status", Types.VARCHAR),
                        new SqlParameter("p_changed_by", Types.VARCHAR),
                        new SqlParameter("p_note", Types.VARCHAR));

        this.cancelOrderCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_cancel_order")
                .declareParameters(
                        new SqlParameter("p_order_id", Types.BIGINT),
                        new SqlParameter("p_reason", Types.VARCHAR));
    }

    /** The core transactional order placement. */
    public Long placeOrder(Long customerId, Long cartId, Long warehouseId, String discountCode,
                           String shippingAddress, BigDecimal shippingAmount) {
        logger.debug("CALL sp_place_order customer {} cart {}", customerId, cartId);
        try {
            Map<String, Object> out = placeOrderCall.execute(new MapSqlParameterSource()
                    .addValue("p_customer_id", customerId)
                    .addValue("p_cart_id", cartId)
                    .addValue("p_warehouse_id", warehouseId)
                    .addValue("p_discount_code", discountCode)
                    .addValue("p_shipping_address", shippingAddress)
                    .addValue("p_shipping_amount", shippingAmount == null ? BigDecimal.ZERO : shippingAmount));
            return ((Number) out.get("p_order_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void updateOrderStatus(Long orderId, String newStatus, String changedBy, String note) {
        logger.debug("CALL sp_update_order_status {} -> {}", orderId, newStatus);
        try {
            updateStatusCall.execute(new MapSqlParameterSource()
                    .addValue("p_order_id", orderId)
                    .addValue("p_new_status", newStatus)
                    .addValue("p_changed_by", changedBy)
                    .addValue("p_note", note));
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void cancelOrder(Long orderId, String reason) {
        logger.debug("CALL sp_cancel_order {}", orderId);
        try {
            cancelOrderCall.execute(new MapSqlParameterSource()
                    .addValue("p_order_id", orderId)
                    .addValue("p_reason", reason));
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public OrderView getOrderById(Long orderId) {
        List<OrderView> r = jdbcTemplate.query("CALL sp_get_order_by_id(?)",
                new Object[]{orderId}, RowMappers.ORDER);
        return r.isEmpty() ? null : r.get(0);
    }

    public OrderView getOrderByRef(String orderRef) {
        List<OrderView> r = jdbcTemplate.query("CALL sp_get_order_by_ref(?)",
                new Object[]{orderRef}, RowMappers.ORDER);
        return r.isEmpty() ? null : r.get(0);
    }

    public OrderView getOrderFull(Long orderId) {
        List<OrderView> r = jdbcTemplate.query("CALL sp_get_order_full(?)",
                new Object[]{orderId}, RowMappers.ORDER);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<OrderItemView> getOrderItems(Long orderId) {
        return jdbcTemplate.query("CALL sp_get_order_items(?)",
                new Object[]{orderId}, RowMappers.ORDER_ITEM);
    }

    public List<OrderView> getOrdersByCustomer(Long customerId, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_get_orders_by_customer(?, ?, ?)",
                new Object[]{customerId, limit, offset}, RowMappers.ORDER);
    }

    public List<OrderView> getOrdersByStatus(String status, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_get_orders_by_status(?, ?, ?)",
                new Object[]{status, limit, offset}, RowMappers.ORDER);
    }

    public List<OrderView> getRecentOrders(int limit) {
        return jdbcTemplate.query("CALL sp_get_recent_orders(?)",
                new Object[]{limit}, RowMappers.ORDER);
    }

    public List<Map<String, Object>> getOrderStatusHistory(Long orderId) {
        return jdbcTemplate.query("CALL sp_get_order_status_history(?)", new Object[]{orderId}, (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("historyId", rs.getLong("history_id"));
            row.put("orderId", rs.getLong("order_id"));
            row.put("oldStatus", rs.getString("old_status"));
            row.put("newStatus", rs.getString("new_status"));
            row.put("changedBy", rs.getString("changed_by"));
            row.put("note", rs.getString("note"));
            row.put("createdAt", rs.getTimestamp("created_at"));
            return row;
        });
    }

    public List<Map<String, Object>> countOrdersByStatus() {
        return jdbcTemplate.query("CALL sp_count_orders_by_status()", (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("status", rs.getString("status"));
            row.put("orderCount", rs.getInt("order_count"));
            row.put("totalValue", rs.getBigDecimal("total_value"));
            return row;
        });
    }

    public String getOrderStatus(Long orderId) {
        return jdbcTemplate.queryForObject("SELECT fn_get_order_status(?)", String.class, orderId);
    }
}
