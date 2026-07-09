package com.lichuan.tea.service;

import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.Order;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FarmerProfileRepository;
import com.lichuan.tea.repository.OrderRepository;
import com.lichuan.tea.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @Autowired
    private ProductRepository productRepository;

    public PageResult<Order> getPagedOrders(String keyword, int pageNum, int pageSize, User currentUser) {
        if (currentUser == null) {
            return new PageResult<>(0, pageNum, pageSize, Collections.emptyList());
        }

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.DESC, "createTime");
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Order> page;

        if ("ADMIN".equals(currentUser.getRole())) {
            page = orderRepository.findAllOrdersWithSearch(normalizedKeyword, pageable);
        } else if ("FARMER".equals(currentUser.getRole())) {
            List<Long> productIds = resolveFarmerProductIds(currentUser);
            if (productIds.isEmpty()) {
                return new PageResult<>(0, pageNum, pageSize, Collections.emptyList());
            }
            page = orderRepository.findFarmerOrdersByProductIdsWithSearch(productIds, normalizedKeyword, pageable);
        } else {
            return new PageResult<>(0, pageNum, pageSize, Collections.emptyList());
        }

        return new PageResult<>(
                page.getTotalElements(),
                pageNum,
                pageSize,
                page.getContent()
        );
    }

    public List<Order> getOrdersForRole(User currentUser) {
        if (currentUser == null) {
            return Collections.emptyList();
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            return orderRepository.findAllByOrderByCreateTimeDesc();
        }

        if ("FARMER".equals(currentUser.getRole())) {
            List<Long> productIds = resolveFarmerProductIds(currentUser);
            if (productIds.isEmpty()) {
                return Collections.emptyList();
            }
            return orderRepository.findFarmerOrdersByProductIds(productIds);
        }

        return Collections.emptyList();
    }

    private List<Long> resolveFarmerProductIds(User currentUser) {
        Optional<FarmerProfile> farmerProfileOpt = farmerProfileRepository.findByUserId(currentUser.getId());
        if (farmerProfileOpt.isEmpty()) {
            return Collections.emptyList();
        }

        return productRepository.findAllByFarmerId(farmerProfileOpt.get().getId()).stream()
                .map(Product::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
