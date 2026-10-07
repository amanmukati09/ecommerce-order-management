package com.ecommerce.service;

import com.ecommerce.dao.jdbc.CartProcedureDao;
import com.ecommerce.exception.InvalidAmountException;
import com.ecommerce.model.CartItemView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for cart operations. The stock-aware add-to-cart logic lives in the
 * stored procedure; this layer validates inputs and composes cart views.
 */
@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartProcedureDao cartProcedureDao;

    public Long getOrCreateCart(Long customerId) {
        return cartProcedureDao.getOrCreateCart(customerId);
    }

    public Long addToCart(Long customerId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new InvalidAmountException("Quantity must be positive");
        }
        Long cartId = cartProcedureDao.getOrCreateCart(customerId);
        cartProcedureDao.addToCart(cartId, productId, quantity);
        logger.info("Added product {} x{} to cart {}", productId, quantity, cartId);
        return cartId;
    }

    public Map<String, Object> getCart(Long customerId) {
        Long cartId = cartProcedureDao.getOrCreateCart(customerId);
        List<CartItemView> items = cartProcedureDao.getCartItems(cartId);
        BigDecimal total = cartProcedureDao.getCartTotal(cartId);
        int itemCount = cartProcedureDao.getCartItemCount(cartId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cartId", cartId);
        result.put("items", items);
        result.put("itemCount", itemCount);
        result.put("total", total);
        return result;
    }

    public List<CartItemView> getCartItems(Long cartId) {
        return cartProcedureDao.getCartItems(cartId);
    }

    public void updateItemQuantity(Long cartItemId, int quantity) {
        cartProcedureDao.updateCartItemQuantity(cartItemId, quantity);
    }

    public void removeItem(Long cartItemId) {
        cartProcedureDao.removeCartItem(cartItemId);
    }

    public void clearCart(Long cartId) {
        cartProcedureDao.clearCart(cartId);
    }
}
