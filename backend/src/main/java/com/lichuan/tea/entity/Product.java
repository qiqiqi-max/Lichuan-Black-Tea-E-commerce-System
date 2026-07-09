package com.lichuan.tea.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(name = "product_story", columnDefinition = "TEXT")
    private String story;

    private BigDecimal price;
    private Integer stock;
    private Integer sales = 0;
    private String coverImg;
    private String category;

    private String origin;

    @ManyToOne
    @JoinColumn(name = "farmer_id")
    private FarmerProfile farmer;

    @Transient
    private Boolean flashSaleActive = false;

    @Transient
    private BigDecimal flashSalePrice;

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime flashSaleStartTime;

    @Transient
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime flashSaleEndTime;
}
