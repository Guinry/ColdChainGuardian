package com.coldchain.guardian.contract.dto.auth;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 微信手动登录请求DTO - 用于手机号绑定场景
 */
@Data
public class WxManualLoginRequestDto {

    /**
     * 微信登录凭证code
     */
    @NotBlank(message = "登录凭证不能为空")
    private String loginCode;

    /**
     * 手机号 - 用于验证身份并绑定
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的11位手机号")
    private String phone;
}