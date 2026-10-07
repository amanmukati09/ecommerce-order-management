package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.CartItemView;
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
 * Spring JDBC DAO for cart stored procedures.
 */
@Repository
public class CartProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(CartProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall getOrCreateCartCall;
    private SimpleJdbcCall addToCartCall;

    @PostConstruct
    public void init() {
        this.getOrCreateCartCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_get_or_create_cart")
                .declareParameters(
                        new SqlParameter("p_customer_id", Types.BIGINT),
                        new SqlOutParameter("p_cart_id", Types.BIGINT));

        this.addToCartCall = new SimpleJdbcCall(dataSource).withProcedureName("sp_add_to_cart")
                .declareParameters(
                        new SqlParameter("p_cart_id", Types.BIGINT),
                        new SqlParameter("p_product_id", Types.BIGINT),
                        new SqlParameter("p_quantity", Types.INTEGER),
                        new SqlOutParameter("p_cart_item_id", Types.BIGINT));
    }

    public Long getOrCreateCart(Long customerId) {
        try {
            Map<String, Object> out = getOrCreateCartCall.execute(
                    new MapSqlParameterSource().addValue("p_customer_id", customerId));
            return ((Number) out.get("p_cart_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public Long addToCart(Long cartId, Long productId, int quantity) {
        logger.debug("CALL sp_add_to_cart cart {} product {} qty {}", cartId, productId, quantity);
        try {
            Map<String, Object> out = addToCartCall.execute(new MapSqlParameterSource()
                    .addValue("p_cart_id", cartId)
                    .addValue("p_product_id", productId)
                    .addValue("p_quantity", quantity));
            return ((Number) out.get("p_cart_item_id")).longValue();
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        jdbcTemplate.update("CALL sp_update_cart_item_quantity(?, ?)", cartItemId, quantity);
    }

    public void removeCartItem(Long cartItemId) {
        jdbcTemplate.update("CALL sp_remove_cart_item(?)", cartItemId);
    }

    public List<CartItemView> getCartItems(Long cartId) {
        return jdbcTemplate.query("CALL sp_get_cart_items(?)",
                new Object[]{cartId}, RowMappers.CART_ITEM);
    }

    public void clearCart(Long cartId) {
        jdbcTemplate.update("CALL sp_clear_cart(?)", cartId);
    }

    public BigDecimal getCartTotal(Long cartId) {
        return jdbcTemplate.queryForObject("SELECT fn_get_cart_total(?)", BigDecimal.class, cartId);
    }

    public int getCartItemCount(Long cartId) {
        Integer r = jdbcTemplate.queryForObject("SELECT fn_get_cart_item_count(?)", Integer.class, cartId);
        return r == null ? 0 : r;
    }
}
