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
}
