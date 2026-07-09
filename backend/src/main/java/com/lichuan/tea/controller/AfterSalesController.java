package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.AfterSalesCreateRequest;
import com.lichuan.tea.dto.AfterSalesReviewRequest;
import com.lichuan.tea.dto.AfterSalesStatusUpdateRequest;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.AfterSalesRequest;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.AfterSalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/after-sales")
public class AfterSalesController {

    @Autowired
    private AfterSalesService afterSalesService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public Result<AfterSalesRequest> create(
            @RequestBody AfterSalesCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("Please login first");
        }
        try {
            return Result.success(afterSalesService.createRequest(currentUser, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/my")
    public Result<List<AfterSalesRequest>> getMyRequests(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("Please login first");
        }
        try {
            return Result.success(afterSalesService.listMyRequests(currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/manage")
    public Result<List<AfterSalesRequest>> getManageRequests(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("Please login first");
        }
        try {
            return Result.success(afterSalesService.listManageRequests(currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/manage-page")
    public Result<PageResult<AfterSalesRequest>> getManagePage(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long filterUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("Please login first");
        }
        try {
            return Result.success(afterSalesService.getManagePage(currentUser, keyword, filterUserId, status, type, pageNum, pageSize));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/review")
    public Result<AfterSalesRequest> review(
            @PathVariable Long id,
            @RequestBody AfterSalesReviewRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("Please login first");
        }
        try {
            return Result.success(afterSalesService.reviewRequest(currentUser, id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<AfterSalesRequest> updateStatus(
            @PathVariable Long id,
            @RequestBody AfterSalesStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        if (currentUser == null) {
            return Result.error("Please login first");
        }
        try {
            return Result.success(afterSalesService.updateRequestStatus(currentUser, id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private User getCurrentUser(String userId) {
        if (userId == null) {
            return null;
        }
        try {
            Long id = Long.parseLong(userId);
            Optional<User> user = userRepository.findById(id);
            return user.orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
