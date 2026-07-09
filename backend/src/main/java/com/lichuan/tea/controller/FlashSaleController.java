package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.FlashSaleSaveRequest;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.FlashSale;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flash-sales")
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/active")
    public Result<List<FlashSale>> listActive() {
        return Result.success(flashSaleService.listActive());
    }

    @GetMapping("/manage")
    public Result<List<FlashSale>> listManage(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(flashSaleService.listManage(getCurrentUser(userId)));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/manage-page")
    public Result<PageResult<FlashSale>> managePage(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            return Result.success(flashSaleService.getManagePage(getCurrentUser(userId), productName, status, pageNum, pageSize));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<FlashSale> create(
            @RequestBody FlashSaleSaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(flashSaleService.create(getCurrentUser(userId), request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<FlashSale> update(
            @PathVariable Long id,
            @RequestBody FlashSaleSaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(flashSaleService.update(getCurrentUser(userId), id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            flashSaleService.delete(getCurrentUser(userId), id);
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
