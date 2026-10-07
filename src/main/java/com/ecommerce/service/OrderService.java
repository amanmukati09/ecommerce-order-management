package com.ecommerce.service;

import com.ecommerce.dao.jdbc.OrderProcedureDao;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.OrderItemView;
import com.ecommerce.model.OrderView;
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
 * Service for order placement and lifecycle. The atomic placement, inventory
 * decrement, discount application and status transitions all happen inside the
 * stored procedures; this layer composes views and validates inputs. The
 * procedure DAO translates SIGNAL errors into typed exceptions.
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderProcedureDao orderProcedureDao;

    public Long placeOrder(Long customerId, Long cartId, Long warehouseId, String discountCode,
                           String shippingAddress, BigDecimal shippingAmount) {
        logger.info("Placing order for customer {} cart {}", customerId, cartId);
        return orderProcedureDao.placeOrder(customerId, cartId, warehouseId, discountCode,
                shippingAddress, shippingAmount);
    }

    public Map<String, Object> getOrderWithItems(Long orderId) {
        OrderView order = orderProcedureDao.getOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        List<OrderItemView> items = orderProcedureDao.getOrderItems(orderId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    public OrderView getOrder(Long orderId) {
        OrderView order = orderProcedureDao.getOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return order;
    }

    public Map<String, Object> getOrderStatus(Long orderId) {
        OrderView order = orderProcedureDao.getOrderById(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("orderRef", order.getOrderRef());
        result.put("status", order.getStatus());
        result.put("totalAmount", order.getTotalAmount());
        result.put("history", orderProcedureDao.getOrderStatusHistory(orderId));
        return result;
    }

    public List<OrderItemView> getOrderItems(Long orderId) {
        return orderProcedureDao.getOrderItems(orderId);
    }

    public List<OrderView> getOrdersByCustomer(Long customerId, int limit, int offset) {
        return orderProcedureDao.getOrdersByCustomer(customerId, limit, offset);
    }

    public List<OrderView> getOrdersByStatus(String status, int limit, int offset) {
        return orderProcedureDao.getOrdersByStatus(status, limit, offset);
    }

    public List<OrderView> getRecentOrders(int limit) {
        return orderProcedureDao.getRecentOrders(limit);
    }

    public void updateStatus(Long orderId, String newStatus, String changedBy, String note) {
        orderProcedureDao.updateOrderStatus(orderId, newStatus, changedBy, note);
    }

    public void cancelOrder(Long orderId, String reason) {
        orderProcedureDao.cancelOrder(orderId, reason);
    }

    public List<Map<String, Object>> countByStatus() {
        return orderProcedureDao.countOrdersByStatus();
    }
}
