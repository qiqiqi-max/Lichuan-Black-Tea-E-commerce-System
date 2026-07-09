package com.lichuan.tea.dto;

import lombok.Data;

@Data
public class AfterSalesStatusUpdateRequest {
    // PROCESSING / COMPLETED
    private String status;
    private String remark;
}
