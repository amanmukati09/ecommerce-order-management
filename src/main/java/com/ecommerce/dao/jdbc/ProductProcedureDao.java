package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.ProductView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring JDBC DAO for product catalog stored procedures and scalar functions.
 * Entity CRUD lives in ProductHibernateDao.
 */
@Repository
public class ProductProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(ProductProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ProductView getProductById(Long productId) {
        List<ProductView> r = jdbcTemplate.query("CALL sp_get_product_by_id(?)",
                new Object[]{productId}, RowMappers.PRODUCT);
        return r.isEmpty() ? null : r.get(0);
    }

    public List<ProductView> searchByCategory(Long categoryId, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_search_products_by_category(?, ?, ?)",
                new Object[]{categoryId, limit, offset}, RowMappers.PRODUCT);
    }

    public List<ProductView> search(String query, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_search_products(?, ?, ?)",
                new Object[]{query, limit, offset}, RowMappers.PRODUCT);
    }

    public List<ProductView> getByPriceRange(BigDecimal min, BigDecimal max, int limit, int offset) {
        return jdbcTemplate.query("CALL sp_get_products_price_range(?, ?, ?, ?)",
                new Object[]{min, max, limit, offset}, RowMappers.PRODUCT);
    }

    public List<ProductView> getTopRated(int limit) {
        return jdbcTemplate.query("CALL sp_get_top_rated_products(?)",
                new Object[]{limit}, RowMappers.PRODUCT);
    }

    public void updatePrice(Long productId, BigDecimal price) {
        try {
            jdbcTemplate.update("CALL sp_update_product_price(?, ?)", productId, price);
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void updateStatus(Long productId, String status) {
        jdbcTemplate.update("CALL sp_update_product_status(?, ?)", productId, status);
    }

    public BigDecimal getProductPrice(Long productId) {
        return jdbcTemplate.queryForObject("SELECT fn_get_product_price(?)", BigDecimal.class, productId);
    }

    public BigDecimal getDiscountedPrice(Long productId, String discountCode) {
        return jdbcTemplate.queryForObject("SELECT fn_discounted_price(?, ?)",
                BigDecimal.class, productId, discountCode);
    }

    public int getTotalStock(Long productId) {
        Integer r = jdbcTemplate.queryForObject("SELECT fn_get_total_stock(?)", Integer.class, productId);
        return r == null ? 0 : r;
    }

    public boolean isInStock(Long productId, int quantity) {
        Integer r = jdbcTemplate.queryForObject("SELECT fn_is_in_stock(?, ?)", Integer.class, productId, quantity);
        return r != null && r > 0;
    }
}
