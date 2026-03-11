package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AuthService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.auth.LoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.LoginResponseDto;
import com.coldchain.guardian.contract.dto.user.CurrentUserInfoResponseDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.app.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的完整信息")
    public ApiResponse<CurrentUserInfoResponseDto> getCurrentUserInfo(HttpServletRequest request) {
        String token = jwtUtil.getTokenFromRequest(request);

        if (token == null || token.isEmpty()) {
            return ApiResponse.error("未提供认证令牌");
        }

        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            CurrentUserInfoResponseDto userInfo = authService.getCurrentUserInfo(userId);
            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<UserEntity> register(@Valid @RequestBody UserEntity user) {
        UserEntity registeredUser = authService.register(user);
        return ApiResponse.success(registeredUser);
    }

    /**
     * 检查用户是否存在
     */
    @GetMapping("/exists/{username}")
    public ApiResponse<Boolean> userExists(@PathVariable String username) {
        boolean exists = authService.userExists(username);
        return ApiResponse.success(exists);
    }
}