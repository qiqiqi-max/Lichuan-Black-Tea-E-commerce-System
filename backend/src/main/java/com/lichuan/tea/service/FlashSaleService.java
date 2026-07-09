package com.lichuan.tea.service;

import com.lichuan.tea.dto.FlashSaleSaveRequest;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.FlashSale;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FlashSaleRepository;
import com.lichuan.tea.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class FlashSaleService {

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<FlashSale> listActive() {
        return flashSaleRepository.findActiveList(LocalDateTime.now());
    }

    public List<FlashSale> listManage(User currentUser) {
        ensureAdmin(currentUser);
        return flashSaleRepository.findAllByOrderByStartTimeDescIdDesc();
    }

    public PageResult<FlashSale> getManagePage(
            User currentUser,
            String productName,
            String status,
            int pageNum,
            int pageSize) {
        ensureAdmin(currentUser);

        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }

        String normalizedStatus = trimToNull(status);
        if (normalizedStatus != null) {
            normalizedStatus = normalizedStatus.toUpperCase(Locale.ROOT);
        }

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.DESC, "createTime");
        Page<FlashSale> page = flashSaleRepository.findManagePage(
                trimToNull(productName),
                normalizedStatus,
                LocalDateTime.now(),
                pageable
        );
        return new PageResult<>(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public FlashSale create(User currentUser, FlashSaleSaveRequest request) {
        ensureAdmin(currentUser);
        validateRequest(request);

        Long productId = Objects.requireNonNull(request.getProductId(), "productId");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        validateSeckillPrice(product, request.getSeckillPrice());

        FlashSale entity = new FlashSale();
        entity.setProduct(product);
        entity.setSeckillPrice(request.getSeckillPrice());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return flashSaleRepository.save(entity);
    }

    public FlashSale update(User currentUser, Long id, FlashSaleSaveRequest request) {
        ensureAdmin(currentUser);
        validateRequest(request);

        Long nonNullId = Objects.requireNonNull(id, "id");
        FlashSale entity = flashSaleRepository.findById(nonNullId)
                .orElseThrow(() -> new RuntimeException("Flash sale record not found"));

        Long productId = Objects.requireNonNull(request.getProductId(), "productId");
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        validateSeckillPrice(product, request.getSeckillPrice());

        entity.setProduct(product);
        entity.setSeckillPrice(request.getSeckillPrice());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setEnabled(request.getEnabled() == null || request.getEnabled());
        entity.setUpdateTime(LocalDateTime.now());
        return flashSaleRepository.save(entity);
    }

    public void delete(User currentUser, Long id) {
        ensureAdmin(currentUser);
        Long nonNullId = Objects.requireNonNull(id, "id");
        if (!flashSaleRepository.existsById(nonNullId)) {
            throw new RuntimeException("Flash sale record not found");
        }
        flashSaleRepository.deleteById(nonNullId);
    }

    private void validateRequest(FlashSaleSaveRequest request) {
        if (request == null) {
            throw new RuntimeException("Invalid request");
        }
        if (request.getProductId() == null) {
            throw new RuntimeException("Product is required");
        }
        if (request.getSeckillPrice() == null || request.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Seckill price must be greater than 0");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new RuntimeException("End time must be later than start time");
        }
    }

    private void validateSeckillPrice(Product product, BigDecimal seckillPrice) {
        if (product.getPrice() == null) {
            throw new RuntimeException("Product original price is missing");
        }
        if (seckillPrice.compareTo(product.getPrice()) >= 0) {
            throw new RuntimeException("Seckill price must be lower than product original price");
        }
    }

    private void ensureAdmin(User currentUser) {
        if (currentUser == null) {
            throw new RuntimeException("Please login first");
        }
        if (!"ADMIN".equals(currentUser.getRole())) {
            throw new RuntimeException("Only admin can manage flash sales");
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
