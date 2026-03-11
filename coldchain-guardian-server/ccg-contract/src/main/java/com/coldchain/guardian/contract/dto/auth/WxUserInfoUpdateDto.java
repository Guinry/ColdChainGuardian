package com.coldchain.guardian.contract.dto.auth;

import lombok.Data;

/**
 * 微信用户信息更新DTO
 */
@Data
public class WxUserInfoUpdateDto {
    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 微信昵称
     */
    private String nickname;

    /**
     * 微信头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;
}