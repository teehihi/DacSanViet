package com.dacsanviet.service;

import com.dacsanviet.dao.CartDao;
import com.dacsanviet.dao.CartItemDao;
import com.dacsanviet.model.CartItem;
import com.dacsanviet.model.Category;
import com.dacsanviet.model.Product;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.CartItemRepository;
import com.dacsanviet.service.CartSyncService.CartSyncException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.Positive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for CartSyncService
 * Feature: fix-logged-in-checkout
 */
@ExtendWith(MockitoExtension.class)
class CartSyncServiceTest {
    
    @Mock
    private CartItemRepository cartItemRepository;
    
    private CartSyncService cartSyncService;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cartSyncService = new CartSyncService(cartItemRepository, objectMapper);
    }
    
    /**
     * Property 1: Database to localStorage Synchronization
     * For any authenticated user with non-empty database cart and empty localStorage,
     * navigating to checkout should result in localStorage being populated with correctly formatted cart items
     * 
     * Feature: fix-logged-in-checkout, Property 1: Database to localStorage Synchronization
     * Validates: Requirements 1.3, 2.2, 3.1, 3.2
     */
    @Property(tries = 100)
    @Tag("Feature: fix-logged-in-checkout, Property 1: Database to localStorage Synchronization")
    void databaseToLocalStorageSynchronization(
            @ForAll @Positive Long userId,
            @ForAll @NotEmpty List<@From("cartItems") CartItem> cartItems) {
        
        // Given: User has non-empty database cart
        when(cartItemRepository.findByUserIdOrderByAddedDateDesc(userId)).thenReturn(cartItems);
        when(cartItemRepository.countByUserId(userId)).thenReturn((long) cartItems.size());
        
        try {
            // When: Cart synchronization is performed
            CartDao result = cartSyncService.syncDatabaseCartToLocalStorage(userId);
            
            // Then: localStorage should be populated with correctly formatted cart items
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getItems()).hasSize(cartItems.size());
            assertThat(result.getTotalItems()).isEqualTo(cartItems.size());
            
            // Verify each item is correctly formatted
            for (int i = 0; i < cartItems.size(); i++) {
                CartItem originalItem = cartItems.get(i);
                CartItemDao syncedItem = result.getItems().get(i);
                
                assertThat(syncedItem.getProduct().getId()).isEqualTo(originalItem.getProduct().getId());
                assertThat(syncedItem.getProduct().getName()).isEqualTo(originalItem.getProduct().getName());
                assertThat(syncedItem.getQuantity()).isEqualTo(originalItem.getQuantity());
                assertThat(syncedItem.getUnitPrice()).isEqualTo(originalItem.getUnitPrice());
            }
            
            // Verify total calculation is correct
            double expectedTotal = cartItems.stream()
                    .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                    .sum();
            assertThat(result.getTotalAmount().doubleValue()).isCloseTo(expectedTotal, org.assertj.core.data.Offset.offset(0.01));
            
        } catch (CartSyncException e) {
            throw new RuntimeException("Cart synchronization should not fail for valid input", e);
        }
    }
    
    @Provide
    Arbitrary<CartItem> cartItems() {
        return Combinators.combine(
                users(),
                products(),
                Arbitraries.integers().between(1, 10),
                prices()
        ).as((user, product, quantity, price) -> {
            CartItem cartItem = new CartItem(user, product, quantity);
            cartItem.setUnitPrice(price);
            cartItem.setAddedDate(LocalDateTime.now().minusHours(1));
            cartItem.setUpdatedAt(LocalDateTime.now());
            return cartItem;
        });
    }
    
    @Provide
    Arbitrary<User> users() {
        return Arbitraries.longs().between(1L, 1000L).map(id -> {
            User user = new User();
            user.setId(id);
            user.setUsername("user" + id);
            user.setEmail("user" + id + "@example.com");
            return user;
        });
    }
    
    @Provide
    Arbitrary<Product> products() {
        return Combinators.combine(
                Arbitraries.longs().between(1L, 1000L),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50),
                prices(),
                Arbitraries.integers().between(0, 100),
                categories()
        ).as((id, name, price, stock, category) -> {
            Product product = new Product();
            product.setId(id);
            product.setName(name);
            product.setPrice(price);
            product.setStockQuantity(stock);
            product.setIsActive(true);
            product.setImageUrl("/images/product" + id + ".jpg");
            product.setCategory(category);
            product.setCreatedAt(LocalDateTime.now().minusDays(1));
            product.setUpdatedAt(LocalDateTime.now());
            return product;
        });
    }
    
    @Provide
    Arbitrary<Category> categories() {
        return Arbitraries.longs().between(1L, 10L).map(id -> {
            Category category = new Category();
            category.setId(id);
            category.setName("Category " + id);
            category.setDescription("Description for category " + id);
            return category;
        });
    }
    
    @Provide
    Arbitrary<BigDecimal> prices() {
        return Arbitraries.doubles()
                .between(1.0, 1000.0)
                .map(price -> BigDecimal.valueOf(Math.round(price * 100.0) / 100.0));
    }
    
    // Unit tests for edge cases
    
    @Test
    void syncDatabaseCartToLocalStorage_EmptyCart_ReturnsEmptyCartDao() throws CartSyncException {
        // Given
        Long userId = 1L;
        when(cartItemRepository.findByUserIdOrderByAddedDateDesc(userId)).thenReturn(new ArrayList<>());
        
        // When
        CartDao result = cartSyncService.syncDatabaseCartToLocalStorage(userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalItems()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    void needsCartSynchronization_UserWithItems_ReturnsTrue() {
        // Given
        Long userId = 1L;
        when(cartItemRepository.countByUserId(userId)).thenReturn(3L);
        
        // When
        boolean result = cartSyncService.needsCartSynchronization(userId);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void needsCartSynchronization_UserWithoutItems_ReturnsFalse() {
        // Given
        Long userId = 1L;
        when(cartItemRepository.countByUserId(userId)).thenReturn(0L);
        
        // When
        boolean result = cartSyncService.needsCartSynchronization(userId);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void needsCartSynchronization_NullUserId_ReturnsFalse() {
        // When
        boolean result = cartSyncService.needsCartSynchronization(null);
        
        // Then
        assertThat(result).isFalse();
    }
    
    /**
     * Property 2: Cart Data Format Consistency
     * For any cart items loaded from database to localStorage, the resulting localStorage format
     * should match the format used by guest users (same field names and data types)
     * 
     * Feature: fix-logged-in-checkout, Property 2: Cart Data Format Consistency
     * Validates: Requirements 1.4, 2.3, 3.3
     */
    @Property(tries = 100)
    @Tag("Feature: fix-logged-in-checkout, Property 2: Cart Data Format Consistency")
    void cartDataFormatConsistency(
            @ForAll @NotEmpty List<@From("cartItems") CartItem> cartItems) {
        
        try {
            // When: Converting database cart items to localStorage format
            String localStorageJson = cartSyncService.convertCartItemsToLocalStorageFormat(cartItems);
            
            // Then: The format should match guest user localStorage format
            assertThat(localStorageJson).isNotNull();
            assertThat(localStorageJson).isNotEmpty();
            
            // Parse the JSON to verify structure
            @SuppressWarnings("unchecked")
            var cartData = objectMapper.readValue(localStorageJson, java.util.Map.class);
            
            // Verify top-level structure matches guest cart format
            assertThat(cartData).containsKeys("items", "total", "lastModified");
            
            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> items = (List<java.util.Map<String, Object>>) cartData.get("items");
            assertThat(items).hasSize(cartItems.size());
            
            // Verify each item has the required fields with correct types
            for (int i = 0; i < items.size(); i++) {
                java.util.Map<String, Object> item = items.get(i);
                CartItem originalItem = cartItems.get(i);
                
                // Verify required fields exist
                assertThat(item).containsKeys("productId", "productName", "price", "quantity", "imageUrl");
                
                // Verify field types match guest cart format
                assertThat(item.get("productId")).isInstanceOf(Number.class);
                assertThat(item.get("productName")).isInstanceOf(String.class);
                assertThat(item.get("price")).isInstanceOf(Number.class);
                assertThat(item.get("quantity")).isInstanceOf(Number.class);
                assertThat(item.get("imageUrl")).isInstanceOf(String.class);
                
                // Verify values are correctly mapped
                assertThat(((Number) item.get("productId")).longValue()).isEqualTo(originalItem.getProduct().getId());
                assertThat(item.get("productName")).isEqualTo(originalItem.getProduct().getName());
                assertThat(((Number) item.get("quantity")).intValue()).isEqualTo(originalItem.getQuantity());
                
                // Verify price is correctly formatted (should be a number, not string)
                double expectedPrice = originalItem.getUnitPrice().doubleValue();
                double actualPrice = ((Number) item.get("price")).doubleValue();
                assertThat(actualPrice).isCloseTo(expectedPrice, org.assertj.core.data.Offset.offset(0.01));
            }
            
            // Verify total is correctly calculated and is a number
            assertThat(cartData.get("total")).isInstanceOf(Number.class);
            double expectedTotal = cartItems.stream()
                    .mapToDouble(item -> item.getUnitPrice().doubleValue() * item.getQuantity())
                    .sum();
            double actualTotal = ((Number) cartData.get("total")).doubleValue();
            assertThat(actualTotal).isCloseTo(expectedTotal, org.assertj.core.data.Offset.offset(0.01));
            
            // Verify lastModified is a timestamp (number)
            assertThat(cartData.get("lastModified")).isInstanceOf(Number.class);
            long lastModified = ((Number) cartData.get("lastModified")).longValue();
            assertThat(lastModified).isGreaterThan(0);
            
        } catch (Exception e) {
            throw new RuntimeException("Format conversion should not fail for valid cart items", e);
        }
    }
    
    @Test
    void convertCartItemsToLocalStorageFormat_EmptyList_ReturnsEmptyCartJson() throws CartSyncException {
        // Given
        List<CartItem> emptyList = new ArrayList<>();
        
        // When
        String result = cartSyncService.convertCartItemsToLocalStorageFormat(emptyList);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("\"items\":[]");
        assertThat(result).contains("\"total\":0.0");
        assertThat(result).contains("\"lastModified\":");
    }
    
    @Test
    void convertCartItemsToLocalStorageFormat_NullList_ReturnsEmptyCartJson() throws CartSyncException {
        // When
        String result = cartSyncService.convertCartItemsToLocalStorageFormat(null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("\"items\":[]");
        assertThat(result).contains("\"total\":0.0");
        assertThat(result).contains("\"lastModified\":");
    }
}