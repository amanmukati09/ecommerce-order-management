package com.ecommerce.dao.hibernate;

import com.ecommerce.entity.Review;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hibernate DAO for Review entity reads. Submission goes through a stored
 * procedure (sp_submit_review).
 */
@Repository
public class ReviewHibernateDao {

    private static final Logger logger = LoggerFactory.getLogger(ReviewHibernateDao.class);

    @Autowired
    private SessionFactory sessionFactory;

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    public Review findById(Long reviewId) {
        return session().get(Review.class, reviewId);
    }

    public List<Review> findByProduct(Long productId, int offset, int limit) {
        Query<Review> q = session().createQuery(
                "FROM Review r WHERE r.productId = :pid AND r.status = 'PUBLISHED' ORDER BY r.createdAt DESC",
                Review.class);
        q.setParameter("pid", productId);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.list();
    }

    public List<Review> findByCustomer(Long customerId) {
        Query<Review> q = session().createQuery(
                "FROM Review r WHERE r.customerId = :cid ORDER BY r.createdAt DESC", Review.class);
        q.setParameter("cid", customerId);
        return q.list();
    }

    public long countByProduct(Long productId) {
        Query<Long> q = session().createQuery(
                "SELECT COUNT(r) FROM Review r WHERE r.productId = :pid AND r.status = 'PUBLISHED'", Long.class);
        q.setParameter("pid", productId);
        return q.uniqueResult();
    }
}
