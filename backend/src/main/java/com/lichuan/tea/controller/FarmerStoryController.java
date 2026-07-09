package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.FarmerStorySaveRequest;
import com.lichuan.tea.dto.FarmerStoryStatusUpdateRequest;
import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.entity.FarmerStory;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import com.lichuan.tea.service.FarmerStoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
@RequestMapping("/api/farmer-stories")
public class FarmerStoryController {

    private static final Logger log = LoggerFactory.getLogger(FarmerStoryController.class);
    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;
    private static final Path IMAGE_UPLOAD_DIR = resolvePrimaryUploadDir();
    private static final List<Path> IMAGE_SEARCH_DIRS = resolveImageSearchDirs();
    private static final List<Path> FALLBACK_IMAGE_CANDIDATES = List.of(
            Paths.get("frontend", "public", "images", "farmers", "farmer1.1.jpg").toAbsolutePath().normalize(),
            Paths.get("..", "frontend", "public", "images", "farmers", "farmer1.1.jpg").toAbsolutePath().normalize(),
            Paths.get("public", "images", "farmers", "farmer1.1.jpg").toAbsolutePath().normalize()
    );

    @Autowired
    private FarmerStoryService farmerStoryService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/active")
    public Result<List<FarmerStory>> listActive() {
        return Result.success(farmerStoryService.listActive());
    }

    @GetMapping("/{id}")
    public Result<FarmerStory> getDetail(@PathVariable Long id) {
        try {
            return Result.success(farmerStoryService.getActiveDetail(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/manage-page")
    public Result<PageResult<FarmerStory>> managePage(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String farmerName,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            return Result.success(farmerStoryService.getManagePage(getCurrentUser(userId), farmerName, region, status, pageNum, pageSize));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<FarmerStory> create(
            @RequestBody FarmerStorySaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(farmerStoryService.create(getCurrentUser(userId), request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<FarmerStory> update(
            @PathVariable Long id,
            @RequestBody FarmerStorySaveRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(farmerStoryService.update(getCurrentUser(userId), id, request));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<FarmerStory> updateStatus(
            @PathVariable Long id,
            @RequestBody FarmerStoryStatusUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            return Result.success(farmerStoryService.updateStatus(getCurrentUser(userId), id, request.getStatus()));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            farmerStoryService.delete(getCurrentUser(userId), id);
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
            return Result.success("/api/farmer-stories/image/" + fileName);
        } catch (IOException e) {
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
            log.error("Read farmer story image failed, fileName={}", fileName, e);
            return fallbackImageResponse(fileName);
        }
    }

    private ResponseEntity<Resource> fallbackImageResponse(String fileName) {
        for (Path fallback : FALLBACK_IMAGE_CANDIDATES) {
            if (Files.exists(fallback) && Files.isReadable(fallback)) {
                log.debug("Farmer story image not found, use fallback. fileName={}, fallback={}", fileName, fallback);
                try {
                    return buildImageResponse(fallback);
                } catch (IOException e) {
                    log.error("Read fallback farmer story image failed, fallback={}", fallback, e);
                }
            }
        }
        log.warn("Farmer story image not found and fallback unavailable. fileName={}", fileName);
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
            return current.resolve(Paths.get("uploads", "farmer-stories")).normalize();
        }
        Path backendDir = current.resolve("backend").normalize();
        if (Files.exists(backendDir.resolve("pom.xml"))) {
            return backendDir.resolve(Paths.get("uploads", "farmer-stories")).normalize();
        }
        return current.resolve(Paths.get("uploads", "farmer-stories")).normalize();
    }

    private static List<Path> resolveImageSearchDirs() {
        LinkedHashSet<Path> dirs = new LinkedHashSet<>();
        dirs.add(IMAGE_UPLOAD_DIR);
        dirs.add(Paths.get("uploads", "farmer-stories").toAbsolutePath().normalize());
        dirs.add(Paths.get("backend", "uploads", "farmer-stories").toAbsolutePath().normalize());
        dirs.add(Paths.get("..", "backend", "uploads", "farmer-stories").toAbsolutePath().normalize());
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
}
