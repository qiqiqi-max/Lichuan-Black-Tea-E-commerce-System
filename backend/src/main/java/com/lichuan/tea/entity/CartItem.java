package com.lichuan.tea.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long productId;

    private String productName;

    private String spec;

    private java.math.BigDecimal price;

    private String coverImg;

    private Integer quantity;
}
