package com.dacsanviet.controller;

import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Controller for cart-related operations
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * Get product weights for cart items
     * Used for shipping fee calculation
     */
    @PostMapping("/weights")
    public ResponseEntity<Map<String, Object>> getProductWeights(@RequestBody List<Long> productIds) {
        try {
            Map<Long, Integer> weights = new HashMap<>();
            
            for (Long productId : productIds) {
                ProductDao product = productService.getProductById(productId);
                if (product != null) {
                    // Get weight in grams, default to 500g if not set
                    int weight = product.getWeightGrams() != null ? product.getWeightGrams() : 500;
                    weights.put(productId, weight);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("weights", weights);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
