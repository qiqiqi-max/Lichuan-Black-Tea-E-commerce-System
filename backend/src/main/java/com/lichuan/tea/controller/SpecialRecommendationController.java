package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.dto.SpecialRecommendationSaveRequest;
import com.lichuan.tea.entity.SpecialRecommendation;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.SpecialRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/special-recommendations")
public class SpecialRecommendationController {

    @Autowired
    private SpecialRecommendationService specialRecommendationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/active")
    public Result<List<SpecialRecommendation>> listActive() {
        return Result.success(specialRecommendationService.listActive());
    }

    @GetMapping("/manage")
    public Result<List<SpecialRecommendation>> listManage(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(specialRecommendationService.listManage(getCurrentUser(userId)));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/manage-page")
    public Result<PageResult<SpecialRecommendation>> managePage(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            return Result.success(specialRecommendationService.getManagePage(getCurrentUser(userId), productName, enabled, pageNum, pageSize));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<SpecialRecommendation> create(
            @RequestBody SpecialRecommendationSaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(specialRecommendationService.create(getCurrentUser(userId), request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<SpecialRecommendation> update(
            @PathVariable Long id,
            @RequestBody SpecialRecommendationSaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(specialRecommendationService.update(getCurrentUser(userId), id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            specialRecommendationService.delete(getCurrentUser(userId), id);
            return Result.success("Deleted");
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
