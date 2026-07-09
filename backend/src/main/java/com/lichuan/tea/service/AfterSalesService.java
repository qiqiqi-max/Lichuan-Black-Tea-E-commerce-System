package com.lichuan.tea.service;

import com.lichuan.tea.dto.AfterSalesCreateRequest;
import com.lichuan.tea.dto.AfterSalesReviewRequest;
import com.lichuan.tea.dto.AfterSalesStatusUpdateRequest;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.AfterSalesRequest;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.Order;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.AfterSalesRequestRepository;
import com.lichuan.tea.repository.FarmerProfileRepository;
import com.lichuan.tea.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class AfterSalesService {

    @Autowired
    private AfterSalesRequestRepository afterSalesRequestRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    public AfterSalesRequest createRequest(User currentUser, AfterSalesCreateRequest request) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        if (request == null || request.getOrderId() == null) {
            throw new RuntimeException("Order is required");
        }
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new RuntimeException("Reason is required");
        }

        String type = normalize(request.getType());
        if (!isValidType(type)) {
            throw new RuntimeException("Unsupported after-sales type");
        }

        Long orderId = Objects.requireNonNull(request.getOrderId(), "orderId");
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if (!currentUser.getId().equals(order.getUserId())) {
            throw new RuntimeException("No permission for this order");
        }
        if (!isAfterSalesEligibleOrderStatus(order.getStatus())) {
            throw new RuntimeException("Only paid orders can apply for after-sales");
        }

        List<String> activeStatuses = Arrays.asList("PENDING_REVIEW", "APPROVED", "PROCESSING");
        boolean existsActive = afterSalesRequestRepository.existsByOrderIdAndUserIdAndStatusIn(
                order.getId(), currentUser.getId(), activeStatuses
        );
        if (existsActive) {
            throw new RuntimeException("This order already has an active after-sales request");
        }

        AfterSalesRequest entity = new AfterSalesRequest();
        entity.setOrderId(order.getId());
        entity.setOrderNo(order.getOrderNo());
        entity.setUserId(currentUser.getId());
        entity.setType(type);
        entity.setStatus("PENDING_REVIEW");
        entity.setReason(request.getReason());
        entity.setDescription(request.getDescription());
        entity.setContactName(
                isBlank(request.getContactName())
                        ? (isBlank(currentUser.getNickname()) ? currentUser.getUsername() : currentUser.getNickname())
                        : request.getContactName()
        );
        entity.setContactPhone(isBlank(request.getContactPhone()) ? currentUser.getPhone() : request.getContactPhone());

        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        return afterSalesRequestRepository.save(entity);
    }

    public List<AfterSalesRequest> listMyRequests(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        return afterSalesRequestRepository.findByUserIdOrderByCreateTimeDesc(currentUser.getId());
    }

    public List<AfterSalesRequest> listManageRequests(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        if ("ADMIN".equals(currentUser.getRole())) {
            return afterSalesRequestRepository.findAllByOrderByCreateTimeDesc();
        }
        if ("FARMER".equals(currentUser.getRole())) {
            Optional<FarmerProfile> farmerProfileOpt = farmerProfileRepository.findByUserId(currentUser.getId());
            if (farmerProfileOpt.isEmpty()) {
                return Collections.emptyList();
            }
            return afterSalesRequestRepository.findManageListByFarmerId(farmerProfileOpt.get().getId());
        }
        throw new RuntimeException("Insufficient permission");
    }

    public PageResult<AfterSalesRequest> getManagePage(
            User currentUser,
            String keyword,
            Long userId,
            String status,
            String type,
            int pageNum,
            int pageSize) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.DESC, "createTime");
        String trimmedKeyword = trimToNull(keyword);
        String normalizedStatus = trimToNull(status);
        String normalizedType = trimToNull(type);
        if (normalizedStatus != null) {
            normalizedStatus = normalizedStatus.toUpperCase(Locale.ROOT);
        }
        if (normalizedType != null) {
            normalizedType = normalizedType.toUpperCase(Locale.ROOT);
        }

        Page<AfterSalesRequest> page;
        if ("ADMIN".equals(currentUser.getRole())) {
            page = afterSalesRequestRepository.findManagePage(
                    trimmedKeyword,
                    userId,
                    normalizedStatus,
                    normalizedType,
                    pageable
            );
        } else if ("FARMER".equals(currentUser.getRole())) {
            Optional<FarmerProfile> farmerProfileOpt = farmerProfileRepository.findByUserId(currentUser.getId());
            if (farmerProfileOpt.isEmpty()) {
                return new PageResult<>(0, pageNum, pageSize, Collections.emptyList());
            }
            page = afterSalesRequestRepository.findManagePageByFarmerId(
                    farmerProfileOpt.get().getId(),
                    trimmedKeyword,
                    userId,
                    normalizedStatus,
                    normalizedType,
                    pageable
            );
        } else {
            throw new RuntimeException("Insufficient permission");
        }

        return new PageResult<>(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public AfterSalesRequest reviewRequest(User currentUser, Long id, AfterSalesReviewRequest request) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        Long nonNullId = Objects.requireNonNull(id, "id");
        AfterSalesRequest entity = afterSalesRequestRepository.findById(nonNullId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("After-sales request not found");
        }
        ensureManagePermission(currentUser, entity);
        if (!"PENDING_REVIEW".equals(entity.getStatus())) {
            throw new RuntimeException("Current status cannot be reviewed");
        }

        String action = normalize(request == null ? null : request.getAction());
        if ("APPROVE".equals(action)) {
            entity.setStatus("APPROVED");
        } else if ("REJECT".equals(action)) {
            entity.setStatus("REJECTED");
        } else {
            throw new RuntimeException("Unsupported review action");
        }

        entity.setReviewRemark(request == null ? null : request.getRemark());
        entity.setHandledBy(currentUser.getId());
        entity.setUpdateTime(LocalDateTime.now());
        return afterSalesRequestRepository.save(entity);
    }

    public AfterSalesRequest updateRequestStatus(User currentUser, Long id, AfterSalesStatusUpdateRequest request) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        Long nonNullId = Objects.requireNonNull(id, "id");
        AfterSalesRequest entity = afterSalesRequestRepository.findById(nonNullId).orElse(null);
        if (entity == null) {
            throw new RuntimeException("After-sales request not found");
        }
        ensureManagePermission(currentUser, entity);

        String targetStatus = normalize(request == null ? null : request.getStatus());
        String currentStatus = entity.getStatus();

        if ("PROCESSING".equals(targetStatus)) {
            if (!"APPROVED".equals(currentStatus)) {
                throw new RuntimeException("Only approved requests can move to processing");
            }
        } else if ("COMPLETED".equals(targetStatus)) {
            if (!"APPROVED".equals(currentStatus) && !"PROCESSING".equals(currentStatus)) {
                throw new RuntimeException("Current status cannot be completed");
            }
        } else {
            throw new RuntimeException("Unsupported target status");
        }

        entity.setStatus(targetStatus);
        entity.setProcessRemark(request == null ? null : request.getRemark());
        entity.setHandledBy(currentUser.getId());
        entity.setUpdateTime(LocalDateTime.now());
        return afterSalesRequestRepository.save(entity);
    }

    private void ensureManagePermission(User currentUser, AfterSalesRequest request) {
        if ("ADMIN".equals(currentUser.getRole())) {
            return;
        }
        if (!"FARMER".equals(currentUser.getRole())) {
            throw new RuntimeException("Insufficient permission");
        }

        Optional<FarmerProfile> farmerProfileOpt = farmerProfileRepository.findByUserId(currentUser.getId());
        if (farmerProfileOpt.isEmpty()) {
            throw new RuntimeException("Farmer profile not found");
        }
        long count = orderRepository.countByOrderIdAndFarmerId(request.getOrderId(), farmerProfileOpt.get().getId());
        if (count <= 0) {
            throw new RuntimeException("No permission to handle this request");
        }
    }

    private boolean isValidType(String type) {
        return "REFUND".equals(type) || "RETURN_REFUND".equals(type) || "EXCHANGE".equals(type);
    }

    private boolean isAfterSalesEligibleOrderStatus(String status) {
        return "PAID".equals(status)
                || "WAIT_SHIP".equals(status)
                || "SHIPPED".equals(status)
                || "DONE".equals(status);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
