package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.WxAuthService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.auth.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 微信认证控制器
 */
@Tag(name = "微信认证管理", description = "处理微信小程序登录相关接口")
@RestController
@RequestMapping("/api/wx/auth")
@RequiredArgsConstructor
public class WxAuthController {

    private final WxAuthService wxAuthService;

    /**
     * 微信登录接口
     *
     * @param request 微信登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "微信登录", description = "使用微信登录凭证进行登录")
    public ApiResponse<WxLoginResponseDto> wxLogin(@Valid @RequestBody WxLoginRequestDto request) {
        WxLoginResponseDto response = wxAuthService.wxLogin(request);
        return ApiResponse.success(response);
    }

    /**
     * 微信手动登录（通过手机号绑定）
     *
     * @param request 手机号绑定登录请求
     * @return 登录响应
     */
    @PostMapping("/login-manual")
    @Operation(summary = "微信手动登录", description = "通过输入手机号与微信环境绑定进行登录")
    public ApiResponse<WxLoginResponseDto> wxManualLogin(@Valid @RequestBody WxManualLoginRequestDto request) {
        WxLoginResponseDto response = wxAuthService.wxManualLogin(request);
        return ApiResponse.success(response);
    }

    /**
     * 微信手动登录（通过手机号绑定）- 返回标准登录响应
     *
     * @param request 手机号绑定登录请求
     * @return 标准登录响应
     */
    @PostMapping("/login-manual-standard")
    @Operation(summary = "微信手动登录（标准响应）", description = "通过输入手机号与微信环境绑定进行登录，返回标准登录响应格式")
    public ApiResponse<LoginResponseDto> loginManualStandard(@Valid @RequestBody WxManualLoginRequestDto request) {
        LoginResponseDto response = wxAuthService.loginManual(request);
        return ApiResponse.success(response);
    }

    /**
     * 更新微信用户信息
     */
    @Operation(summary = "更新微信用户信息", description = "更新当前微信用户的个人信息")
    @PutMapping("/user-info")
    public ApiResponse<Boolean> updateWxUserInfo(@RequestParam String openId, @Valid @RequestBody WxUserInfoUpdateDto userInfoUpdateDto) {
        Boolean result = wxAuthService.updateWxUserInfo(openId, userInfoUpdateDto);
        return ApiResponse.success(result);
    }

    /**
     * 绑定微信账号到系统用户
     */
    @Operation(summary = "绑定微信账号", description = "将微信账号绑定到指定的系统用户")
    @PostMapping("/bind-account")
    public ApiResponse<Boolean> bindWxAccount(@RequestParam Long userId, @RequestParam String openId) {
        Boolean result = wxAuthService.bindWxAccount(userId, openId);
        return ApiResponse.success(result);
    }

    /**
     * 解绑微信账号
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("/unbind/{userId}")
    @Operation(summary = "解绑微信账号", description = "解除用户与微信账号的绑定关系")
    public ApiResponse<Void> unbindWxAccount(@PathVariable Long userId) {
        boolean success = wxAuthService.unbindWxAccount(userId);
        return success ? ApiResponse.success(null) : ApiResponse.error("解绑失败");
    }

    /**
     * 获取用户ID通过OpenId
     *
     * @param openId 微信OpenId
     * @return 用户ID
     */
    @GetMapping("/user-id/{openId}")
    @Operation(summary = "获取用户ID", description = "根据微信OpenId获取系统用户ID")
    public ApiResponse<Long> getUserIdByOpenId(@PathVariable String openId) {
        Long userId = wxAuthService.getUserIdByOpenId(openId);
        return ApiResponse.success(userId);
    }
}