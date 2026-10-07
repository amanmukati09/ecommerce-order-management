package com.ecommerce.service;

import com.ecommerce.dao.jdbc.PaymentProcedureDao;
import com.ecommerce.exception.InvalidAmountException;
import com.ecommerce.model.PaymentView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for payments. The transactional capture + order-status advance
 * happens inside the stored procedure.
 */
@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentProcedureDao paymentProcedureDao;

    public Long processPayment(Long orderId, BigDecimal amount, String method, String transactionId) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Payment amount must be positive");
        }
        logger.info("Processing payment for order {} amount {}", orderId, amount);
        return paymentProcedureDao.processPayment(orderId, amount, method, transactionId);
    }

    public boolean refundPayment(Long paymentId) {
        return paymentProcedureDao.refundPayment(paymentId);
    }

    public PaymentView getPayment(Long paymentId) {
        return paymentProcedureDao.getPaymentById(paymentId);
    }

    public List<PaymentView> getPaymentsByOrder(Long orderId) {
        return paymentProcedureDao.getPaymentsByOrder(orderId);
    }

    public List<PaymentView> getPaymentsByStatus(String status, int limit, int offset) {
        return paymentProcedureDao.getPaymentsByStatus(status, limit, offset);
    }
}
