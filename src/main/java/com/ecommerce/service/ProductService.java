package com.ecommerce.service;

import com.ecommerce.dao.hibernate.ProductHibernateDao;
import com.ecommerce.dao.jdbc.ProductProcedureDao;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.ProductView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for product catalog operations. Reads/search go through the stored
 * procedure DAO (joined views); entity CRUD uses the Hibernate DAO.
 */
@Service
@Transactional
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductProcedureDao productProcedureDao;

    @Autowired
    private ProductHibernateDao productHibernateDao;

    public ProductView getProduct(Long productId) {
        ProductView p = productProcedureDao.getProductById(productId);
        if (p == null) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return p;
    }

    public List<ProductView> searchByCategory(Long categoryId, int limit, int offset) {
        return productProcedureDao.searchByCategory(categoryId, limit, offset);
    }

    public List<ProductView> search(String query, int limit, int offset) {
        return productProcedureDao.search(query, limit, offset);
    }

    public List<ProductView> getByPriceRange(BigDecimal min, BigDecimal max, int limit, int offset) {
        return productProcedureDao.getByPriceRange(min, max, limit, offset);
    }

    public List<ProductView> getTopRated(int limit) {
        return productProcedureDao.getTopRated(limit);
    }

    public void updatePrice(Long productId, BigDecimal price) {
        productProcedureDao.updatePrice(productId, price);
    }

    public void updateStatus(Long productId, String status) {
        productProcedureDao.updateStatus(productId, status);
    }

    public BigDecimal getDiscountedPrice(Long productId, String discountCode) {
        return productProcedureDao.getDiscountedPrice(productId, discountCode);
    }

    public int getTotalStock(Long productId) {
        return productProcedureDao.getTotalStock(productId);
    }
}
