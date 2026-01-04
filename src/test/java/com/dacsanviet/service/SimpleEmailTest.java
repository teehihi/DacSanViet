package com.dacsanviet.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.dacsanviet.dao.OrderDao;

public class SimpleEmailTest {
    
    public static void main(String[] args) {
        try {
            // Create JavaMailSender manually
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("smtp.gmail.com");
            mailSender.setPort(587);
            mailSender.setUsername("dacsanviethotro@gmail.com");
            mailSender.setPassword("pyqfmdfifkfqeacg");
            
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.debug", "true");
            
            // Create EmailService manually
            EmailService emailService = new EmailService();
            
            // Use reflection to set private fields
            java.lang.reflect.Field mailSenderField = EmailService.class.getDeclaredField("mailSender");
            mailSenderField.setAccessible(true);
            mailSenderField.set(emailService, mailSender);
            
            java.lang.reflect.Field fromEmailField = EmailService.class.getDeclaredField("fromEmail");
            fromEmailField.setAccessible(true);
            fromEmailField.set(emailService, "dacsanviethotro@gmail.com");
            
            java.lang.reflect.Field frontendUrlField = EmailService.class.getDeclaredField("frontendUrl");
            frontendUrlField.setAccessible(true);
            frontendUrlField.set(emailService, "http://localhost:8082");
            
            // Create test order
            OrderDao order = new OrderDao();
            order.setId(999L);
            order.setOrderNumber("TEST999");
            order.setCustomerName("Test User");
            order.setCustomerEmail("test@example.com"); // Change this to your email for testing
            order.setTotalAmount(new BigDecimal("500000"));
            order.setPaymentMethod("MOMO");
            order.setOrderDate(LocalDateTime.now());
            order.setShippingCarrier("Giao Hàng Nhanh");
            order.setTrackingNumber("GHN123456789");
            
            System.out.println("🧪 Testing Email Service...");
            System.out.println("📧 Sending to: " + order.getCustomerEmail());
            
            // Test payment confirmation email
            System.out.println("\n1️⃣ Testing Payment Confirmation Email...");
            emailService.sendPaymentConfirmationEmail(order);
            System.out.println("✅ Payment confirmation email sent successfully!");
            
            Thread.sleep(2000); // Wait 2 seconds between emails
            
            // Test shipping notification email
            System.out.println("\n2️⃣ Testing Shipping Notification Email...");
            emailService.sendShippingNotificationEmail(order);
            System.out.println("✅ Shipping notification email sent successfully!");
            
            Thread.sleep(2000);
            
            // Test order completion email
            System.out.println("\n3️⃣ Testing Order Completion Email...");
            emailService.sendOrderCompletionEmail(order);
            System.out.println("✅ Order completion email sent successfully!");
            
            Thread.sleep(2000);
            
            // Test payment failure email
            System.out.println("\n4️⃣ Testing Payment Failure Email...");
            String retryLink = "http://localhost:8082/checkout/retry/TEST999";
            emailService.sendPaymentFailureEmail(order, retryLink);
            System.out.println("✅ Payment failure email sent successfully!");
            
            System.out.println("\n🎉 All email tests completed successfully!");
            System.out.println("📬 Check your email inbox: " + order.getCustomerEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Email test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}