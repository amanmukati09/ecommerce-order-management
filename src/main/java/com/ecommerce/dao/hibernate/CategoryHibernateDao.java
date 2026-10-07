package com.ecommerce.dao.hibernate;

import com.ecommerce.entity.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hibernate DAO for Category entity CRUD. No stored procedure calls here.
 */
@Repository
public class CategoryHibernateDao {

    private static final Logger logger = LoggerFactory.getLogger(CategoryHibernateDao.class);

    @Autowired
    private SessionFactory sessionFactory;

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    public Category findById(Long categoryId) {
        return session().get(Category.class, categoryId);
    }

    public Category findBySlug(String slug) {
        Query<Category> q = session().createQuery("FROM Category c WHERE c.slug = :slug", Category.class);
        q.setParameter("slug", slug);
        return q.uniqueResult();
    }

    public List<Category> findAll() {
        return session().createQuery(
                "FROM Category c WHERE c.status = 'ACTIVE' ORDER BY c.name", Category.class).list();
    }

    public List<Category> findSubcategories(Long parentId) {
        Query<Category> q = session().createQuery(
                "FROM Category c WHERE c.parentId = :pid AND c.status = 'ACTIVE' ORDER BY c.name", Category.class);
        q.setParameter("pid", parentId);
        return q.list();
    }

    public Long save(Category category) {
        logger.debug("Saving category {}", category.getName());
        session().save(category);
        return category.getCategoryId();
    }

    public void update(Category category) {
        session().merge(category);
    }

    public boolean existsBySlug(String slug) {
        Query<Long> q = session().createQuery(
                "SELECT COUNT(c) FROM Category c WHERE c.slug = :slug", Long.class);
        q.setParameter("slug", slug);
        return q.uniqueResult() > 0;
    }
}
