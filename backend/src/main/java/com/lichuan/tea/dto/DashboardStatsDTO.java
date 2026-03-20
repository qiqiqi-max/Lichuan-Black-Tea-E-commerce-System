package com.lichuan.tea.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsDTO {
    private long todayOrders;
    private BigDecimal totalSales;
    private long totalUsers;
    private long pendingOrders;
    private List<DailySales> last7DaysSales;
    private List<RecentOrder> recentOrders;

    @Data
    public static class DailySales {
        private String date;
        private BigDecimal sales;

        public DailySales(String date, BigDecimal sales) {
            this.date = date;
            this.sales = sales;
        }
        
        // Manual Getters/Setters just in case
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public BigDecimal getSales() { return sales; }
        public void setSales(BigDecimal sales) { this.sales = sales; }
    }

    @Data
    public static class RecentOrder {
        private Long id;
        private String orderNo;
        private String user; // username or nickname
        private BigDecimal totalAmount;
        private String createTime;
        private String status;

        // Manual Getters/Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    // Manual Getters/Setters for main DTO
    public long getTodayOrders() { return todayOrders; }
    public void setTodayOrders(long todayOrders) { this.todayOrders = todayOrders; }
    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
    public List<DailySales> getLast7DaysSales() { return last7DaysSales; }
    public void setLast7DaysSales(List<DailySales> last7DaysSales) { this.last7DaysSales = last7DaysSales; }
    public List<RecentOrder> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<RecentOrder> recentOrders) { this.recentOrders = recentOrders; }
}
