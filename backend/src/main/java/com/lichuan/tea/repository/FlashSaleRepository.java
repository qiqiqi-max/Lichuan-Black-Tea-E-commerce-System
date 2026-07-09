package com.lichuan.tea.repository;

import com.lichuan.tea.entity.FlashSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
    List<FlashSale> findAllByOrderByStartTimeDescIdDesc();

    @Query("SELECT f FROM FlashSale f WHERE f.enabled = true AND f.startTime <= :now AND f.endTime > :now ORDER BY f.startTime ASC, f.id DESC")
    List<FlashSale> findActiveList(@Param("now") LocalDateTime now);

    Optional<FlashSale> findFirstByProduct_IdAndEnabledTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanOrderByStartTimeDescIdDesc(
            Long productId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT f FROM FlashSale f LEFT JOIN f.product p " +
            "WHERE (:productName IS NULL OR p.name LIKE %:productName%) " +
            "AND (" +
            ":status IS NULL " +
            "OR (:status = 'DISABLED' AND f.enabled = false) " +
            "OR (:status = 'UPCOMING' AND f.enabled = true AND f.startTime > :now) " +
            "OR (:status = 'ONGOING' AND f.enabled = true AND f.startTime <= :now AND f.endTime > :now) " +
            "OR (:status = 'ENDED' AND f.endTime <= :now)" +
            ") " +
            "ORDER BY f.createTime DESC")
    Page<FlashSale> findManagePage(@Param("productName") String productName,
                                   @Param("status") String status,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);
}
