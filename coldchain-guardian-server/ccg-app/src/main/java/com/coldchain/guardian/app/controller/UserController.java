package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.security.JwtUtil;
import com.coldchain.guardian.app.service.AuthService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.user.CurrentUserInfoResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "处理用户相关信息接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息和状态")
    @GetMapping("/me")
    public ApiResponse<CurrentUserInfoResponseDto> getCurrentUserInfo(HttpServletRequest request) {
        // 从请求头中提取JWT token
        String token = jwtUtil.getTokenFromRequest(request);

        if (token == null || token.isEmpty()) {
            return ApiResponse.error("未提供认证令牌");
        }

        try {
            // 解析token获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 根据用户ID获取用户信息
            CurrentUserInfoResponseDto userInfo = authService.getCurrentUserInfo(userId);

            return ApiResponse.success(userInfo);
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败: " + e.getMessage());
        }
    }
}