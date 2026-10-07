package com.ecommerce.dao.jdbc;

import com.ecommerce.exception.SqlErrorTranslator;
import com.ecommerce.model.LowStockView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Spring JDBC DAO for inventory stored procedures and the low-stock report.
 */
@Repository
public class InventoryProcedureDao {

    private static final Logger logger = LoggerFactory.getLogger(InventoryProcedureDao.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<LowStockView> getLowStockReport(int limit) {
        logger.debug("CALL sp_get_low_stock_report limit {}", limit);
        return jdbcTemplate.query("CALL sp_get_low_stock_report(?)",
                new Object[]{limit}, RowMappers.LOW_STOCK);
    }

    public void adjustInventory(Long productId, Long warehouseId, int delta) {
        try {
            jdbcTemplate.update("CALL sp_adjust_inventory(?, ?, ?)", productId, warehouseId, delta);
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void reserveStock(Long productId, Long warehouseId, int quantity) {
        try {
            jdbcTemplate.update("CALL sp_reserve_stock(?, ?, ?)", productId, warehouseId, quantity);
        } catch (RuntimeException ex) {
            throw SqlErrorTranslator.translate(ex);
        }
    }

    public void releaseStock(Long productId, Long warehouseId, int quantity) {
        jdbcTemplate.update("CALL sp_release_stock(?, ?, ?)", productId, warehouseId, quantity);
    }

    public List<Map<String, Object>> getInventoryByProduct(Long productId) {
        return jdbcTemplate.query("CALL sp_get_inventory_by_product(?)", new Object[]{productId}, (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("inventoryId", rs.getLong("inventory_id"));
            row.put("productId", rs.getLong("product_id"));
            row.put("warehouseId", rs.getLong("warehouse_id"));
            row.put("quantity", rs.getInt("quantity"));
            row.put("reserved", rs.getInt("reserved"));
            row.put("reorderLevel", rs.getInt("reorder_level"));
            row.put("warehouseCode", rs.getString("warehouse_code"));
            row.put("warehouseName", rs.getString("warehouse_name"));
            row.put("region", rs.getString("region"));
            return row;
        });
    }

    public Map<String, Object> getWarehouseStockValue(Long warehouseId) {
        List<Map<String, Object>> rows = jdbcTemplate.query(
                "CALL sp_get_warehouse_stock_value(?)", new Object[]{warehouseId}, (rs, n) -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("warehouseId", rs.getLong("warehouse_id"));
            row.put("code", rs.getString("code"));
            row.put("name", rs.getString("name"));
            row.put("productCount", rs.getInt("product_count"));
            row.put("totalUnits", rs.getLong("total_units"));
            row.put("totalCostValue", rs.getBigDecimal("total_cost_value"));
            row.put("totalRetailValue", rs.getBigDecimal("total_retail_value"));
            return row;
        });
        return rows.isEmpty() ? null : rows.get(0);
    }
}
