package com.lichuan.tea.service;

import com.lichuan.tea.dto.FarmerStorySaveRequest;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.FarmerStory;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FarmerStoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FarmerStoryService {

    @Autowired
    private FarmerStoryRepository farmerStoryRepository;

    public List<FarmerStory> listActive() {
        return farmerStoryRepository.findByStatusTrueOrderBySortOrderAscIdDesc();
    }

    public FarmerStory getActiveDetail(Long id) {
        if (id == null) {
            throw new RuntimeException("Story id is required");
        }
        return farmerStoryRepository.findByIdAndStatusTrue(id)
                .orElseThrow(() -> new RuntimeException("Story record not found or disabled"));
    }

    public PageResult<FarmerStory> getManagePage(
            User currentUser,
            String farmerName,
            String region,
            Boolean status,
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
        Page<FarmerStory> page = farmerStoryRepository.findManagePage(trimToNull(farmerName), trimToNull(region), status, pageable);
        return new PageResult<>(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public FarmerStory create(User currentUser, FarmerStorySaveRequest request) {
        ensureAdmin(currentUser);
        validateRequest(request);
        FarmerStory entity = new FarmerStory();
        applyPayload(entity, request);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        return farmerStoryRepository.save(entity);
    }

    public FarmerStory update(User currentUser, Long id, FarmerStorySaveRequest request) {
        ensureAdmin(currentUser);
        validateRequest(request);
        Long nonNullId = Objects.requireNonNull(id, "id");
        FarmerStory entity = farmerStoryRepository.findById(nonNullId)
                .orElseThrow(() -> new RuntimeException("Story record not found"));
        applyPayload(entity, request);
        entity.setUpdatedTime(LocalDateTime.now());
        return farmerStoryRepository.save(entity);
    }

    public FarmerStory updateStatus(User currentUser, Long id, Boolean status) {
        ensureAdmin(currentUser);
        if (status == null) {
            throw new RuntimeException("Status is required");
        }
        Long nonNullId = Objects.requireNonNull(id, "id");
        FarmerStory entity = farmerStoryRepository.findById(nonNullId)
                .orElseThrow(() -> new RuntimeException("Story record not found"));
        entity.setStatus(status);
        entity.setUpdatedTime(LocalDateTime.now());
        return farmerStoryRepository.save(entity);
    }

    public void delete(User currentUser, Long id) {
        ensureAdmin(currentUser);
        Long nonNullId = Objects.requireNonNull(id, "id");
        if (!farmerStoryRepository.existsById(nonNullId)) {
            throw new RuntimeException("Story record not found");
        }
        farmerStoryRepository.deleteById(nonNullId);
    }

    private void applyPayload(FarmerStory entity, FarmerStorySaveRequest request) {
        entity.setFarmerName(request.getFarmerName().trim());
        entity.setRegion(request.getRegion().trim());
        entity.setImageUrl(trimToNull(request.getImageUrl()));
        entity.setTagline(trimToNull(request.getTagline()));
        entity.setSummary(trimToNull(request.getSummary()));
        entity.setContent(trimToNull(request.getContent()));
        entity.setSortOrder(normalizeSortOrder(request.getSortOrder()));
        entity.setStatus(request.getStatus() == null || request.getStatus());
    }

    private void validateRequest(FarmerStorySaveRequest request) {
        if (request == null) {
            throw new RuntimeException("Request body is required");
        }
        if (trimToNull(request.getFarmerName()) == null) {
            throw new RuntimeException("Farmer name is required");
        }
        if (trimToNull(request.getRegion()) == null) {
            throw new RuntimeException("Region is required");
        }
        if (trimToNull(request.getTagline()) == null) {
            throw new RuntimeException("Tagline is required");
        }
        if (trimToNull(request.getSummary()) == null) {
            throw new RuntimeException("Summary is required");
        }
    }

    private int normalizeSortOrder(Integer sortOrder) {
        if (sortOrder == null) {
            return 0;
        }
        return Math.max(sortOrder, 0);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void ensureAdmin(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Only admin can manage farmer stories");
        }
    }
}
