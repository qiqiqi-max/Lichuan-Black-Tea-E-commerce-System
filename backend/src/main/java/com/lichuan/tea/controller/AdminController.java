package com.lichuan.tea.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lichuan.tea.common.Result;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.mapper.FarmerProfileMapper;
import com.lichuan.tea.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private FarmerProfileMapper farmerProfileMapper;

    @Autowired
    private ProductService productService;

    private boolean isAdmin(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return "ADMIN".equals(currentUser.getRole());
    }

    @GetMapping("/farmers/pending")
    public Result<List<FarmerProfile>> getPendingFarmers(HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error("无权限");
        List<FarmerProfile> profiles = farmerProfileMapper.selectList(new QueryWrapper<FarmerProfile>().eq("audit_status", "PENDING"));
        return Result.success(profiles);
    }

    @PostMapping("/farmers/approve/{id}")
    public Result<String> approveFarmer(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error("无权限");
        FarmerProfile profile = farmerProfileMapper.selectById(id);
        if (profile == null) return Result.error("找不到该农户");
        profile.setAuditStatus("APPROVED");
        farmerProfileMapper.updateById(profile);
        return Result.success("审核通过");
    }

    @PostMapping("/farmers/reject/{id}")
    public Result<String> rejectFarmer(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error("无权限");
        FarmerProfile profile = farmerProfileMapper.selectById(id);
        if (profile == null) return Result.error("找不到该农户");
        profile.setAuditStatus("REJECTED");
        farmerProfileMapper.updateById(profile);
        return Result.success("审核拒绝");
    }

    @GetMapping("/products/pending")
    public Result<List<Product>> getPendingProducts(HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error("无权限");
        List<Product> products = productService.list(new QueryWrapper<Product>().eq("audit_status", "PENDING"));
        return Result.success(products);
    }

    @PostMapping("/products/approve/{id}")
    public Result<String> approveProduct(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error("无权限");
        Product product = productService.getById(id);
        if (product == null) return Result.error("找不到该商品");
        product.setAuditStatus("APPROVED");
        productService.updateById(product);
        return Result.success("审核通过");
    }

    @PostMapping("/products/reject/{id}")
    public Result<String> rejectProduct(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error("无权限");
        Product product = productService.getById(id);
        if (product == null) return Result.error("找不到该商品");
        product.setAuditStatus("REJECTED");
        productService.updateById(product);
        return Result.success("审核拒绝");
    }
}
