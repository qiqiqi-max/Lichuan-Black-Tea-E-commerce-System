package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Public endpoint to get approved products
    @GetMapping
    public Result<List<Product>> list(@RequestParam(required = false) String search) {
        return Result.success(productService.getApprovedProducts(search));
    }

    @GetMapping("/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) { // Null check added
            return Result.error(404, "商品不存在");
        }
        if (!"APPROVED".equals(product.getAuditStatus())) {
            return Result.error(403, "该商品正在审核中或未通过审核");
        }
        return Result.success(product);
    }

    // Farmer/Admin endpoint to create a product
    @PostMapping
    public Result<Product> create(@RequestBody Product product, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        try {
            return Result.success(productService.createProduct(product, currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // Farmer/Admin endpoint to update a product
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        try {
            return Result.success(productService.updateProduct(id, product, currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // Farmer/Admin endpoint to delete a product
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        try {
            productService.deleteProduct(id, currentUser);
            return Result.success("删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
