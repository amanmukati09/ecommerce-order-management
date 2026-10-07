package com.ecommerce.service;

import com.ecommerce.dao.jdbc.InventoryProcedureDao;
import com.ecommerce.model.LowStockView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for inventory operations and the low-stock report.
 */
@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryProcedureDao inventoryProcedureDao;

    public List<LowStockView> getLowStockReport(int limit) {
        logger.info("Generating low-stock report (limit {})", limit);
        return inventoryProcedureDao.getLowStockReport(limit);
    }

    public void adjustInventory(Long productId, Long warehouseId, int delta) {
        inventoryProcedureDao.adjustInventory(productId, warehouseId, delta);
    }

    public void reserveStock(Long productId, Long warehouseId, int quantity) {
        inventoryProcedureDao.reserveStock(productId, warehouseId, quantity);
    }

    public void releaseStock(Long productId, Long warehouseId, int quantity) {
        inventoryProcedureDao.releaseStock(productId, warehouseId, quantity);
    }

    public List<Map<String, Object>> getInventoryByProduct(Long productId) {
        return inventoryProcedureDao.getInventoryByProduct(productId);
    }

    public Map<String, Object> getWarehouseStockValue(Long warehouseId) {
        return inventoryProcedureDao.getWarehouseStockValue(warehouseId);
    }
}
