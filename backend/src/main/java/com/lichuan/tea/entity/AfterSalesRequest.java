package com.lichuan.tea.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "after_sales_requests")
public class AfterSalesRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String orderNo;
    private Long userId;

    // REFUND / RETURN_REFUND / EXCHANGE
    private String type;

    // PENDING_REVIEW / APPROVED / REJECTED / PROCESSING / COMPLETED
    private String status;

    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String contactName;
    private String contactPhone;

    private String reviewRemark;
    private String processRemark;

    private Long handledBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
