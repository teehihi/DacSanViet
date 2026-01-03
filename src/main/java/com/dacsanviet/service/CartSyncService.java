package com.dacsanviet.service;

import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dao.CartItemDao;
import com.dacsanviet.dao.ProductDao;
import com.dacsanviet.dao.CategoryDao;
import com.dacsanviet.model.CartItem;
import com.dacsanviet.model.Product;
import com.dacsanviet.repository.CartItemRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for synchronizing database cart items to localStorage format
 * for authenticated users during checkout process
 */
@Service
@Transactional(readOnly = true)
public class CartSyncService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartSyncService.class);
    
    private final CartItemRepository cartItemRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public CartSyncService(CartItemRepository cartItemRepository, ObjectMapper objectMapper) {
        this.cartItemRepository = cartItemRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Load database cart items to localStorage format for authenticated user
     * @param userId The authenticated user ID
     * @return CartDao representing the synchronized cart
     * @throws CartSyncException if synchronization fails
     */
    public CartDao syncDatabaseCartToLocalStorage(Long userId) throws CartSyncException {
        try {
            logger.debug("Starting cart synchronization for user: {}", userId);
            
            if (userId == null) {
                throw new CartSyncException("User ID cannot be null");
            }
            
            // Load database cart items
            List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByAddedDateDesc(userId);
            logger.debug("Found {} cart items for user: {}", cartItems.size(), userId);
            
            // Convert to CartDao format
            CartDao cartDao = convertToCartDao(userId, cartItems);
            
            logger.info("Successfully synchronized cart for user: {} with {} items", userId, cartDao.getTotalItems());
            return cartDao;
            
        } catch (Exception e) {
            logger.error("Failed to sync cart for user {}: {}", userId, e.getMessage(), e);
            throw new CartSyncException("Failed to synchronize cart for user: " + userId, e);
        }
    }
    
    /**
     * Check if localStorage cart needs synchronization
     * @param userId The authenticated user ID
     * @return true if sync is needed (database has items)
     */
    public boolean needsCartSynchronization(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            Long itemCount = cartItemRepository.countByUserId(userId);
            boolean needsSync = itemCount > 0;
            logger.debug("User {} has {} database cart items, needs sync: {}", userId, itemCount, needsSync);
            return needsSync;
        } catch (Exception e) {
            logger.error("Error checking cart sync need for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Convert database CartItem list to localStorage format JSON string
     * @param cartItems Database cart items
     * @return JSON string for localStorage
     * @throws CartSyncException if conversion fails
     */
    public String convertCartItemsToLocalStorageFormat(List<CartItem> cartItems) throws CartSyncException {
        try {
            if (cartItems == null || cartItems.isEmpty()) {
                return createEmptyCartJson();
            }
            
            Map<String, Object> localStorageCart = new HashMap<>();
            
            // Convert cart items to localStorage format
            List<Map<String, Object>> items = cartItems.stream()
                    .map(this::convertCartItemToLocalStorageFormat)
                    .collect(Collectors.toList());
            
            localStorageCart.put("items", items);
            localStorageCart.put("total", calculateTotal(cartItems));
            localStorageCart.put("lastModified", System.currentTimeMillis());
            
            String json = objectMapper.writeValueAsString(localStorageCart);
            logger.debug("Converted {} cart items to localStorage format", cartItems.size());
            return json;
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert cart items to JSON: {}", e.getMessage());
            throw new CartSyncException("Failed to convert cart items to localStorage format", e);
        }
    }
    
    /**
     * Ensure cart is available for checkout by synchronizing if needed
     * @param userId The authenticated user ID
     * @return true if cart is available (either already in localStorage or successfully synced)
     */
    public boolean ensureCartAvailableForCheckout(Long userId) {
        try {
            if (needsCartSynchronization(userId)) {
                syncDatabaseCartToLocalStorage(userId);
                return true;
            }
            return true; // Cart is already available or user has no items
        } catch (CartSyncException e) {
            logger.error("Failed to ensure cart availability for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
    
    // Helper methods
    
    private CartDao convertToCartDao(Long userId, List<CartItem> cartItems) {
        CartDao cartDao = new CartDao(userId);
        
        for (CartItem item : cartItems) {
            CartItemDao cartItemDao = convertToCartItemDao(item);
            cartDao.addItem(cartItemDao);
        }
        
        return cartDao;
    }
    
    private CartItemDao convertToCartItemDao(CartItem cartItem) {
        ProductDao productDao = convertToProductDao(cartItem.getProduct());
        
        return new CartItemDao(
                cartItem.getId(),
                productDao,
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                cartItem.getAddedDate(),
                cartItem.getUpdatedAt()
        );
    }
    
    private ProductDao convertToProductDao(Product product) {
        CategoryDao categoryDao = null;
        if (product.getCategory() != null) {
            categoryDao = new CategoryDao(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            );
            categoryDao.setDescription(product.getCategory().getDescription());
        }
        
        return new ProductDao(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getWeightGrams(),
                product.getOrigin(),
                categoryDao,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
    
    private Map<String, Object> convertCartItemToLocalStorageFormat(CartItem cartItem) {
        Map<String, Object> item = new HashMap<>();
        Product product = cartItem.getProduct();
        
        item.put("productId", product.getId());
        item.put("productName", product.getName());
        item.put("price", cartItem.getUnitPrice());
        item.put("quantity", cartItem.getQuantity());
        item.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");
        
        return item;
    }
    
    private double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                .sum();
    }
    
    private String createEmptyCartJson() throws CartSyncException {
        try {
            Map<String, Object> emptyCart = new HashMap<>();
            emptyCart.put("items", List.of());
            emptyCart.put("total", 0.0);
            emptyCart.put("lastModified", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(emptyCart);
        } catch (JsonProcessingException e) {
            throw new CartSyncException("Failed to create empty cart JSON", e);
        }
    }
    
    /**
     * Custom exception for cart synchronization errors
     */
    public static class CartSyncException extends Exception {
        public CartSyncException(String message) {
            super(message);
        }
        
        public CartSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}