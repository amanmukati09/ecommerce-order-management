package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.PaymentView;
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
 * Spring JDBC DAO for payment stored procedures (transactional).
 */
@Repository
public class PaymentProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(PaymentProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall processPaymentCall;
    private SimpleJdbcCall refundCall;

    @PostConstruct
    public void init() {
        this.processPaymentCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_process_payment")
                .declareParameters(
                        new SqlParameter("p_order_id", Types.BIGINT),
                        new SqlParameter("p_amount", Types.DECIMAL),
                        new SqlParameter("p_method", Types.VARCHAR),
                        new SqlParameter("p_transaction_id", Types.VARCHAR),
                        new SqlOutParameter("p_payment_id", Types.BIGINT));

        this.refundCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_refund_payment")
                .declareParameters(
                        new SqlParameter("p_payment_id", Types.BIGINT),
                        new SqlOutParameter("p_result", Types.INTEGER));
    }

    public Long processPayment(Long orderId, BigDecimal amount, String method, String transactionId) {
        logger.debug("CALL sp_process_payment order {} amount {}", orderId, amount);
        try {
            Map<String, Object> out = processPaymentCall.execute(new MapSqlParameterSource()
                    .addValue("p_order_id", orderId)
                    .addValue("p_amount", amount)
                    .addValue("p_method", method)
                    .addValue("p_transaction_id", transactionId));
            return ((Number) out.get("p_payment_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public boolean refundPayment(Long paymentId) {
        try {
            Map<String, Object> out = refundCall.execute(
                    new MapSqlParameterSource().addValue("p_payment_id", paymentId));
            Object r = out.get("p_result");
            return r != null && ((Number) r).intValue() == 1;
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public PaymentView getPaymentById(Long paymentId) {
        List<PaymentView> r = jdbcTemplate.query("CALL sp_get_payment_by_id(?)",
                new Object[]{paymentId}, RowMappers.PAYMENT);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<PaymentView> getPaymentsByOrder(Long orderId) {
        return jdbcTemplate.query("CALL sp_get_payments_by_order(?)",
                new Object[]{orderId}, RowMappers.PAYMENT);
    }

    public List<PaymentView> getPaymentsByStatus(String status, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_get_payments_by_status(?, ?, ?)",
                new Object[]{status, limit, offset}, RowMappers.PAYMENT);
    }
}
