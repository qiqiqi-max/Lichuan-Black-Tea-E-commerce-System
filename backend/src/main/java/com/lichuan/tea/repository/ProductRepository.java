package com.lichuan.tea.repository;

import com.lichuan.tea.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.name LIKE %?1% OR p.origin LIKE %?1%")
    List<Product> search(String keyword);
}
