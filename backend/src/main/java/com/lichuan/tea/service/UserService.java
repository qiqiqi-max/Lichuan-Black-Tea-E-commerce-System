package com.lichuan.tea.service;

import com.lichuan.tea.dto.PageResult;
import com.lichuan.tea.dto.RegisterRequest;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.FarmerProfileRepository;
import com.lichuan.tea.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());

        String role = request.getRole();
        if ("ADMIN".equals(role) || "FARMER".equals(role)) {
            user.setRole(role);
        } else {
            user.setRole("USER");
        }

        User savedUser = userRepository.save(user);

        if ("FARMER".equals(savedUser.getRole())) {
            FarmerProfile farmerProfile = new FarmerProfile();
            farmerProfile.setUser(savedUser);
            farmerProfile.setRealName(savedUser.getNickname());
            farmerProfile.setOrigin("Lichuan");
            farmerProfileRepository.save(farmerProfile);
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public PageResult<User> findPage(String username, String nickname, String phone, String role, int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 20;
        }

        PageRequest pageable = PageRequest.of(pageNum - 1, pageSize, Sort.Direction.DESC, "id");
        Page<User> page = userRepository.findManagePage(
                trimToNull(username),
                trimToNull(nickname),
                trimToNull(phone),
                trimToNull(role),
                pageable
        );
        return new PageResult<>(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    public void createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        userRepository.save(user);
    }

    public void updateUser(User user) {
        Long userId = Objects.requireNonNull(user.getId(), "userId");
        User existing = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        existing.setNickname(user.getNickname());
        existing.setPhone(user.getPhone());
        existing.setRole(user.getRole());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(user.getPassword());
        }
        userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        Long nonNullId = Objects.requireNonNull(id, "id");
        userRepository.deleteById(nonNullId);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
