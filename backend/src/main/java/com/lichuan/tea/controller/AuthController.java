package com.lichuan.tea.controller;

import com.lichuan.tea.common.Result;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        User user = userRepository.findByUsername(loginUser.getUsername());
        if (user == null || !user.getPassword().equals(loginUser.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock-token-" + user.getId());
        data.put("user", user);
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        user.setRole("USER");
        return Result.success(userRepository.save(user));
    }
}
