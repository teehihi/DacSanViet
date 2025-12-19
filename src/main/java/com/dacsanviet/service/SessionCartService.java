package com.dacsanviet.service;

import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dao.CartItemDao;
import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.dao.CategoryDao;
import com.dacsanviet.dto.AddToCartRequest;
import com.dacsanviet.model.Product;
import com.dacsanviet.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing session-based shopping cart (for guests)
 */
@Service
public class SessionCartService {
    
    private static final String CART_SESSION_KEY = "GUEST_CART";
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Add item to session cart
     */
    @Transactional(readOnly = true)
    public CartDao addToSessionCart(HttpSession session, AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Validate product
        if (!product.getIsActive()) {
            throw new RuntimeException("Sản phẩm không còn bán");
        }
        
        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Không đủ hàng trong kho. Còn lại: " + product.getStockQuantity());
        }
        
        // Get or create cart from session
        Map<Long, CartItemData> cart = getCartFromSession(session);
        
        // Add or update item
        CartItemData existingItem = cart.get(product.getId());
        if (existingItem != null) {
            int newQuantity = existingItem.quantity + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Không đủ hàng trong kho. Còn lại: " + product.getStockQuantity());
            }
            existingItem.quantity = newQuantity;
        } else {
            cart.put(product.getId(), new CartItemData(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                request.getQuantity(),
                product.getImageUrl(),
                product.getStockQuantity()
            ));
        }
        
        // Save back to session
        session.setAttribute(CART_SESSION_KEY, cart);
        
        return convertToCartDao(cart);
    }
    
    /**
     * Get session cart
     */
    public CartDao getSessionCart(HttpSession session) {
        Map<Long, CartItemData> cart = getCartFromSession(session);
        return convertToCartDao(cart);
    }
    
    /**
     * Update item quantity in session cart
     */
    public CartDao updateSessionCartItem(HttpSession session, Long productId, Integer quantity) {
        Map<Long, CartItemData> cart = getCartFromSession(session);
        
        CartItemData item = cart.get(productId);
        if (item == null) {
            throw new RuntimeException("Sản phẩm không có trong giỏ hàng");
        }
        
        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            // Validate stock
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Không đủ hàng trong kho. Còn lại: " + product.getStockQuantity());
            }
            
            item.quantity = quantity;
        }
        
        session.setAttribute(CART_SESSION_KEY, cart);
        return convertToCartDao(cart);
    }
    
    /**
     * Remove item from session cart
     */
    public CartDao removeFromSessionCart(HttpSession session, Long productId) {
        Map<Long, CartItemData> cart = getCartFromSession(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
        return convertToCartDao(cart);
    }
    
    /**
     * Clear session cart
     */
    public void clearSessionCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
    
    /**
     * Get cart item count
     */
    public int getSessionCartItemCount(HttpSession session) {
        Map<Long, CartItemData> cart = getCartFromSession(session);
        return cart.values().stream().mapToInt(item -> item.quantity).sum();
    }
    
    /**
     * Merge session cart to user cart (when user logs in)
     */
    public void mergeSessionCartToUserCart(HttpSession session, Long userId, CartService cartService) {
        Map<Long, CartItemData> sessionCart = getCartFromSession(session);
        
        for (CartItemData item : sessionCart.values()) {
            try {
                AddToCartRequest request = new AddToCartRequest();
                request.setProductId(item.productId);
                request.setQuantity(item.quantity);
                cartService.addToCart(userId, request);
            } catch (Exception e) {
                // Log error but continue with other items
                System.err.println("Error merging cart item: " + e.getMessage());
            }
        }
        
        // Clear session cart after merge
        clearSessionCart(session);
    }
    
    // Helper methods
    
    @SuppressWarnings("unchecked")
    private Map<Long, CartItemData> getCartFromSession(HttpSession session) {
        Map<Long, CartItemData> cart = (Map<Long, CartItemData>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }
    
    private CartDao convertToCartDao(Map<Long, CartItemData> cart) {
        CartDao cartDao = new CartDao(null); // null userId for guest cart
        
        for (CartItemData item : cart.values()) {
            ProductDao productDao = new ProductDao(
                item.productId,
                item.name,
                item.description,
                item.price,
                item.stockQuantity,
                item.imageUrl,
                true,
                false,
                null,
                null,
                null,
                null,
                null
            );
            
            CartItemDao cartItemDao = new CartItemDao(
                null, // no ID for session cart items
                productDao,
                item.quantity,
                item.price,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            cartDao.addItem(cartItemDao);
        }
        
        return cartDao;
    }
    
    /**
     * Inner class to store cart item data in session
     */
    public static class CartItemData implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        public Long productId;
        public String name;
        public String description;
        public BigDecimal price;
        public Integer quantity;
        public String imageUrl;
        public Integer stockQuantity;
        
        public CartItemData() {}
        
        public CartItemData(Long productId, String name, String description, 
                           BigDecimal price, Integer quantity, String imageUrl, Integer stockQuantity) {
            this.productId = productId;
            this.name = name;
            this.description = description;
            this.price = price;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
            this.stockQuantity = stockQuantity;
        }
    }
}
