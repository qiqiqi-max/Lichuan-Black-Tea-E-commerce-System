package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.Product;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;
    private static final Path IMAGE_UPLOAD_DIR = resolvePrimaryUploadDir();
    private static final List<Path> IMAGE_SEARCH_DIRS = resolveImageSearchDirs();
    private static final List<Path> FALLBACK_IMAGE_CANDIDATES = List.of(
            Paths.get("frontend", "public", "images", "products", "product1.jpg").toAbsolutePath().normalize(),
            Paths.get("..", "frontend", "public", "images", "products", "product1.jpg").toAbsolutePath().normalize(),
            Paths.get("public", "images", "products", "product1.jpg").toAbsolutePath().normalize()
    );

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    // A simple way to get current user, should be replaced with Spring Security in production
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

    @GetMapping
    public Result<List<Product>> list(
            @RequestParam(required = false) String search,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Result.success(productService.list(search));
    }

    @GetMapping("/search")
    public Result<Page<Product>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return Result.success(productService.search(keyword, page, size));
    }

    @GetMapping("/manage-page")
    public Result<PageResult<Product>> managePage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String farmerName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        try {
            return Result.success(productService.getManagePage(name, origin, farmerName, pageNum, pageSize, currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        log.info("Request getDetail id: {}", id);
        Product product = productService.getDetail(id);
        log.info("Result getDetail product: {}", product);
        if (product == null) {
            log.warn("Product not found for id: {}", id);
            return Result.error("Product not found or removed");
        }
        return Result.success(product);
    }

    @PostMapping
    public Result<Product> create(
            @RequestBody Product product,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        try {
            return Result.success(productService.create(product, currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Product> update(
            @PathVariable Long id,
            @RequestBody Product product,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        try {
            return Result.success(productService.update(id, product, currentUser));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        User currentUser = getCurrentUser(userId);
        try {
            productService.delete(id, currentUser);
            return Result.success("Deleted");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("Please select an image");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            return Result.error("Image size cannot exceed 5MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXT.contains(extension)) {
            return Result.error("Only jpg, jpeg, png, webp are supported");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("Invalid image file");
        }

        try {
            Files.createDirectories(IMAGE_UPLOAD_DIR);
            String fileName = UUID.randomUUID() + "." + extension;
            Path target = IMAGE_UPLOAD_DIR.resolve(fileName).normalize();
            if (!target.startsWith(IMAGE_UPLOAD_DIR)) {
                return Result.error("Invalid upload path");
            }
            File targetFile = Objects.requireNonNull(target.toFile(), "targetFile");
            file.transferTo(targetFile);
            return Result.success("/api/products/image/" + fileName);
        } catch (IOException e) {
            log.error("Upload product image failed", e);
            return Result.error("Upload failed, please retry");
        }
    }

    @GetMapping("/image/{fileName:.+}")
    public ResponseEntity<Resource> viewImage(@PathVariable String fileName) {
        try {
            if (!StringUtils.hasText(fileName)) {
                return fallbackImageResponse(fileName);
            }

            Path file = resolveExistingImagePath(fileName);
            if (file == null) {
                return fallbackImageResponse(fileName);
            }

            return buildImageResponse(file);
        } catch (Exception e) {
            log.error("Read product image failed, fileName={}", fileName, e);
            return fallbackImageResponse(fileName);
        }
    }

    private ResponseEntity<Resource> fallbackImageResponse(String fileName) {
        for (Path fallback : FALLBACK_IMAGE_CANDIDATES) {
            if (Files.exists(fallback) && Files.isReadable(fallback)) {
                log.debug("Product image not found, use fallback. fileName={}, fallback={}", fileName, fallback);
                try {
                    return buildImageResponse(fallback);
                } catch (IOException e) {
                    log.error("Read fallback product image failed, fallback={}", fallback, e);
                }
            }
        }
        log.warn("Product image not found and fallback unavailable. fileName={}", fileName);
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Resource> buildImageResponse(Path file) throws IOException {
        URI fileUri = Objects.requireNonNull(file.toUri(), "fileUri");
        Resource resource = new UrlResource(fileUri);
        String contentType = Files.probeContentType(file);
        MediaType mediaType = contentType != null
                ? Objects.requireNonNull(MediaType.parseMediaType(contentType), "mediaType")
                : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok().contentType(Objects.requireNonNull(mediaType, "mediaType")).body(resource);
    }

    private static Path resolvePrimaryUploadDir() {
        Path current = Paths.get("").toAbsolutePath().normalize();
        String name = current.getFileName() != null ? current.getFileName().toString() : "";
        if ("backend".equalsIgnoreCase(name)) {
            return current.resolve(Paths.get("uploads", "product-images")).normalize();
        }
        Path backendDir = current.resolve("backend").normalize();
        if (Files.exists(backendDir.resolve("pom.xml"))) {
            return backendDir.resolve(Paths.get("uploads", "product-images")).normalize();
        }
        return current.resolve(Paths.get("uploads", "product-images")).normalize();
    }

    private static List<Path> resolveImageSearchDirs() {
        LinkedHashSet<Path> dirs = new LinkedHashSet<>();
        dirs.add(IMAGE_UPLOAD_DIR);
        dirs.add(Paths.get("uploads", "product-images").toAbsolutePath().normalize());
        dirs.add(Paths.get("backend", "uploads", "product-images").toAbsolutePath().normalize());
        dirs.add(Paths.get("..", "backend", "uploads", "product-images").toAbsolutePath().normalize());
        return new ArrayList<>(dirs);
    }

    private Path resolveExistingImagePath(String fileName) {
        for (Path dir : IMAGE_SEARCH_DIRS) {
            Path candidate = dir.resolve(fileName).normalize();
            if (!candidate.startsWith(dir)) {
                continue;
            }
            if (Files.exists(candidate) && Files.isReadable(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String getFileExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase();
    }
}
