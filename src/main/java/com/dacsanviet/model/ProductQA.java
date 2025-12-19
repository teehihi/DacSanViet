package com.dacsanviet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for Product Q&A
 */
@Entity
@Table(name = "product_qa")
public class ProductQA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;
    
    @Column(name = "user_email", length = 255)
    private String userEmail;
    
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;
    
    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;
    
    @Column(name = "answered_by", length = 100)
    private String answeredBy;
    
    @Column(name = "is_answered")
    private Boolean isAnswered = false;
    
    @Column(name = "is_visible")
    private Boolean isVisible = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "likes_count")
    private Integer likesCount = 0;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getAnsweredBy() {
        return answeredBy;
    }
    
    public void setAnsweredBy(String answeredBy) {
        this.answeredBy = answeredBy;
    }
    
    public Boolean getIsAnswered() {
        return isAnswered;
    }
    
    public void setIsAnswered(Boolean isAnswered) {
        this.isAnswered = isAnswered;
    }
    
    public Boolean getIsVisible() {
        return isVisible;
    }
    
    public void setIsVisible(Boolean isVisible) {
        this.isVisible = isVisible;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
    
    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Integer getLikesCount() {
        return likesCount;
    }
    
    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }
}
