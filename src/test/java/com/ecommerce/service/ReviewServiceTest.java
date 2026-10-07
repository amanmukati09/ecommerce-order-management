package com.ecommerce.service;

import com.ecommerce.dao.jdbc.ReviewProcedureDao;
import com.ecommerce.exception.InvalidAmountException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReviewService using Mockito.
 */
public class ReviewServiceTest {

    @Mock
    private ReviewProcedureDao reviewProcedureDao;

    @InjectMocks
    private ReviewService reviewService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubmitReview() {
        when(reviewProcedureDao.submitReview(1L, 2L, 5, "Great", "Love it")).thenReturn(50L);
        Long id = reviewService.submitReview(1L, 2L, 5, "Great", "Love it");
        assertEquals(Long.valueOf(50L), id);
    }

    @Test(expected = InvalidAmountException.class)
    public void testSubmitReviewRatingTooLow() {
        reviewService.submitReview(1L, 2L, 0, "Bad", "x");
    }

    @Test(expected = InvalidAmountException.class)
    public void testSubmitReviewRatingTooHigh() {
        reviewService.submitReview(1L, 2L, 6, "Bad", "x");
    }

    @Test
    public void testSubmitReviewBoundaryRatings() {
        when(reviewProcedureDao.submitReview(eq(1L), eq(2L), eq(1), any(), any())).thenReturn(1L);
        when(reviewProcedureDao.submitReview(eq(1L), eq(2L), eq(5), any(), any())).thenReturn(2L);
        assertEquals(Long.valueOf(1L), reviewService.submitReview(1L, 2L, 1, "t", "b"));
        assertEquals(Long.valueOf(2L), reviewService.submitReview(1L, 2L, 5, "t", "b"));
    }
}
