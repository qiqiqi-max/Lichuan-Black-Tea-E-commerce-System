package com.lichuan.tea.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private BigDecimal price;
    private Integer stock;
    private String coverImg;
    private String category; // 冷后浑, 玛瑙红
    
    private String origin; // 产地
    private String farmerName; // 农户
}
