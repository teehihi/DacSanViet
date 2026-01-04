package com.dacsanviet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for return and refund policy pages
 */
@Controller
@RequestMapping("/return-policy")
public class ReturnPolicyController {

    @GetMapping
    public String returnPolicy(Model model) {
        model.addAttribute("pageTitle", "Chính Sách Đổi Trả - Đặc Sản Việt");
        return "return-policy/index";
    }

    @GetMapping("/process")
    public String returnProcess(Model model) {
        model.addAttribute("pageTitle", "Quy Trình Đổi Trả - Đặc Sản Việt");
        return "return-policy/process";
    }

    @GetMapping("/refund")
    public String refundPolicy(Model model) {
        model.addAttribute("pageTitle", "Chính Sách Hoàn Tiền - Đặc Sản Việt");
        return "return-policy/refund";
    }
}