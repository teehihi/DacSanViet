package com.dacsanviet.dao;

import com.dacsanviet.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User DAO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDao {
    
    private Long id;
    private String username;
    private String password; // For create/update operations only
    private String email;
    private String fullName;
    private String phoneNumber;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Analytics fields
    private Long orderCount;
    private Long totalOrders;
    private BigDecimal totalSpent;
    
    // Constructor for basic user info
    public UserDao(Long id, String username, String email, String fullName, 
                   String phoneNumber, Role role, Boolean isActive, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}