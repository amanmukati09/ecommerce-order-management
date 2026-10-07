package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
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
 * Spring JDBC DAO for discount code stored procedures.
 */
@Repository
public class DiscountProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(DiscountProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall validateCall;

    @PostConstruct
    public void init() {
        this.validateCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_validate_discount")
                .declareParameters(
                        new SqlParameter("p_code", Types.VARCHAR),
                        new SqlParameter("p_order_amount", Types.DECIMAL),
                        new SqlOutParameter("p_discount_amount", Types.DECIMAL));
    }

    /** Validates a discount against an order amount, returning the computed discount. */
    public BigDecimal validateDiscount(String code, BigDecimal orderAmount) {
        logger.debug("CALL sp_validate_discount {} amount {}", code, orderAmount);
        try {
            Map<String, Object> out = validateCall.execute(new MapSqlParameterSource()
                    .addValue("p_code", code)
                    .addValue("p_order_amount", orderAmount));
            Object v = out.get("p_discount_amount");
            return v == null ? BigDecimal.ZERO : (BigDecimal) v;
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public Map<String, Object> getByCode(String code) {
        List<Map<String, Object>> rows = jdbcTemplate.query("CALL sp_get_discount_by_code(?)",
                new Object[]{code}, (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("discountId", rs.getLong("discount_id"));
            row.put("code", rs.getString("code"));
            row.put("description", rs.getString("description"));
            row.put("discountType", rs.getString("discount_type"));
            row.put("discountValue", rs.getBigDecimal("discount_value"));
            row.put("minOrderAmount", rs.getBigDecimal("min_order_amount"));
            row.put("maxDiscount", rs.getBigDecimal("max_discount"));
            row.put("validFrom", rs.getTimestamp("valid_from"));
            row.put("validTo", rs.getTimestamp("valid_to"));
            row.put("usageLimit", rs.getObject("usage_limit"));
            row.put("timesUsed", rs.getInt("times_used"));
            row.put("status", rs.getString("status"));
            return row;
        });
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Map<String, Object>> getActiveDiscounts() {
        return jdbcTemplate.query("CALL sp_get_active_discounts()", (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("discountId", rs.getLong("discount_id"));
            row.put("code", rs.getString("code"));
            row.put("description", rs.getString("description"));
            row.put("discountType", rs.getString("discount_type"));
            row.put("discountValue", rs.getBigDecimal("discount_value"));
            row.put("minOrderAmount", rs.getBigDecimal("min_order_amount"));
            row.put("validTo", rs.getTimestamp("valid_to"));
            return row;
        });
    }

    public boolean isValid(String code) {
        Integer r = jdbcTemplate.queryForObject("SELECT fn_is_discount_valid(?)", Integer.class, code);
        return r != null && r > 0;
    }
}
