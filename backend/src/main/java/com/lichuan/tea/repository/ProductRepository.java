package com.lichuan.tea.repository;

import com.lichuan.tea.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1% OR p.origin LIKE %?1%")
    List<Product> search(String keyword);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1% OR p.origin LIKE %?1% OR p.description LIKE %?1%")
    Page<Product> searchPage(String keyword, Pageable pageable);

    Page<Product> findAllByFarmerId(Long farmerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.farmer.id = :farmerId AND (p.name LIKE %:keyword% OR p.origin LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchPageByFarmerId(String keyword, Long farmerId, Pageable pageable);

    List<Product> findAllByFarmerId(Long farmerId);

    @Query("SELECT p FROM Product p WHERE p.farmer.id = :farmerId AND (p.name LIKE %:keyword% OR p.origin LIKE %:keyword%)")
    List<Product> searchByFarmerId(String keyword, Long farmerId);

    @Query("SELECT p FROM Product p LEFT JOIN p.farmer f " +
            "WHERE (:name IS NULL OR p.name LIKE %:name%) " +
            "AND (:origin IS NULL OR p.origin LIKE %:origin%) " +
            "AND (:farmerName IS NULL OR f.realName LIKE %:farmerName%) " +
            "ORDER BY p.id DESC")
    Page<Product> findManagePage(@Param("name") String name,
                                 @Param("origin") String origin,
                                 @Param("farmerName") String farmerName,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN p.farmer f " +
            "WHERE f.id = :farmerId " +
            "AND (:name IS NULL OR p.name LIKE %:name%) " +
            "AND (:origin IS NULL OR p.origin LIKE %:origin%) " +
            "AND (:farmerName IS NULL OR f.realName LIKE %:farmerName%) " +
            "ORDER BY p.id DESC")
    Page<Product> findManagePageByFarmerId(@Param("farmerId") Long farmerId,
                                           @Param("name") String name,
                                           @Param("origin") String origin,
                                           @Param("farmerName") String farmerName,
                                           Pageable pageable);
}
