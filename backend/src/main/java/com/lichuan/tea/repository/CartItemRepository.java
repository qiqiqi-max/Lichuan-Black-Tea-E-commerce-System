package com.lichuan.tea.repository;

import com.lichuan.tea.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    Optional<CartItem> findByUserIdAndProductIdAndSpec(Long userId, Long productId, String spec);
    void deleteByUserId(Long userId);
}
