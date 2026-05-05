package com.coldchain.guardian.contract.dto.auth;

import lombok.Data;

@Data
public class WxManualLoginRequestDto {
    /**
     * 微信临时登录凭证
     */
    private String loginCode;

    /**
     * 员工手动输入的手机号
     */
    private String phone;

    /**
     * 小程序运行平台。PC 调试器中 wx.getSystemInfoSync().platform 通常为 devtools。
     */
    private String platform;

    /**
     * 是否来自小程序开发工具。仅用于本地毕业设计演示/PC 模拟器调试。
     */
    private Boolean devtools;
}
