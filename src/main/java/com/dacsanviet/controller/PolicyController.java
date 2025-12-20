package com.dacsanviet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for policy pages
 */
@Controller
public class PolicyController {
    
    @GetMapping("/shipping-policy")
    public String shippingPolicy(Model model) {
        model.addAttribute("pageTitle", "Chính Sách Vận Chuyển");
        return "shipping-policy";
    }
}
