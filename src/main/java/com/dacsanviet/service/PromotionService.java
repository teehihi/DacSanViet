package com.dacsanviet.service;

import com.dacsanviet.model.Promotion;
import com.dacsanviet.repository.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PromotionService {
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    /**
     * Validate and get promotion by code
     */
    public Optional<Promotion> validatePromotion(String code, BigDecimal orderTotal) {
        Optional<Promotion> promotionOpt = promotionRepository.findValidPromotionByCode(
            code.toUpperCase(), 
            LocalDateTime.now()
        );
        
        if (promotionOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Promotion promotion = promotionOpt.get();
        
        // Check minimum order value
        if (orderTotal.compareTo(promotion.getMinOrderValue()) < 0) {
            return Optional.empty();
        }
        
        return Optional.of(promotion);
    }
    
    /**
     * Calculate discount amount for a promotion
     */
    public BigDecimal calculateDiscount(Promotion promotion, BigDecimal orderTotal) {
        return promotion.calculateDiscount(orderTotal);
    }
    
    /**
     * Apply promotion and increment usage count
     */
    @Transactional
    public void applyPromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
            .orElseThrow(() -> new RuntimeException("Promotion not found"));
        
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);
    }
    
    /**
     * Get promotion by code (without validation)
     */
    public Optional<Promotion> getPromotionByCode(String code) {
        return promotionRepository.findByCode(code.toUpperCase());
    }
}
