package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public Result<List<Product>> list(@RequestParam(required = false) String search) {
        if (search != null && !search.isEmpty()) {
            return Result.success(productRepository.search(search));
        }
        return Result.success(productRepository.findAll());
    }

    @PostMapping("/admin/products")
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productRepository.save(product));
    }

    @PutMapping("/admin/products/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return Result.success(productRepository.save(product));
    }

    @DeleteMapping("/admin/products/{id}")
    public Result<String> delete(@PathVariable Long id) {
        productRepository.deleteById(id);
        return Result.success("Deleted");
    }
}
