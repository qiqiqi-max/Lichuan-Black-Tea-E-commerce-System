package com.lichuan.tea.repository;

import com.lichuan.tea.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findAllByOrderByCreateTimeDesc();
    
    // 新增：Admin 用 - 支持分页、搜索、倒序
    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR o.orderNo LIKE %:keyword% OR o.address LIKE %:keyword% OR CAST(o.userId AS string) LIKE %:keyword%) " +
           "ORDER BY o.createTime DESC")
    Page<Order> findAllOrdersWithSearch(String keyword, Pageable pageable);
    
    // 新增：Farmer 用 - 只看自己商品的订单，支持分页、搜索、倒序
    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i " +
           "WHERE i.farmer.id = :farmerId " +
           "AND (:keyword IS NULL OR o.orderNo LIKE %:keyword% OR o.address LIKE %:keyword% OR CAST(o.userId AS string) LIKE %:keyword%) " +
           "ORDER BY o.createTime DESC")
    Page<Order> findFarmerOrdersWithSearch(Long farmerId, String keyword, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i " +
           "WHERE i.productId IN :productIds " +
           "AND (:keyword IS NULL OR o.orderNo LIKE %:keyword% OR o.address LIKE %:keyword% OR CAST(o.userId AS string) LIKE %:keyword%) " +
           "ORDER BY o.createTime DESC")
    Page<Order> findFarmerOrdersByProductIdsWithSearch(@Param("productIds") List<Long> productIds,
                                                       @Param("keyword") String keyword,
                                                       Pageable pageable);
    
    // For Dashboard
    long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    long countByStatus(String status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o")
    BigDecimal sumTotalSales();
    
    List<Order> findTop10ByOrderByCreateTimeDesc();
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.farmer.id = :farmerId")
    List<Order> findOrdersByFarmerId(Long farmerId);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.items i WHERE i.productId IN :productIds ORDER BY o.createTime DESC")
    List<Order> findFarmerOrdersByProductIds(@Param("productIds") List<Long> productIds);

    @Query("SELECT COUNT(DISTINCT o.id) FROM Order o JOIN o.items i WHERE i.farmer.id = :farmerId AND o.createTime BETWEEN :start AND :end")
    long countByCreateTimeBetweenAndFarmerId(LocalDateTime start, LocalDateTime end, Long farmerId);

    @Query("SELECT COUNT(DISTINCT o.id) FROM Order o JOIN o.items i WHERE i.farmer.id = :farmerId AND o.status = :status")
    long countByStatusAndFarmerId(String status, Long farmerId);

    List<Order> findAllByCreateTimeAfter(LocalDateTime date);

    @Query("SELECT COUNT(o.id) FROM Order o JOIN o.items i WHERE o.id = ?1 AND i.farmer.id = ?2")
    long countByOrderIdAndFarmerId(Long orderId, Long farmerId);
}
