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
     * Works for both authenticated users and guests
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(@Valid @RequestBody AddToCartRequest request, 
                                      Authentication authentication,
                                      HttpSession session) {
        try {
            CartDao cart;
            
            if (authentication != null && authentication.isAuthenticated()) {
                // Logged in user - save to database
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                cart = cartService.addToCart(userPrincipal.getId(), request);
            } else {
                // Guest user - save to session
                cart = sessionCartService.addToSessionCart(session, request);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã thêm sản phẩm vào giỏ hàng",
                "cartItemCount", cart.getTotalItems(),
                "cartTotal", cart.getTotalAmount()
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
     */
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(@Valid @RequestBody UpdateCartItemRequest request,
                                           Authentication authentication,
                                           HttpSession session) {
        try {
            CartDao cart;
            
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                cart = cartService.updateCartItem(userPrincipal.getId(), request);
            } else {
                cart = sessionCartService.updateSessionCartItem(session, request.getProductId(), request.getQuantity());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã cập nhật giỏ hàng",
                "cartItemCount", cart.getTotalItems(),
                "cartTotal", cart.getTotalAmount()
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
     */
    @DeleteMapping("/remove/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId,
                                           Authentication authentication,
                                           HttpSession session) {
        try {
            CartDao cart;
            
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                cart = cartService.removeFromCart(userPrincipal.getId(), productId);
            } else {
                cart = sessionCartService.removeFromSessionCart(session, productId);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa sản phẩm khỏi giỏ hàng",
                "cartItemCount", cart.getTotalItems(),
                "cartTotal", cart.getTotalAmount()
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
     */
    @DeleteMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(Authentication authentication, HttpSession session) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                cartService.clearCart(userPrincipal.getId());
            } else {
                sessionCartService.clearSessionCart(session);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa tất cả sản phẩm khỏi giỏ hàng",
                "cartItemCount", 0,
                "cartTotal", 0
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
     */
    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<?> getCartItems(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                CartDao cart = cartService.getCart(userPrincipal.getId());
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "items", cart.getItems(),
                    "total", cart.getTotalAmount()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "items", new java.util.ArrayList<>(),
                    "total", 0
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}