package com.specialtyfood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Password reset token entity for secure password reset functionality
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_token", columnList = "token"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 255)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @lombok.ToString.Exclude
    private User user;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "used", nullable = false)
    private Boolean used = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Default initializer block
    {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public PasswordResetToken(String token, User user, LocalDateTime expiryDate) {
        // Initializer block runs
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }
    
    // Helper methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }
    
    public boolean isValid() {
        return !this.used && !this.isExpired();
    }
}