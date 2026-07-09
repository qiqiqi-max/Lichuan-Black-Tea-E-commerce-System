package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.DashboardStatsDTO;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.Order;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FarmerProfileRepository;
import com.lichuan.tea.repository.OrderItemRepository;
import com.lichuan.tea.repository.OrderRepository;
import com.lichuan.tea.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("用户未登录");
        }

        if ("FARMER".equals(currentUser.getRole())) {
            return getFarmerStats(currentUser);
        } else {
            return getAdminStats();
        }
    }

    private Result<DashboardStatsDTO> getAdminStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        stats.setTodayOrders(orderRepository.countByCreateTimeBetween(todayStart, todayEnd));

        BigDecimal totalSales = orderRepository.sumTotalSales();
        stats.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);

        stats.setTotalUsers(userRepository.count());
        stats.setPendingOrders(orderRepository.countByStatus("WAIT_SHIP"));

        List<DashboardStatsDTO.DailySales> last7Days = new ArrayList<>();
        LocalDateTime sevenDaysAgo = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
        List<Order> recentOrdersList = orderRepository.findAllByCreateTimeAfter(sevenDaysAgo);

        Map<String, BigDecimal> salesByDate = recentOrdersList.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreateTime().toLocalDate().toString(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
                ));

        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            BigDecimal sales = salesByDate.getOrDefault(date, BigDecimal.ZERO);
            last7Days.add(new DashboardStatsDTO.DailySales(date, sales));
        }
        stats.setLast7DaysSales(last7Days);

        List<Order> top10 = orderRepository.findTop10ByOrderByCreateTimeDesc();
        List<DashboardStatsDTO.RecentOrder> recentDTOs = top10.stream().map(o -> {
            DashboardStatsDTO.RecentOrder dto = new DashboardStatsDTO.RecentOrder();
            dto.setId(o.getId());
            dto.setOrderNo(o.getOrderNo());
            dto.setTotalAmount(o.getTotalAmount());
            dto.setStatus(o.getStatus());
            dto.setCreateTime(o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            Long orderUserId = o.getUserId();
            User u = orderUserId == null ? null : userRepository.findById(orderUserId).orElse(null);
            dto.setUser(u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "Unknown");
            return dto;
        }).collect(Collectors.toList());
        stats.setRecentOrders(recentDTOs);

        return Result.success(stats);
    }

    private Result<DashboardStatsDTO> getFarmerStats(User currentUser) {
        Optional<FarmerProfile> farmerProfileOpt = farmerProfileRepository.findByUserId(currentUser.getId());
        if (farmerProfileOpt.isEmpty()) {
            // Fallback for new farmer with no profile yet or other issues
            DashboardStatsDTO emptyStats = new DashboardStatsDTO();
            emptyStats.setTodayOrders(0L);
            emptyStats.setTotalSales(BigDecimal.ZERO);
            emptyStats.setPendingOrders(0L);
            emptyStats.setLast7DaysSales(new ArrayList<>());
            emptyStats.setRecentOrders(new ArrayList<>());
            return Result.success(emptyStats);
        }
        FarmerProfile farmerProfile = farmerProfileOpt.get();

        DashboardStatsDTO stats = new DashboardStatsDTO();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        stats.setTodayOrders(orderRepository.countByCreateTimeBetweenAndFarmerId(todayStart, todayEnd, farmerProfile.getId()));

        BigDecimal totalSales = orderItemRepository.sumTotalSalesByFarmerId(farmerProfile.getId());
        stats.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);

        // stats.setTotalUsers(1L); // This is not needed for farmer and causes confusion
        stats.setPendingOrders(orderRepository.countByStatusAndFarmerId("WAIT_SHIP", farmerProfile.getId()));

        // Last 7 Days Sales for Farmer
        List<DashboardStatsDTO.DailySales> last7Days = new ArrayList<>();
        LocalDateTime sevenDaysAgo = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
        List<Order> recentOrdersList = orderItemRepository.findAllByCreateTimeAfterAndFarmerId(sevenDaysAgo, farmerProfile.getId());

        Map<String, BigDecimal> salesByDate = recentOrdersList.stream()
                .flatMap(order -> order.getItems().stream()
                        .filter(item -> item.getFarmer() != null && item.getFarmer().getId().equals(farmerProfile.getId()))
                        .map(item -> new Object[]{order.getCreateTime().toLocalDate().toString(), item.getPrice().multiply(new BigDecimal(item.getQuantity()))})
                )
                .collect(Collectors.groupingBy(
                        data -> (String) data[0],
                        Collectors.reducing(BigDecimal.ZERO, data -> (BigDecimal) data[1], BigDecimal::add)
                ));

        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).toString();
            BigDecimal sales = salesByDate.getOrDefault(date, BigDecimal.ZERO);
            last7Days.add(new DashboardStatsDTO.DailySales(date, sales));
        }
        stats.setLast7DaysSales(last7Days);

        // Recent Orders for Farmer
        List<Order> top10 = orderItemRepository.findTop10ByFarmerIdOrderByCreateTimeDesc(farmerProfile.getId(), PageRequest.of(0, 10));
        List<DashboardStatsDTO.RecentOrder> recentDTOs = top10.stream().map(o -> {
            DashboardStatsDTO.RecentOrder dto = new DashboardStatsDTO.RecentOrder();
            dto.setId(o.getId());
            dto.setOrderNo(o.getOrderNo());
            // Calculate total amount for the farmer in this order
            BigDecimal farmerTotal = o.getItems().stream()
                    .filter(item -> item.getFarmer() != null && item.getFarmer().getId().equals(farmerProfile.getId()))
                    .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            dto.setTotalAmount(farmerTotal);
            dto.setStatus(o.getStatus());
            dto.setCreateTime(o.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            Long orderUserId = o.getUserId();
            User u = orderUserId == null ? null : userRepository.findById(orderUserId).orElse(null);
            dto.setUser(u != null ? (u.getNickname() != null ? u.getNickname() : u.getUsername()) : "Unknown");
            return dto;
        }).collect(Collectors.toList());
        stats.setRecentOrders(recentDTOs);

        return Result.success(stats);
    }

    private User getCurrentUser(String userId) {
        if (userId == null) return null;
        try {
            Long id = Long.parseLong(userId);
            Optional<User> user = userRepository.findById(id);
            return user.orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
