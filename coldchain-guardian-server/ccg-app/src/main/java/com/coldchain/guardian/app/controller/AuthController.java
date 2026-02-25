package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.AuthService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.auth.LoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.LoginResponseDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto);
        return ApiResponse.success(response);
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