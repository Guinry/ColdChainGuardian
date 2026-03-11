package com.coldchain.guardian.contract.dto.auth;

import lombok.Data;

/**
 * 微信登录请求DTO
 */
@Data
public class WxLoginRequestDto {
    /**
     * 微信登录凭证(code)，从小程序端获取
     */
    private String code;

    /**
     * 用户信息加密数据(可选)，包含敏感信息如手机号等
     */
    private String encryptedData;

    /**
     * 加密算法的初始向量(可选)
     */
    private String iv;

    /**
     * 用户签名(可选)，用于验证数据完整性
     */
    private String signature;
}