package com.lichuan.tea.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lichuan.tea.dto.RegisterRequest;
import com.lichuan.tea.entity.FarmerProfile;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.mapper.FarmerProfileMapper;
import com.lichuan.tea.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FarmerProfileMapper farmerProfileMapper;

    @Transactional
    public User register(RegisterRequest request) {
        if (userMapper.selectOne(new QueryWrapper<User>().eq("username", request.getUsername())) != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.selectOne(new QueryWrapper<User>().eq("phone", request.getPhone())) != null) {
            throw new RuntimeException("手机号已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        // Remember to hash passwords in a real-world application!
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole().toUpperCase());
        user.setStatus(1);
        userMapper.insert(user);

        if ("FARMER".equals(user.getRole())) {
            FarmerProfile farmerProfile = new FarmerProfile();
            farmerProfile.setUserId(user.getId());
            farmerProfile.setAuditStatus("PENDING");
            farmerProfileMapper.insert(farmerProfile);
        }

        return user;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));

        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账户已被封禁");
        }

        String token = JWT.create()
                .withAudience(user.getId().toString())
                .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 hours
                .sign(Algorithm.HMAC256(user.getPassword()));

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", user);

        if ("FARMER".equals(user.getRole())) {
            FarmerProfile profile = farmerProfileMapper.selectOne(new QueryWrapper<FarmerProfile>().eq("user_id", user.getId()));
            response.put("farmerProfile", profile);
        }

        return response;
    }
}
