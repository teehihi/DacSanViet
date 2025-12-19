package com.dacsanviet.dao;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Access Object for Product
 * Using Lombok @Data for automatic getters/setters/toString/equals/hashCode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDao {
    
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    private String name;
    
    private String shortDescription;
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
    
    private Boolean isActive;
    private Boolean isFeatured;
    
    @Min(value = 0, message = "Weight cannot be negative")
    private Integer weightGrams;
    
    @Size(max = 100, message = "Origin must not exceed 100 characters")
    private String origin;
    
    private String story;
    private String storyImageUrl;
    
    private CategoryDao category;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Analytics fields
    private Long totalSold;
    private BigDecimal totalRevenue;
    
    // Custom constructor for common use case
    public ProductDao(Long id, String name, String description, BigDecimal price, 
                     Integer stockQuantity, String imageUrl, Boolean isActive, 
                     Boolean isFeatured, Integer weightGrams, String origin, 
                     CategoryDao category, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
        this.isFeatured = isFeatured;
        this.weightGrams = weightGrams;
        this.origin = origin;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Helper methods
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
    
    public boolean isAvailable() {
        return isActive != null && isActive && isInStock();
    }
}
