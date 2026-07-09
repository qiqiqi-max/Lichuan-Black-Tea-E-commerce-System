package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.entity.Order;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/admin/orders")
    public Result<List<Order>> getAllOrders(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = null;
        if (userId != null) {
            currentUser = userRepository.findById(Long.parseLong(userId)).orElse(null);
        }
        return Result.success(orderService.getOrdersForRole(currentUser));
    }
}
