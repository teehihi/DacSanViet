package com.dacsanviet.controller;

import com.dacsanviet.model.Address;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.AddressRepository;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.security.UserPrincipal;
import com.dacsanviet.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * User controller for user profile and account management
 */
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @GetMapping({"/profile", "/user/profile"})
    public String profile(Model model, Authentication authentication) {
        try {
            model.addAttribute("pageTitle", "Thông Tin Cá Nhân");
            
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                Long userId = userPrincipal.getId();
                
                // Load user from database
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                // Load user addresses
                List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
                
                model.addAttribute("user", user);
                model.addAttribute("addresses", addresses);
                model.addAttribute("addressCount", addresses.size());
                
                // Check if user has default address
                boolean hasDefaultAddress = addressRepository.existsByUserIdAndIsDefaultTrue(userId);
                model.addAttribute("hasDefaultAddress", hasDefaultAddress);
                
            } else {
                return "redirect:/login";
            }
            
            return "user/simple-profile";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin cá nhân: " + e.getMessage());
            return "user/simple-profile";
        }
    }
}