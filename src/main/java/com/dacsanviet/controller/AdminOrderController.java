package com.dacsanviet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.model.Order;
import com.dacsanviet.repository.OrderRepository;
import com.dacsanviet.service.OrderService;

/**
 * Admin Order Controller for order detail pages
 */
@Controller
@RequestMapping("/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    /**
     * Show order detail page
     */
    @GetMapping("/{id}")
    @Transactional
    public String showOrderDetail(@PathVariable Long id, Model model) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Force load orderItems to avoid LazyInitializationException
            if (order.getOrderItems() != null) {
                order.getOrderItems().size();
            }

            OrderDao orderDao = orderService.convertToDao(order);
            model.addAttribute("order", orderDao);

            return "admin/orders/detail";
        } catch (Exception e) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/admin/orders";
        }
    }
}
