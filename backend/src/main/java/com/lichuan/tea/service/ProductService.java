package com.lichuan.tea.service;

import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.FlashSale;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FarmerProfileRepository;
import com.lichuan.tea.repository.FlashSaleRepository;
import com.lichuan.tea.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @Autowired
    private FlashSaleRepository flashSaleRepository;


    // Public-facing product list: always platform-wide.
    public List<Product> list(String search) {
        if (search != null && !search.isEmpty()) {
            return productRepository.search(search);
        }
        return productRepository.findAll();
    }

    // Public-facing product search: always platform-wide.
    public Page<Product> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (keyword == null || keyword.isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchPage(keyword, pageable);
    }

    public PageResult<Product> getManagePage(
            String name,
            String origin,
            String farmerName,
            int pageNum,
            int pageSize,
            User currentUser) {

        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.DESC, "id");
        String trimmedName = trimToNull(name);
        String trimmedOrigin = trimToNull(origin);
        String trimmedFarmerName = trimToNull(farmerName);

        Page<Product> page;
        if ("FARMER".equals(currentUser.getRole())) {
            Optional<FarmerProfile> farmerProfile = farmerProfileRepository.findByUserId(currentUser.getId());
            if (farmerProfile.isEmpty()) {
                return new PageResult<>(0, pageNum, pageSize, List.of());
            }
            page = productRepository.findManagePageByFarmerId(
                    farmerProfile.get().getId(),
                    trimmedName,
                    trimmedOrigin,
                    trimmedFarmerName,
                    pageable
            );
        } else if ("ADMIN".equals(currentUser.getRole())) {
            page = productRepository.findManagePage(trimmedName, trimmedOrigin, trimmedFarmerName, pageable);
        } else {
            throw new RuntimeException("Insufficient permission");
        }

        return new PageResult<>(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public Product getDetail(Long id) {
        if (id == null) {
            return null;
        }
        Long nonNullId = Objects.requireNonNull(id, "id");
        Product product = productRepository.findById(nonNullId).orElse(null);
        if (product == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<FlashSale> activeFlashSale = flashSaleRepository
                .findFirstByProduct_IdAndEnabledTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanOrderByStartTimeDescIdDesc(
                        nonNullId, now, now
                );
        if (activeFlashSale.isPresent()) {
            FlashSale flashSale = activeFlashSale.get();
            product.setFlashSaleActive(true);
            product.setFlashSalePrice(flashSale.getSeckillPrice());
            product.setFlashSaleStartTime(flashSale.getStartTime());
            product.setFlashSaleEndTime(flashSale.getEndTime());
        } else {
            product.setFlashSaleActive(false);
            product.setFlashSalePrice(null);
            product.setFlashSaleStartTime(null);
            product.setFlashSaleEndTime(null);
        }
        return product;
    }

    public Product create(Product product, User currentUser) {
        ensureManagePermission(currentUser);
        Product nonNullProduct = Objects.requireNonNull(product, "product");
        if ("FARMER".equals(currentUser.getRole())) {
            nonNullProduct.setFarmer(resolveFarmerProfile(currentUser));
        }
        return productRepository.save(nonNullProduct);
    }

    public Product update(Long id, Product request, User currentUser) {
        ensureManagePermission(currentUser);
        Long nonNullId = Objects.requireNonNull(id, "id");
        Product nonNullRequest = Objects.requireNonNull(request, "request");
        Product existing = productRepository.findById(nonNullId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if ("FARMER".equals(currentUser.getRole())) {
            FarmerProfile farmerProfile = resolveFarmerProfile(currentUser);
            if (existing.getFarmer() == null || !farmerProfile.getId().equals(existing.getFarmer().getId())) {
                throw new RuntimeException("You can only edit your own products");
            }
            existing.setFarmer(farmerProfile);
        }

        existing.setName(nonNullRequest.getName());
        existing.setDescription(nonNullRequest.getDescription());
        existing.setStory(nonNullRequest.getStory());
        existing.setPrice(nonNullRequest.getPrice());
        existing.setStock(nonNullRequest.getStock());
        if (nonNullRequest.getSales() != null) {
            existing.setSales(nonNullRequest.getSales());
        }
        existing.setCoverImg(nonNullRequest.getCoverImg());
        if (nonNullRequest.getCategory() != null) {
            existing.setCategory(nonNullRequest.getCategory());
        }
        existing.setOrigin(nonNullRequest.getOrigin());

        return productRepository.save(existing);
    }

    public void delete(Long id, User currentUser) {
        ensureManagePermission(currentUser);
        Long nonNullId = Objects.requireNonNull(id, "id");
        Product existing = productRepository.findById(nonNullId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if ("FARMER".equals(currentUser.getRole())) {
            FarmerProfile farmerProfile = resolveFarmerProfile(currentUser);
            if (existing.getFarmer() == null || !farmerProfile.getId().equals(existing.getFarmer().getId())) {
                throw new RuntimeException("You can only delete your own products");
            }
        }

        productRepository.delete(Objects.requireNonNull(existing, "existing"));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private FarmerProfile resolveFarmerProfile(User currentUser) {
        return farmerProfileRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Farmer profile not found"));
    }

    private void ensureManagePermission(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        if (!"ADMIN".equals(currentUser.getRole()) && !"FARMER".equals(currentUser.getRole())) {
            throw new RuntimeException("Insufficient permission");
        }
    }
}
