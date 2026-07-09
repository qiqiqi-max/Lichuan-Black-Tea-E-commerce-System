package com.lichuan.tea.service;

import com.lichuan.tea.dto.CartItemDTO;
import com.lichuan.tea.entity.CartItem;
import com.lichuan.tea.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Transactional
    public void addToCart(String userIdStr, CartItemDTO item) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid User ID format.", e);
        }

        if (item == null || item.getProductId() == null) {
            throw new IllegalArgumentException("Product ID cannot be null.");
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserIdAndProductIdAndSpec(userId, item.getProductId(), item.getSpec());

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(item.getProductId());
            newItem.setProductName(item.getProductName());
            newItem.setSpec(item.getSpec());
            newItem.setPrice(item.getPrice());
            newItem.setCoverImg(item.getCoverImg());
            newItem.setQuantity(item.getQuantity());
            cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public void removeFromCart(String userIdStr, Long productId, String spec) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid User ID format.", e);
        }
        cartItemRepository.findByUserIdAndProductIdAndSpec(userId, productId, spec).ifPresent(cartItemRepository::delete);
    }

    public List<CartItemDTO> getCart(String userIdStr) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid User ID format.", e);
        }
        return cartItemRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearCart(String userIdStr) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid User ID format.", e);
        }
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemDTO convertToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setSpec(item.getSpec());
        dto.setPrice(item.getPrice());
        dto.setCoverImg(item.getCoverImg());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
