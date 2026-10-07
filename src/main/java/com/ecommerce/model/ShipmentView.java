package com.ecommerce.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ShipmentView - read model from stored procedure result sets.
 */
public class ShipmentView {

    private Long shipmentId;
    private String shipmentRef;
    private Long orderId;
    private Long warehouseId;
    private String carrier;
    private String trackingNumber;
    private String status;
    private BigDecimal shippingCost;
    private Date shippedAt;
    private Date deliveredAt;
    private Date createdAt;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getShipmentRef() { return shipmentRef; }
    public void setShipmentRef(String shipmentRef) { this.shipmentRef = shipmentRef; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getShippingCost() { return shippingCost; }
    public void setShippingCost(BigDecimal shippingCost) { this.shippingCost = shippingCost; }

    public Date getShippedAt() { return shippedAt; }
    public void setShippedAt(Date shippedAt) { this.shippedAt = shippedAt; }

    public Date getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Date deliveredAt) { this.deliveredAt = deliveredAt; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
