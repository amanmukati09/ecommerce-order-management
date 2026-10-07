package com.ecommerce.service;

import com.ecommerce.dao.jdbc.ReportProcedureDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service for returns and sales/customer reporting.
 */
@Service
@Transactional
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportProcedureDao reportProcedureDao;

    public Long createReturn(Long orderId, String reason, BigDecimal refundAmount) {
        logger.info("Creating return for order {}", orderId);
        return reportProcedureDao.createReturn(orderId, reason, refundAmount);
    }

    public List<Map<String, Object>> getSalesReport(Date from, Date to) {
        return reportProcedureDao.getSalesReport(from, to);
    }

    public List<Map<String, Object>> getTopCustomers(int limit) {
        return reportProcedureDao.getTopCustomersBySpend(limit);
    }
}
