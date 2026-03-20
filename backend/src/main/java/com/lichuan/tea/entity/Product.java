package com.lichuan.tea.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String image;
    private String origin;
    private Integer sales;
    private Long farmerId;
    private String auditStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
