package com.lichuan.tea.repository;

import com.lichuan.tea.entity.FarmerStory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FarmerStoryRepository extends JpaRepository<FarmerStory, Long> {
    List<FarmerStory> findByStatusTrueOrderBySortOrderAscIdDesc();
    Optional<FarmerStory> findByIdAndStatusTrue(Long id);

    @Query("SELECT f FROM FarmerStory f " +
            "WHERE (:farmerName IS NULL OR f.farmerName LIKE %:farmerName%) " +
            "AND (:region IS NULL OR f.region LIKE %:region%) " +
            "AND (:status IS NULL OR f.status = :status) " +
            "ORDER BY f.sortOrder ASC, f.id DESC")
    Page<FarmerStory> findManagePage(@Param("farmerName") String farmerName,
                                     @Param("region") String region,
                                     @Param("status") Boolean status,
                                     Pageable pageable);
}
