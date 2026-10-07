package com.ecommerce.service;

import com.ecommerce.dao.jdbc.ReviewProcedureDao;
import com.ecommerce.exception.InvalidAmountException;
import com.ecommerce.model.ReviewView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for product reviews. Submission (which recalculates the product
 * rating) happens transactionally in the stored procedure.
 */
@Service
@Transactional
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewProcedureDao reviewProcedureDao;

    public Long submitReview(Long productId, Long customerId, int rating, String title, String body) {
        if (rating < 1 || rating > 5) {
            throw new InvalidAmountException("Rating must be between 1 and 5");
        }
        logger.info("Submitting review product {} customer {} rating {}", productId, customerId, rating);
        return reviewProcedureDao.submitReview(productId, customerId, rating, title, body);
    }

    public List<ReviewView> getReviewsByProduct(Long productId, int limit, int offset) {
        return reviewProcedureDao.getReviewsByProduct(productId, limit, offset);
    }
}
