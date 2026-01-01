package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.entity.Order;
import com.lichuan.tea.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/orders")
    public Result<Order> createOrder(@RequestBody Order order) {
        order.setOrderNo(UUID.randomUUID().toString());
        order.setCreateTime(LocalDateTime.now());
        order.setStatus("WAIT_PAY"); // 默认待支付
        
        // 关联子项
        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                // item.setOrder(order); // 如果有双向关联需设置，这里是单向
            });
        }
        
        return Result.success(orderRepository.save(order));
    }

    @GetMapping("/orders/my")
    public Result<List<Order>> myOrders(@RequestHeader("Authorization") String token) {
        // Mock: extract userId from token "mock-token-{id}"
        if (token == null || !token.startsWith("mock-token-")) {
            return Result.error("未登录");
        }
        Long userId = Long.parseLong(token.replace("mock-token-", ""));
        return Result.success(orderRepository.findByUserId(userId));
    }

    @GetMapping("/admin/orders")
    public Result<List<Order>> allOrders() {
        return Result.success(orderRepository.findAllByOrderByCreateTimeDesc());
    }

    @PutMapping("/admin/orders/{id}/ship")
    public Result<Order> shipOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return Result.error("订单不存在");
        order.setStatus("SHIPPED");
        return Result.success(orderRepository.save(order));
    }
}
