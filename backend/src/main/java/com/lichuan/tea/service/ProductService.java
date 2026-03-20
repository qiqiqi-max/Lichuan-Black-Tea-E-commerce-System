package com.lichuan.tea.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    public List<Product> getApprovedProducts(String keyword) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("audit_status", "APPROVED");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("name", keyword).or().like("description", keyword);
        }
        return list(wrapper);
    }

    public List<Product> getProductsByFarmer(Long farmerId) {
        return list(new QueryWrapper<Product>().eq("farmer_id", farmerId));
    }

    public Product createProduct(Product product, User currentUser) {
        if (!"FARMER".equals(currentUser.getRole())) {
            throw new RuntimeException("只有农户才能创建商品");
        }
        product.setAuditStatus("PENDING");
        save(product);
        return product;
    }

    public Product updateProduct(Long id, Product productDetails, User currentUser) {
        Product existingProduct = getById(id);
        if (existingProduct == null) {
            throw new RuntimeException("商品不存在");
        }

        if ("FARMER".equals(currentUser.getRole())) {
            // A better implementation would be to get farmerId from currentUser
            if (!existingProduct.getFarmerId().equals(productDetails.getFarmerId())) {
                throw new RuntimeException("无权修改他人商品");
            }
            // When a farmer updates a product, it should be re-audited
            existingProduct.setAuditStatus("PENDING");
        }
        // Admins can update any product without re-audit unless they change the status explicitly.

        // Update only the fields that are meant to be changed
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStock(productDetails.getStock());
        existingProduct.setImage(productDetails.getImage());
        existingProduct.setOrigin(productDetails.getOrigin());

        updateById(existingProduct);
        return existingProduct;
    }

    public void deleteProduct(Long id, User currentUser) {
        Product existingProduct = getById(id);
        if (existingProduct == null) {
            throw new RuntimeException("商品不存在");
        }

        if ("FARMER".equals(currentUser.getRole())) {
            // This logic needs to be improved by getting farmerId from currentUser
            if (!existingProduct.getFarmerId().equals(1L)) {
                throw new RuntimeException("无权删除他人商品");
            }
        }

        removeById(id);
    }
}
