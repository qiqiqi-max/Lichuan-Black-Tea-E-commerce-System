package com.lichuan.tea.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("farmer_profiles")
public class FarmerProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String farmName;
    private String address;
    private String idCardImg;
    private String intro;
    private String auditStatus;
    private LocalDateTime auditTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
