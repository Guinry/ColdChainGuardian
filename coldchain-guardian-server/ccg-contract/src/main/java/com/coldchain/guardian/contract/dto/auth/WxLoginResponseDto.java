package com.coldchain.guardian.contract.dto.auth;

import lombok.Data;

/**
 * 微信登录响应DTO
 */
@Data
public class WxLoginResponseDto {
    /**
     * JWT访问令牌
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户角色
     */
    private String role;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 是否首次登录（用于引导绑定流程）
     */
    private Boolean isFirstLogin;

    /**
     * 是否已完善个人信息
     */
    private Boolean isProfileComplete;
}