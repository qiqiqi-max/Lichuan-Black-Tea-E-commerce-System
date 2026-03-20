package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.dto.RegisterRequest;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") // Standardized path
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginRequest) {
        try {
            Map<String, Object> data = authService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
