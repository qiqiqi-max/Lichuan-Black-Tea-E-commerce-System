package com.lichuan.tea.dto;

import lombok.Data;

@Data
public class SpecialRecommendationSaveRequest {
    private Long productId;
    private Integer sortOrder;
    private Boolean enabled;
}

