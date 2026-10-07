package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.ReviewView;
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
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC DAO for review submission (transactional, recalcs product rating)
 * and listing stored procedures.
 */
@Repository
public class ReviewProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(ReviewProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall submitReviewCall;

    @PostConstruct
    public void init() {
        this.submitReviewCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_submit_review")
                .declareParameters(
                        new SqlParameter("p_product_id", Types.BIGINT),
                        new SqlParameter("p_customer_id", Types.BIGINT),
                        new SqlParameter("p_rating", Types.TINYINT),
                        new SqlParameter("p_title", Types.VARCHAR),
                        new SqlParameter("p_body", Types.LONGVARCHAR),
                        new SqlOutParameter("p_review_id", Types.BIGINT));
    }

    public Long submitReview(Long productId, Long customerId, int rating, String title, String body) {
        logger.debug("CALL sp_submit_review product {} customer {}", productId, customerId);
        try {
            Map<String, Object> out = submitReviewCall.execute(new MapSqlParameterSource()
                    .addValue("p_product_id", productId)
                    .addValue("p_customer_id", customerId)
                    .addValue("p_rating", rating)
                    .addValue("p_title", title)
                    .addValue("p_body", body));
            return ((Number) out.get("p_review_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public List<ReviewView> getReviewsByProduct(Long productId, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_get_reviews_by_product(?, ?, ?)",
                new Object[]{productId, limit, offset}, RowMappers.REVIEW);
    }
}
