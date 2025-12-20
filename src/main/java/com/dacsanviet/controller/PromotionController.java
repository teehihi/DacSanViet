package com.dacsanviet.controller;

import com.dacsanviet.model.Promotion;
import com.dacsanviet.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    
    @Autowired
    private PromotionService promotionService;
    
    /**
     * Validate promotion code
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validatePromotion(
            @RequestParam String code,
            @RequestParam BigDecimal orderTotal) {
        
        Map<String, Object> response = new HashMap<>();
        
        Optional<Promotion> promotionOpt = promotionService.validatePromotion(code, orderTotal);
        
        if (promotionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mã giảm giá không hợp lệ hoặc không đủ điều kiện");
            return ResponseEntity.ok(response);
        }
        
        Promotion promotion = promotionOpt.get();
        BigDecimal discountAmount = promotionService.calculateDiscount(promotion, orderTotal);
        
        response.put("success", true);
        response.put("message", "Áp dụng mã giảm giá thành công");
        response.put("promotion", Map.of(
            "id", promotion.getId(),
            "code", promotion.getCode(),
            "description", promotion.getDescription(),
            "discountType", promotion.getDiscountType().toString(),
            "discountAmount", discountAmount,
            "minOrderValue", promotion.getMinOrderValue()
        ));
        response.put("discountAmount", discountAmount);
        response.put("newTotal", orderTotal.subtract(discountAmount));
        
        return ResponseEntity.ok(response);
    }
}
