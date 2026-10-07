package com.ecommerce.dao.hibernate;

import com.ecommerce.entity.Product;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hibernate DAO for Product entity CRUD. Catalog search / rating recalculation
 * use stored procedures; this handles entity reads and creation.
 */
@Repository
public class ProductHibernateDao {

    private static final Logger logger = LoggerFactory.getLogger(ProductHibernateDao.class);

    @Autowired
    private SessionFactory sessionFactory;

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    public Product findById(Long productId) {
        return session().get(Product.class, productId);
    }

    public Product findBySku(String sku) {
        Query<Product> q = session().createQuery("FROM Product p WHERE p.sku = :sku", Product.class);
        q.setParameter("sku", sku);
        return q.uniqueResult();
    }

    public List<Product> findByCategory(Long categoryId, int offset, int limit) {
        Query<Product> q = session().createQuery(
                "FROM Product p WHERE p.categoryId = :cid AND p.status = 'ACTIVE' ORDER BY p.name", Product.class);
        q.setParameter("cid", categoryId);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.list();
    }

    public List<Product> findAll(int offset, int limit) {
        Query<Product> q = session().createQuery(
                "FROM Product p WHERE p.status = 'ACTIVE' ORDER BY p.name", Product.class);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.list();
    }

    public Long save(Product product) {
        logger.debug("Saving product {}", product.getSku());
        session().save(product);
        return product.getProductId();
    }

    public void update(Product product) {
        session().merge(product);
    }

    public boolean existsBySku(String sku) {
        Query<Long> q = session().createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.sku = :sku", Long.class);
        q.setParameter("sku", sku);
        return q.uniqueResult() > 0;
    }

    public long countActive() {
        Query<Long> q = session().createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.status = 'ACTIVE'", Long.class);
        return q.uniqueResult();
    }
}
