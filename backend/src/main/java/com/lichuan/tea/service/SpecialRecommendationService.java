package com.lichuan.tea.service;

import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.dto.SpecialRecommendationSaveRequest;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.SpecialRecommendation;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.ProductRepository;
import com.lichuan.tea.repository.SpecialRecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SpecialRecommendationService {

    @Autowired
    private SpecialRecommendationRepository specialRecommendationRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<SpecialRecommendation> listActive() {
        return specialRecommendationRepository.findByEnabledTrueOrderBySortOrderAscIdDesc();
    }

    public List<SpecialRecommendation> listManage(User currentUser) {
        ensureAdmin(currentUser);
        return specialRecommendationRepository.findAllByOrderBySortOrderAscIdDesc();
    }

    public PageResult<SpecialRecommendation> getManagePage(
            User currentUser,
            String productName,
            Boolean enabled,
            int pageNum,
            int pageSize) {
        ensureAdmin(currentUser);
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.desc("id")));
        Page<SpecialRecommendation> page = specialRecommendationRepository.findManagePage(trimToNull(productName), enabled, pageable);
        return new PageResult<>(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public SpecialRecommendation create(User currentUser, SpecialRecommendationSaveRequest request) {
        ensureAdmin(currentUser);
        validateRequest(request);
        Long productId = Objects.requireNonNull(request.getProductId(), "productId");

        if (specialRecommendationRepository.existsByProduct_Id(productId)) {
            throw new RuntimeException("This product is already in special recommendations");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        SpecialRecommendation entity = new SpecialRecommendation();
        entity.setProduct(product);
        entity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return specialRecommendationRepository.save(entity);
    }

    public SpecialRecommendation update(User currentUser, Long id, SpecialRecommendationSaveRequest request) {
        ensureAdmin(currentUser);
        validateRequest(request);
        Long nonNullId = Objects.requireNonNull(id, "id");
        Long productId = Objects.requireNonNull(request.getProductId(), "productId");

        SpecialRecommendation entity = specialRecommendationRepository.findById(nonNullId)
                .orElseThrow(() -> new RuntimeException("Recommendation record not found"));

        if (specialRecommendationRepository.existsByProduct_IdAndIdNot(productId, nonNullId)) {
            throw new RuntimeException("This product is already in special recommendations");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        entity.setProduct(product);
        entity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setUpdateTime(LocalDateTime.now());
        return specialRecommendationRepository.save(entity);
    }

    public void delete(User currentUser, Long id) {
        ensureAdmin(currentUser);
        Long nonNullId = Objects.requireNonNull(id, "id");
        if (!specialRecommendationRepository.existsById(nonNullId)) {
            throw new RuntimeException("Recommendation record not found");
        }
        specialRecommendationRepository.deleteById(nonNullId);
    }

    private void validateRequest(SpecialRecommendationSaveRequest request) {
        if (request == null || request.getProductId() == null) {
            throw new RuntimeException("Product is required");
        }
    }

    private int normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return 0;
        }
        return Math.max(sortOrder, 0);
    }

    private void ensureAdmin(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Only admin can manage special recommendations");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
