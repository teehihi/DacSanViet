package com.dacsanviet.controller;

import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dto.CreateOrderRequest;
import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.security.UserPrincipal;
import com.dacsanviet.service.CartService;
import com.dacsanviet.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Checkout controller for order processing
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * Show checkout page - supports both authenticated users and guests
     * Cart is loaded from localStorage on client side (Yame behavior)
     */
    @GetMapping
    public String showCheckout(Model model, Authentication authentication) {
        try {
            // Always use guest checkout flow since cart is in localStorage
            // Cart will be loaded from localStorage on client side
            CartDao cart = new CartDao();
            
            // Check if user is authenticated for display purposes
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("userId", userPrincipal.getId());
            } else {
                model.addAttribute("isGuest", true);
            }
            
            model.addAttribute("cart", cart);
            model.addAttribute("orderRequest", new CreateOrderRequest());
            model.addAttribute("pageTitle", "Thanh Toán");
            
            return "checkout/simple-checkout";
            
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/cart";
        }
    }
    
    /**
     * Process checkout - supports both authenticated users and guests
     */
    @PostMapping("/process")
    public String processCheckout(
            @Valid @ModelAttribute("orderRequest") CreateOrderRequest orderRequest,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate form
            if (bindingResult.hasErrors()) {
                model.addAttribute("orderRequest", orderRequest);
                model.addAttribute("pageTitle", "Thanh Toán");
                return "checkout/simple-checkout";
            }
            
            // Check if user is authenticated
            Long userId = null;
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                userId = userPrincipal.getId();
            }
            
            // Set user ID (null for guest orders)
            orderRequest.setUserId(userId);
            
            // Create order
            OrderDao order = orderService.createOrderFromCart(orderRequest);
            
            // Clear cart after successful order (for authenticated users)
            if (userId != null) {
                cartService.clearCart(userId);
            }
            
            // Set appropriate success message based on payment method
            String successMessage;
            if ("COD".equals(orderRequest.getPaymentMethod())) {
                successMessage = "Đặt hàng COD thành công! Mã đơn hàng: " + order.getOrderNumber() + 
                               ". Đơn hàng đang được xử lý và sẽ được giao đến bạn sớm nhất.";
            } else {
                successMessage = "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderNumber();
            }
            
            redirectAttributes.addFlashAttribute("message", successMessage);
            
            // For guest orders, redirect to a guest-friendly success page
            if (userId == null) {
                redirectAttributes.addFlashAttribute("orderNumber", order.getOrderNumber());
                return "redirect:/checkout/success?orderNumber=" + order.getOrderNumber();
            }
            
            return "redirect:/orders/" + order.getId();
            
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
            model.addAttribute("pageTitle", "Thanh Toán");
            return "checkout/simple-checkout";
        }
    }
    
    /**
     * Checkout success page
     */
    @GetMapping("/success")
    public String checkoutSuccess(@RequestParam(required = false) Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("pageTitle", "Đặt Hàng Thành Công");
        return "checkout/success";
    }
}