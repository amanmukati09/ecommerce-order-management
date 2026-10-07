package com.ecommerce.service;

import com.ecommerce.dao.jdbc.ShipmentProcedureDao;
import com.ecommerce.model.ShipmentView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for shipments. Creation + order-status advance to SHIPPED and the
 * deliver + advance to DELIVERED happen transactionally in stored procedures.
 */
@Service
@Transactional
public class ShipmentService {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentService.class);

    @Autowired
    private ShipmentProcedureDao shipmentProcedureDao;

    public Long createShipment(Long orderId, Long warehouseId, String carrier,
                               String trackingNumber, BigDecimal shippingCost) {
        logger.info("Creating shipment for order {}", orderId);
        return shipmentProcedureDao.createShipment(orderId, warehouseId, carrier, trackingNumber, shippingCost);
    }

    public void markDelivered(Long shipmentId) {
        shipmentProcedureDao.markDelivered(shipmentId);
    }

    public void updateStatus(Long shipmentId, String status) {
        shipmentProcedureDao.updateStatus(shipmentId, status);
    }

    public ShipmentView getShipment(Long shipmentId) {
        return shipmentProcedureDao.getShipmentById(shipmentId);
    }

    public List<ShipmentView> getShipmentsByOrder(Long orderId) {
        return shipmentProcedureDao.getShipmentsByOrder(orderId);
    }
}
