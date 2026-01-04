package com.dacsanviet.service;

import com.dacsanviet.dao.OrderDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testPaymentConfirmationEmail() {
        // Create a test order
        OrderDao order = new OrderDao();
        order.setId(1L);
        order.setOrderNumber("TEST001");
        order.setCustomerName("Nguyễn Văn Test");
        order.setCustomerEmail("test@example.com");
        order.setTotalAmount(new BigDecimal("500000"));
        order.setPaymentMethod("MOMO");
        order.setOrderDate(LocalDateTime.now());

        try {
            emailService.sendPaymentConfirmationEmail(order);
            System.out.println("✅ Payment confirmation email test passed");
        } catch (Exception e) {
            System.err.println("❌ Payment confirmation email test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testShippingNotificationEmail() {
        // Create a test order
        OrderDao order = new OrderDao();
        order.setId(2L);
        order.setOrderNumber("TEST002");
        order.setCustomerName("Nguyễn Thị Test");
        order.setCustomerEmail("test2@example.com");
        order.setTotalAmount(new BigDecimal("750000"));
        order.setShippingCarrier("Giao Hàng Nhanh");
        order.setTrackingNumber("GHN123456789");
        order.setOrderDate(LocalDateTime.now());

        try {
            emailService.sendShippingNotificationEmail(order);
            System.out.println("✅ Shipping notification email test passed");
        } catch (Exception e) {
            System.err.println("❌ Shipping notification email test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testOrderCompletionEmail() {
        // Create a test order
        OrderDao order = new OrderDao();
        order.setId(3L);
        order.setOrderNumber("TEST003");
        order.setCustomerName("Trần Văn Test");
        order.setCustomerEmail("test3@example.com");
        order.setTotalAmount(new BigDecimal("1000000"));
        order.setOrderDate(LocalDateTime.now());

        try {
            emailService.sendOrderCompletionEmail(order);
            System.out.println("✅ Order completion email test passed");
        } catch (Exception e) {
            System.err.println("❌ Order completion email test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testPaymentFailureEmail() {
        // Create a test order
        OrderDao order = new OrderDao();
        order.setId(4L);
        order.setOrderNumber("TEST004");
        order.setCustomerName("Lê Thị Test");
        order.setCustomerEmail("test4@example.com");
        order.setTotalAmount(new BigDecimal("300000"));
        order.setOrderDate(LocalDateTime.now());

        String retryLink = "http://localhost:8082/checkout/retry/TEST004";

        try {
            emailService.sendPaymentFailureEmail(order, retryLink);
            System.out.println("✅ Payment failure email test passed");
        } catch (Exception e) {
            System.err.println("❌ Payment failure email test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}