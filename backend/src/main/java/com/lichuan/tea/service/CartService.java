package com.lichuan.tea.service;

import com.lichuan.tea.dto.CartItemDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    // In-memory storage: userId -> (productId -> Item)
    private final Map<String, Map<String, CartItemDTO>> carts = new ConcurrentHashMap<>();

    public void addToCart(String userId, CartItemDTO item) {
        System.out.println("Adding to cart: userId=" + userId + ", item=" + item);
        if (item == null || item.getProductId() == null) {
            System.err.println("Error: item or productId is null");
            return; 
        }
        carts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        Map<String, CartItemDTO> userCart = carts.get(userId);
        
        // Use productName as unique key because it includes specification
        String key = item.getProductName(); 
        if (key == null || key.isEmpty()) {
             key = item.getProductId().toString(); // Fallback
        }

        if (userCart.containsKey(key)) {
            CartItemDTO existingItem = userCart.get(key);
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            // Update price just in case, though it should be same for same spec
            existingItem.setPrice(item.getPrice()); 
        } else {
            userCart.put(key, item);
        }
    }

    public void removeFromCart(String userId, String productName) {
        Map<String, CartItemDTO> userCart = carts.get(userId);
        if (userCart != null) {
            userCart.remove(productName);
        }
    }

    public List<CartItemDTO> getCart(String userId) {
        Map<String, CartItemDTO> userCart = carts.get(userId);
        if (userCart == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(userCart.values());
    }

    public void clearCart(String userId) {
        carts.remove(userId);
    }
}
