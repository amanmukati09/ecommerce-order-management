package com.ecommerce.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * Review entity - managed by Hibernate for reads/listing. Submission goes
 * through a stored procedure (sp_submit_review) which also recalculates the
 * product rating.
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "verified_purchase", nullable = false)
    private Boolean verifiedPurchase = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    public enum ReviewStatus { PUBLISHED, PENDING, REJECTED }

    public Review() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Boolean getVerifiedPurchase() { return verifiedPurchase; }
    public void setVerifiedPurchase(Boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
