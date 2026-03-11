package com.coldchain.guardian.contract.dto.user;

import lombok.Data;

/**
 * 员工更新DTO
 */
@Data
public class EmployeeUpdateDto {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态 (1正常, 0停用)
     */
    private Integer status;
}