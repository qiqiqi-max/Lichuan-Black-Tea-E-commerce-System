package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.Order;
import com.lichuan.tea.entity.OrderItem;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FlashSaleRepository;
import com.lichuan.tea.repository.OrderRepository;
import com.lichuan.tea.repository.ProductRepository;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Result<Order> createOrder(@RequestBody Order order, @RequestHeader("X-User-Id") String userId) {
        Long currentUserId;
        try {
            currentUserId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return Result.error("无效的用户ID");
        }

        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return Result.error("订单商品不能为空");
        }

        order.setUserId(currentUserId);
        order.setOrderNo(UUID.randomUUID().toString());
        order.setCreateTime(LocalDateTime.now());
        order.setStatus("WAIT_PAY");

        BigDecimal totalAmount = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null) {
                return Result.error("商品ID不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                return Result.error("购买数量必须大于0");
            }

            Long productId = Objects.requireNonNull(item.getProductId(), "productId");
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return Result.error("商品不存在: " + item.getProductId());
            }
            if (product.getPrice() == null) {
                return Result.error("商品价格缺失: " + item.getProductId());
            }

            BigDecimal unitPrice = flashSaleRepository
                    .findFirstByProduct_IdAndEnabledTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanOrderByStartTimeDescIdDesc(
                            product.getId(), now, now
                    )
                    .map(flashSale -> flashSale.getSeckillPrice())
                    .orElse(product.getPrice());

            item.setPrice(unitPrice);
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                item.setProductName(product.getName());
            }
            item.setFarmer(product.getFarmer());

            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            int currentSales = product.getSales() != null ? product.getSales() : 0;
            product.setSales(currentSales + item.getQuantity());
            productRepository.save(product);
        }

        order.setTotalAmount(totalAmount);
        return Result.success(orderRepository.save(order));
    }

    @GetMapping("/my")
    public Result<List<Order>> getMyOrders(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return Result.error("未登录");
        }
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }
        return Result.success(orderRepository.findByUserId(currentUser.getId()));
    }

    @GetMapping("/list")
    public Result<PageResult<Order>> getPagedOrders(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        if (userId == null) {
            return Result.error("未登录");
        }

        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        if (!"ADMIN".equals(currentUser.getRole()) && !"FARMER".equals(currentUser.getRole())) {
            return Result.error("权限不足");
        }

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        PageResult<Order> result = orderService.getPagedOrders(keyword, pageNum, pageSize, currentUser);
        return Result.success(result);
    }

    @GetMapping
    public Result<List<Order>> getOrders(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        if (userId == null) {
            return Result.error("未登录");
        }

        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        if ("FARMER".equals(currentUser.getRole())) {
            return Result.success(orderService.getOrdersForRole(currentUser));
        } else if ("ADMIN".equals(currentUser.getRole())) {
            return Result.success(orderService.getOrdersForRole(currentUser));
        } else {
            return Result.success(orderRepository.findByUserId(currentUser.getId()));
        }
    }

    private User getCurrentUser(String userId) {
        try {
            Long id = Long.parseLong(userId);
            Optional<User> user = userRepository.findById(id);
            return user.orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @PostMapping("/{id}/pay")
    public Result<Order> payOrder(@PathVariable Long id) {
        Long nonNullId = Objects.requireNonNull(id, "id");
        Order order = orderRepository.findById(nonNullId).orElse(null);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"WAIT_PAY".equals(order.getStatus())) {
            return Result.error("订单状态不正确");
        }
        order.setStatus("PAID");
        return Result.success(orderRepository.save(order));
    }

    @PutMapping("/{id}/ship")
    public Result<Order> shipOrder(@PathVariable Long id) {
        Long nonNullId = Objects.requireNonNull(id, "id");
        Order order = orderRepository.findById(nonNullId).orElse(null);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus("SHIPPED");
        return Result.success(orderRepository.save(order));
    }

    @PutMapping("/{id}")
    public Result<Order> updateOrder(@PathVariable Long id, @RequestBody Order request) {
        Long nonNullId = Objects.requireNonNull(id, "id");
        Order order = orderRepository.findById(nonNullId).orElse(null);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (request.getReceiverName() != null) {
            order.setReceiverName(request.getReceiverName());
        }
        if (request.getReceiverPhone() != null) {
            order.setReceiverPhone(request.getReceiverPhone());
        }
        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }
        if (request.getRemark() != null) {
            order.setRemark(request.getRemark());
        }
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        return Result.success(orderRepository.save(order));
    }
}
