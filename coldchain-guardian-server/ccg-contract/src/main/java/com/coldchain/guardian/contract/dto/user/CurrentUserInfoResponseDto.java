package com.coldchain.guardian.contract.dto.user;

import lombok.Data;

/**
 * 当前用户信息响应DTO
 */
@Data
public class CurrentUserInfoResponseDto {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private String wxNickname;
    private String wxAvatar;
    private String openId;
}