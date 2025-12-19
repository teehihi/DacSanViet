/**
 * Cart Sync Handler - YAME BEHAVIOR
 * Cart is NOT synced with server
 * Each browser has its own independent cart (localStorage only)
 */

(function() {
    'use strict';
    
    console.log('Cart sync disabled - Yame behavior: localStorage only, no server sync');
    
    // Yame behavior: Cart is browser-specific
    // - Different browsers = different carts
    // - Same account, different browsers = different carts
    // - Cart only exists in localStorage (not in database)
    
    // Update cart badge on page load
    document.addEventListener('DOMContentLoaded', function() {
        if (window.CartManager) {
            CartManager.updateCartBadge();
            console.log('Cart badge updated from localStorage');
        }
    });
    
})();
