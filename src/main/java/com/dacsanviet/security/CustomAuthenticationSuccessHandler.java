package com.dacsanviet.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication success handler
 * YAME BEHAVIOR: Cart is NOT synced with server
 * Cart only exists in localStorage (browser-specific)
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        // Yame behavior: Cart is NOT synced to server
        // Each browser has its own independent cart (localStorage only)
        // Different browsers = different carts, even with same account
        
        // Redirect to original URL or home page
        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/");
        }
    }
}
