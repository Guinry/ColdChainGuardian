package com.coldchain.guardian.app.service;

import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.contract.dto.user.EmployeeQueryDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;

/**
 * 管理员管理服务接口
 */
public interface ManagerService {

    /**
     * 分页查询管理员列表
     *
     * @param queryDto 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResponse<UserEntity> getManagerList(EmployeeQueryDto queryDto, int pageNum, int pageSize);

    /**
     * 创建管理员
     *
     * @param manager 管理员信息
     * @return 创建的管理员
     * @throws BusinessException 业务异常
     */
    UserEntity createManager(UserEntity manager);

    /**
     * 更新管理员信息
     *
     * @param manager 管理员信息
     * @return 更新后的管理员
     * @throws BusinessException 业务异常
     */
    UserEntity updateManager(UserEntity manager);

    /**
     * 更新管理员状态
     *
     * @param userId 管理员ID
     * @param status 状态
     * @return 是否成功
     * @throws BusinessException 业务异常
     */
    boolean updateManagerStatus(Long userId, Integer status);

    /**
     * 解绑管理员微信账号
     *
     * @param userId 管理员ID
     * @return 是否成功
     * @throws BusinessException 业务异常
     */
    boolean unbindManagerWechat(Long userId);
}