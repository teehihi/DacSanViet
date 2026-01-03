package com.dacsanviet.controller;

import com.dacsanviet.dto.AddToCartRequest;
import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dto.UpdateCartItemRequest;
import com.dacsanviet.security.UserPrincipal;
import com.dacsanviet.service.CartService;
import com.dacsanviet.service.SessionCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * Cart controller for shopping cart functionality
 * Supports both authenticated users (database cart) and guests (session cart)
 */
@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private SessionCartService sessionCartService;
    
    @GetMapping
    public String viewCart(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Giỏ Hàng");
            
            // YAME BEHAVIOR: Always use localStorage cart (guest-cart.html)
            // Both guests and logged-in users see the same cart page
            // Cart is loaded from localStorage on client-side
            return "cart/guest-cart";
            
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải giỏ hàng: " + e.getMessage());
            return "cart/guest-cart";
        }
    }
    
    /**
     * Add product to cart (AJAX)
     * YAME BEHAVIOR: All users use localStorage only
     * This endpoint is kept for compatibility but returns success without server-side storage
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request, 
                                      Authentication authentication,
                                      HttpSession session) {
        try {
            // YAME BEHAVIOR: Cart is localStorage-only for all users
            // Return success response for client-side cart management
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm sản phẩm vào giỏ hàng",
                "cartItemCount", 1, // Client will update this
                "cartTotal", 0,     // Client will calculate this
                "localStorage", true // Indicate this is localStorage-only
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Update cart item quantity (AJAX)
     * YAME BEHAVIOR: All users use localStorage only
     */
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request,
                                           Authentication authentication,
                                           HttpSession session) {
        try {
            // YAME BEHAVIOR: Cart is localStorage-only for all users
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật giỏ hàng",
                "cartItemCount", 1, // Client will update this
                "cartTotal", 0,     // Client will calculate this
                "localStorage", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Remove item from cart (AJAX)
     * YAME BEHAVIOR: All users use localStorage only
     */
    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId,
                                           Authentication authentication,
                                           HttpSession session) {
        try {
            // YAME BEHAVIOR: Cart is localStorage-only for all users
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa sản phẩm khỏi giỏ hàng",
                "cartItemCount", 0, // Client will update this
                "cartTotal", 0,     // Client will calculate this
                "localStorage", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Clear entire cart (AJAX)
     * YAME BEHAVIOR: All users use localStorage only
     */
    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(Authentication authentication, HttpSession session) {
        try {
            // YAME BEHAVIOR: Cart is localStorage-only for all users
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa tất cả sản phẩm khỏi giỏ hàng",
                "cartItemCount", 0,
                "cartTotal", 0,
                "localStorage", true
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get cart items (for syncing with localStorage)
     * YAME BEHAVIOR: Always return empty - cart is localStorage-only
     */
    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<?> getCartItems(Authentication authentication) {
        try {
            // YAME BEHAVIOR: Cart is localStorage-only for all users
            // Always return empty cart - client should use localStorage
            return ResponseEntity.ok(Map.of(
                "success", true,
                "items", new java.util.ArrayList<>(),
                "total", 0,
                "localStorage", true,
                "message", "Cart is localStorage-only (Yame behavior)"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}