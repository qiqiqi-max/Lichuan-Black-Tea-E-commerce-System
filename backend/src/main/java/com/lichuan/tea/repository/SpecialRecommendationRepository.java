package com.lichuan.tea.repository;

import com.lichuan.tea.entity.SpecialRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpecialRecommendationRepository extends JpaRepository<SpecialRecommendation, Long> {
    List<SpecialRecommendation> findByEnabledTrueOrderBySortOrderAscIdDesc();

    List<SpecialRecommendation> findAllByOrderBySortOrderAscIdDesc();

    boolean existsByProduct_Id(Long productId);

    boolean existsByProduct_IdAndIdNot(Long productId, Long id);

    @Query("SELECT r FROM SpecialRecommendation r LEFT JOIN r.product p " +
            "WHERE (:productName IS NULL OR p.name LIKE %:productName%) " +
            "AND (:enabled IS NULL OR r.enabled = :enabled) " +
            "ORDER BY r.sortOrder ASC, r.id DESC")
    Page<SpecialRecommendation> findManagePage(@Param("productName") String productName,
                                               @Param("enabled") Boolean enabled,
                                               Pageable pageable);
}
