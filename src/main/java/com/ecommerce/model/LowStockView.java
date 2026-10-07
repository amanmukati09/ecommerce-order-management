package com.ecommerce.model;

import java.math.BigDecimal;

/**
 * LowStockView - read model for the low-stock inventory report.
 */
public class LowStockView {

    private Long inventoryId;
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
    private Integer reorderLevel;
    private Integer reorderQty;
    private String sku;
    private String productName;
    private BigDecimal price;
    private String warehouseCode;
    private String warehouseName;
    private String region;
    private Integer shortfall;

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Integer getReorderQty() { return reorderQty; }
    public void setReorderQty(Integer reorderQty) { this.reorderQty = reorderQty; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }

    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public Integer getShortfall() { return shortfall; }
    public void setShortfall(Integer shortfall) { this.shortfall = shortfall; }
}
