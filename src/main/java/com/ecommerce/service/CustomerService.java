package com.ecommerce.service;

import com.ecommerce.dao.hibernate.CategoryHibernateDao;
import com.ecommerce.dao.hibernate.CustomerHibernateDao;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Customer;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.util.ReferenceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for customer and category entity operations (Hibernate-backed).
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerHibernateDao customerHibernateDao;

    @Autowired
    private CategoryHibernateDao categoryHibernateDao;

    public Long createCustomer(Customer customer) {
        if (customer.getEmail() != null && customerHibernateDao.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("email", customer.getEmail());
        }
        if (customer.getCustomerRef() == null) {
            customer.setCustomerRef(ReferenceGenerator.customerRef());
        }
        return customerHibernateDao.save(customer);
    }

    public Customer getCustomer(Long customerId) {
        Customer c = customerHibernateDao.findById(customerId);
        if (c == null) {
            throw new ResourceNotFoundException("Customer", customerId);
        }
        return c;
    }

    public List<Customer> listCustomers(int offset, int limit) {
        return customerHibernateDao.findAll(offset, limit);
    }

    public List<Category> getAllCategories() {
        return categoryHibernateDao.findAll();
    }

    public Category getCategory(Long categoryId) {
        Category c = categoryHibernateDao.findById(categoryId);
        if (c == null) {
            throw new ResourceNotFoundException("Category", categoryId);
        }
        return c;
    }

    public List<Category> getSubcategories(Long parentId) {
        return categoryHibernateDao.findSubcategories(parentId);
    }
}
