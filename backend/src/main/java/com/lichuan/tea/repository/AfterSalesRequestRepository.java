package com.lichuan.tea.repository;

import com.lichuan.tea.entity.AfterSalesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AfterSalesRequestRepository extends JpaRepository<AfterSalesRequest, Long> {

    List<AfterSalesRequest> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<AfterSalesRequest> findAllByOrderByCreateTimeDesc();

    boolean existsByOrderIdAndUserIdAndStatusIn(Long orderId, Long userId, Collection<String> statuses);

    @Query("SELECT r FROM AfterSalesRequest r WHERE r.orderId IN (" +
            "SELECT DISTINCT o.id FROM Order o JOIN o.items i WHERE i.farmer.id = ?1" +
            ") ORDER BY r.createTime DESC")
    List<AfterSalesRequest> findManageListByFarmerId(Long farmerId);

    @Query("SELECT r FROM AfterSalesRequest r " +
            "WHERE (:keyword IS NULL OR r.orderNo LIKE %:keyword% OR CAST(r.id AS string) LIKE %:keyword%) " +
            "AND (:userId IS NULL OR r.userId = :userId) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:type IS NULL OR r.type = :type) " +
            "ORDER BY r.createTime DESC")
    Page<AfterSalesRequest> findManagePage(@Param("keyword") String keyword,
                                           @Param("userId") Long userId,
                                           @Param("status") String status,
                                           @Param("type") String type,
                                           Pageable pageable);

    @Query("SELECT r FROM AfterSalesRequest r WHERE r.orderId IN (" +
            "SELECT DISTINCT o.id FROM Order o JOIN o.items i WHERE i.farmer.id = :farmerId" +
            ") " +
            "AND (:keyword IS NULL OR r.orderNo LIKE %:keyword% OR CAST(r.id AS string) LIKE %:keyword%) " +
            "AND (:userId IS NULL OR r.userId = :userId) " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:type IS NULL OR r.type = :type) " +
            "ORDER BY r.createTime DESC")
    Page<AfterSalesRequest> findManagePageByFarmerId(@Param("farmerId") Long farmerId,
                                                     @Param("keyword") String keyword,
                                                     @Param("userId") Long userId,
                                                     @Param("status") String status,
                                                     @Param("type") String type,
                                                     Pageable pageable);
}
