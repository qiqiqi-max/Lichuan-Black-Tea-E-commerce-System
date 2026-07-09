package com.lichuan.tea.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页结果DTO
 */
@Data
public class PageResult<T> {
    private long total;           // 总记录数
    private long pageNum;         // 当前页码
    private long pageSize;        // 每页数量
    private List<T> records;      // 数据记录

    public PageResult(long total, long pageNum, long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
    }
}
