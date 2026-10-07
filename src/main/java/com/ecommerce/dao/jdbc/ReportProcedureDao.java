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
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC DAO for reporting / return stored procedures.
 */
@Repository
public class ReportProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(ReportProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall createReturnCall;

    @PostConstruct
    public void init() {
        this.createReturnCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_create_return")
                .declareParameters(
                        new SqlParameter("p_order_id", Types.BIGINT),
                        new SqlParameter("p_reason", Types.VARCHAR),
                        new SqlParameter("p_refund_amount", Types.DECIMAL),
                        new SqlOutParameter("p_return_id", Types.BIGINT));
    }

    public Long createReturn(Long orderId, String reason, BigDecimal refundAmount) {
        logger.debug("CALL sp_create_return order {}", orderId);
        try {
            Map<String, Object> out = createReturnCall.execute(new MapSqlParameterSource()
                    .addValue("p_order_id", orderId)
                    .addValue("p_reason", reason)
                    .addValue("p_refund_amount", refundAmount == null ? BigDecimal.ZERO : refundAmount));
            return ((Number) out.get("p_return_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public List<Map<String, Object>> getSalesReport(Date from, Date to) {
        Timestamp f = new Timestamp(from.getTime());
        Timestamp t = new Timestamp(to.getTime());
        return jdbcTemplate.query("CALL sp_get_sales_report(?, ?)", new Object[]{f, t}, (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("categoryId", rs.getLong("category_id"));
            row.put("categoryName", rs.getString("category_name"));
            row.put("orderCount", rs.getInt("order_count"));
            row.put("unitsSold", rs.getLong("units_sold"));
            row.put("grossRevenue", rs.getBigDecimal("gross_revenue"));
            return row;
        });
    }

    public List<Map<String, Object>> getTopCustomersBySpend(int limit) {
        return jdbcTemplate.query("CALL sp_get_top_customers_by_spend(?)", new Object[]{limit}, (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("customerId", rs.getLong("customer_id"));
            row.put("firstName", rs.getString("first_name"));
            row.put("lastName", rs.getString("last_name"));
            row.put("email", rs.getString("email"));
            row.put("loyaltyTier", rs.getString("loyalty_tier"));
            row.put("orderCount", rs.getInt("order_count"));
            row.put("totalSpend", rs.getBigDecimal("total_spend"));
            return row;
        });
    }
}
