package com.coldchain.guardian.contract.dto.user;

import lombok.Data;

/**
 * 员工查询DTO
 */
@Data
public class EmployeeQueryDto {

    /**
     * 关键词（姓名或手机号模糊查询）
     */
    private String keyword;

    /**
     * 角色筛选
     */
    private String role;

    /**
     * 状态筛选 (1正常, 0停用)
     */
    private Integer status;

    /**
     * 是否已绑定微信 (true已绑定, false未绑定)
     */
    private Boolean isWechatBound;
}