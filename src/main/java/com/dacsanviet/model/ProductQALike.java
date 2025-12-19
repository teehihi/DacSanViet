package com.dacsanviet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for tracking Q&A likes
 */
@Entity
@Table(name = "product_qa_likes")
public class ProductQALike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "qa_id", nullable = false)
    private Long qaId;
    
    @Column(name = "user_identifier", nullable = false)
    private String userIdentifier; // Email or session ID
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getQaId() {
        return qaId;
    }
    
    public void setQaId(Long qaId) {
        this.qaId = qaId;
    }
    
    public String getUserIdentifier() {
        return userIdentifier;
    }
    
    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
