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
@RequestMapping("/api")
public class FarmerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private FarmerProfileMapper farmerProfileMapper;

    @GetMapping("/farmer/products")
    public Result<List<Product>> getMyProducts(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (!"FARMER".equals(currentUser.getRole())) {
            return Result.error("无权限访问");
        }

        FarmerProfile farmerProfile = farmerProfileMapper.selectOne(new QueryWrapper<FarmerProfile>().eq("user_id", currentUser.getId()));
        if (farmerProfile == null) {
            return Result.error("找不到农户信息");
        }

        List<Product> products = productService.getProductsByFarmer(farmerProfile.getId());
        return Result.success(products);
    }

    @GetMapping("/farmer-profiles/{id}")
    public Result<FarmerProfile> getFarmerProfile(@PathVariable Long id) {
        FarmerProfile profile = farmerProfileMapper.selectById(id);
        if (profile == null) {
            return Result.error("找不到农户信息");
        }
        // For privacy, you might want to create a DTO and not return all fields
        return Result.success(profile);
    }
}
