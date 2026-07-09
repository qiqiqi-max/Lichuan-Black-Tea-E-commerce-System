package com.lichuan.tea.dto;

import lombok.Data;

@Data
public class FarmerStorySaveRequest {
    private String farmerName;
    private String region;
    private String imageUrl;
    private String tagline;
    private String summary;
    private String content;
    private Integer sortOrder;
    private Boolean status;
}
