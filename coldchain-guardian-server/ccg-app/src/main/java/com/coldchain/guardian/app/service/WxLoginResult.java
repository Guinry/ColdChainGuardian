package com.coldchain.guardian.app.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 微信登录响应数据类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class WxLoginResult {
    private String openid;        // 用户唯一标识
    private String sessionKey;    // 会话密钥
    private String unionid;       // 用户在开放平台的唯一标识符
    private Integer errcode;      // 错误码
    private String errmsg;        // 错误信息
}