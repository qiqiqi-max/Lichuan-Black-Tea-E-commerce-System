package com.lichuan.tea.dto;

import lombok.Data;

@Data
public class AfterSalesReviewRequest {
    // APPROVE / REJECT
    private String action;
    private String remark;
}
