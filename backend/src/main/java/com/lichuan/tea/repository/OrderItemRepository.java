package com.lichuan.tea.repository;

import com.lichuan.tea.entity.Order;
import com.lichuan.tea.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT SUM(i.price * i.quantity) FROM OrderItem i WHERE i.farmer.id = :farmerId")
    BigDecimal sumTotalSalesByFarmerId(Long farmerId);

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.farmer.id = :farmerId AND o.createTime > :sevenDaysAgo")
    List<Order> findAllByCreateTimeAfterAndFarmerId(LocalDateTime sevenDaysAgo, Long farmerId);

    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.farmer.id = :farmerId")
    List<Order> findTop10ByFarmerIdOrderByCreateTimeDesc(Long farmerId, Pageable pageable);

}
