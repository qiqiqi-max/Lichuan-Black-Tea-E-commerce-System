package com.lichuan.tea.dto;

import lombok.Data;

@Data
public class AfterSalesCreateRequest {
    private Long orderId;
    private String type;
    private String reason;
    private String description;
    private String contactName;
    private String contactPhone;
}
