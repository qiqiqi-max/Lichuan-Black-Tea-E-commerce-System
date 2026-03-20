package com.lichuan.tea.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.lichuan.tea.entity.User;
import com.lichuan.tea.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        token = token.substring(7);

        try {
            String userId = JWT.decode(token).getAudience().get(0);
            User user = userMapper.selectById(Long.parseLong(userId));
            if (user == null) {
                response.setStatus(401);
                return false;
            }
            // Verify token
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            jwtVerifier.verify(token);

            // Set user info in request attribute
            request.setAttribute("currentUser", user);

        } catch (JWTVerificationException e) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
