package com.ecommerce.model;

import java.util.Date;

/**
 * ReviewView - read model from stored procedure result sets.
 */
public class ReviewView {

    private Long reviewId;
    private Long productId;
    private Long customerId;
    private Integer rating;
    private String title;
    private String body;
    private Boolean verifiedPurchase;
    private String status;
    private Date createdAt;
    private String firstName;
    private String lastName;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
