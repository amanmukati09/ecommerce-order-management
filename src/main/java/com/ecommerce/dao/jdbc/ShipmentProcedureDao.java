package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.ShipmentView;
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
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC DAO for shipment stored procedures (transactional).
 */
@Repository
public class ShipmentProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall createShipmentCall;
    private SimpleJdbcCall markDeliveredCall;

    @PostConstruct
    public void init() {
        this.createShipmentCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_create_shipment")
                .declareParameters(
                        new SqlParameter("p_order_id", Types.BIGINT),
                        new SqlParameter("p_warehouse_id", Types.BIGINT),
                        new SqlParameter("p_carrier", Types.VARCHAR),
                        new SqlParameter("p_tracking_number", Types.VARCHAR),
                        new SqlParameter("p_shipping_cost", Types.DECIMAL),
                        new SqlOutParameter("p_shipment_id", Types.BIGINT));

        this.markDeliveredCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_mark_shipment_delivered")
                .declareParameters(new SqlParameter("p_shipment_id", Types.BIGINT));
    }

    public Long createShipment(Long orderId, Long warehouseId, String carrier,
                               String trackingNumber, BigDecimal shippingCost) {
        logger.debug("CALL sp_create_shipment order {}", orderId);
        try {
            Map<String, Object> out = createShipmentCall.execute(new MapSqlParameterSource()
                    .addValue("p_order_id", orderId)
                    .addValue("p_warehouse_id", warehouseId)
                    .addValue("p_carrier", carrier)
                    .addValue("p_tracking_number", trackingNumber)
                    .addValue("p_shipping_cost", shippingCost == null ? BigDecimal.ZERO : shippingCost));
            return ((Number) out.get("p_shipment_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void markDelivered(Long shipmentId) {
        try {
            markDeliveredCall.execute(new MapSqlParameterSource().addValue("p_shipment_id", shipmentId));
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void updateStatus(Long shipmentId, String status) {
        jdbcTemplate.update("CALL sp_update_shipment_status(?, ?)", shipmentId, status);
    }

    public ShipmentView getShipmentById(Long shipmentId) {
        List<ShipmentView> r = jdbcTemplate.query("CALL sp_get_shipment_by_id(?)",
                new Object[]{shipmentId}, RowMappers.SHIPMENT);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<ShipmentView> getShipmentsByOrder(Long orderId) {
        return jdbcTemplate.query("CALL sp_get_shipments_by_order(?)",
                new Object[]{orderId}, RowMappers.SHIPMENT);
    }
}
