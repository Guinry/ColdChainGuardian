package com.coldchain.guardian.app.service;

import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.contract.dto.user.EmployeeQueryDto;
import com.coldchain.guardian.contract.dto.user.EmployeeUpdateDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;

/**
 * 员工管理服务接口
 */
public interface EmployeeService {

    /**
     * 分页查询员工列表
     * @param queryDto 查询条件
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<UserEntity> getEmployeeList(EmployeeQueryDto queryDto, int pageNum, int pageSize);

    /**
     * 创建员工
     * @param employee 员工信息
     * @return 创建后的员工信息
     */
    UserEntity createEmployee(UserEntity employee);

    /**
     * 更新员工信息
     * @param employee 员工信息
     * @return 更新后的员工信息
     */
    UserEntity updateEmployee(UserEntity employee);

    /**
     * 更新员工状态
     * @param userId 用户ID
     * @param status 状态 (1正常, 0停用)
     * @return 是否成功
     */
    boolean updateEmployeeStatus(Long userId, Integer status);

    /**
     * 解绑员工微信账号
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unbindEmployeeWechat(Long userId);
}