package com.ecommerce.service;

import com.ecommerce.dao.jdbc.DiscountProcedureDao;
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
 * Service for discount code validation and lookup.
 */
@Service
@Transactional
public class DiscountService {

    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);

    @Autowired
    private DiscountProcedureDao discountProcedureDao;

    /**
     * Applies/validates a discount code against an order amount. Throws
     * DiscountInvalidException (translated from SIGNAL) if the code is invalid.
     */
    public Map<String, Object> applyDiscount(String code, BigDecimal orderAmount) {
        BigDecimal discount = discountProcedureDao.validateDiscount(code, orderAmount);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", code);
        result.put("orderAmount", orderAmount);
        result.put("discountAmount", discount);
        result.put("finalAmount", orderAmount.subtract(discount));
        return result;
    }

    public Map<String, Object> getByCode(String code) {
        return discountProcedureDao.getByCode(code);
    }

    public List<Map<String, Object>> getActiveDiscounts() {
        return discountProcedureDao.getActiveDiscounts();
    }

    public boolean isValid(String code) {
        return discountProcedureDao.isValid(code);
    }
}
