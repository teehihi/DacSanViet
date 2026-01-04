package com.dacsanviet.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dacsanviet.dao.OrderDao;
import com.dacsanviet.service.EmailService;

/**
 * Controller for testing email functionality
 * REMOVE THIS IN PRODUCTION!
 */
@RestController
@RequestMapping("/api/test")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/email")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> testEmail(@RequestParam(defaultValue = "test@example.com") String email,
                                       @RequestParam(defaultValue = "payment") String type) {
        try {
            // Create test order
            OrderDao order = new OrderDao();
            order.setId(999L);
            order.setOrderNumber("TEST999");
            order.setCustomerName("Test User");
            order.setCustomerEmail(email);
            order.setTotalAmount(new BigDecimal("500000"));
            order.setPaymentMethod("MOMO");
            order.setOrderDate(LocalDateTime.now());
            order.setShippingCarrier("Giao Hàng Nhanh");
            order.setTrackingNumber("GHN123456789");

            String message = "";
            
            switch (type.toLowerCase()) {
                case "payment":
                    emailService.sendPaymentConfirmationEmail(order);
                    message = "Payment confirmation email sent to " + email;
                    break;
                case "shipping":
                    emailService.sendShippingNotificationEmail(order);
                    message = "Shipping notification email sent to " + email;
                    break;
                case "completion":
                    emailService.sendOrderCompletionEmail(order);
                    message = "Order completion email sent to " + email;
                    break;
                case "failure":
                    String retryLink = "http://localhost:8082/checkout/retry/TEST999";
                    emailService.sendPaymentFailureEmail(order, retryLink);
                    message = "Payment failure email sent to " + email;
                    break;
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid type. Use: payment, shipping, completion, failure"));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", message,
                "email", email,
                "type", type
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "email", email,
                "type", type
            ));
        }
    }

    @GetMapping("/email/all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> testAllEmails(@RequestParam(defaultValue = "test@example.com") String email) {
        try {
            // Create test order
            OrderDao order = new OrderDao();
            order.setId(999L);
            order.setOrderNumber("TEST999");
            order.setCustomerName("Test User");
            order.setCustomerEmail(email);
            order.setTotalAmount(new BigDecimal("500000"));
            order.setPaymentMethod("MOMO");
            order.setOrderDate(LocalDateTime.now());
            order.setShippingCarrier("Giao Hàng Nhanh");
            order.setTrackingNumber("GHN123456789");

            // Send all types of emails
            emailService.sendPaymentConfirmationEmail(order);
            Thread.sleep(1000);
            
            emailService.sendShippingNotificationEmail(order);
            Thread.sleep(1000);
            
            emailService.sendOrderCompletionEmail(order);
            Thread.sleep(1000);
            
            String retryLink = "http://localhost:8082/checkout/retry/TEST999";
            emailService.sendPaymentFailureEmail(order, retryLink);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All 4 test emails sent successfully to " + email,
                "email", email,
                "types", new String[]{"payment", "shipping", "completion", "failure"}
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "email", email
            ));
        }
    }
}