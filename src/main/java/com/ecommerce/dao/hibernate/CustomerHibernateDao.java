package com.ecommerce.dao.hibernate;

import com.ecommerce.entity.Customer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Hibernate DAO for Customer entity CRUD. No stored procedure calls here.
 */
@Repository
public class CustomerHibernateDao {

    private static final Logger logger = LoggerFactory.getLogger(CustomerHibernateDao.class);

    @Autowired
    private SessionFactory sessionFactory;

    private Session session() {
        return sessionFactory.getCurrentSession();
    }

    public Customer findById(Long customerId) {
        return session().get(Customer.class, customerId);
    }

    public Customer findByEmail(String email) {
        Query<Customer> q = session().createQuery("FROM Customer c WHERE c.email = :email", Customer.class);
        q.setParameter("email", email);
        return q.uniqueResult();
    }

    public Customer findByRef(String customerRef) {
        Query<Customer> q = session().createQuery("FROM Customer c WHERE c.customerRef = :ref", Customer.class);
        q.setParameter("ref", customerRef);
        return q.uniqueResult();
    }

    public List<Customer> findAll(int offset, int limit) {
        Query<Customer> q = session().createQuery(
                "FROM Customer c ORDER BY c.lastName, c.firstName", Customer.class);
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.list();
    }

    public Long save(Customer customer) {
        logger.debug("Saving customer {}", customer.getCustomerRef());
        session().save(customer);
        return customer.getCustomerId();
    }

    public void update(Customer customer) {
        session().merge(customer);
    }

    public boolean existsByEmail(String email) {
        Query<Long> q = session().createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.email = :email", Long.class);
        q.setParameter("email", email);
        return q.uniqueResult() > 0;
    }
}
