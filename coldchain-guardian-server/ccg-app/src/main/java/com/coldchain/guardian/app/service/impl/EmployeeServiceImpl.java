package com.coldchain.guardian.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.guardian.app.service.EmployeeService;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
import com.coldchain.guardian.contract.dto.user.EmployeeQueryDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 员工管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserMapper userMapper;

    @Override
    public PageResponse<UserEntity> getEmployeeList(EmployeeQueryDto queryDto, int pageNum, int pageSize) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        // 关键词查询（姓名或手机号）
        if (queryDto.getKeyword() != null && !queryDto.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(UserEntity::getRealName, queryDto.getKeyword())
                    .or()
                    .like(UserEntity::getPhone, queryDto.getKeyword()));
        }

        // 角色筛选
        if (queryDto.getRole() != null && !queryDto.getRole().isEmpty()) {
            wrapper.eq(UserEntity::getRole, queryDto.getRole());
        }

        // 状态筛选
        if (queryDto.getStatus() != null) {
            wrapper.eq(UserEntity::getStatus, queryDto.getStatus());
        }

        // 微信绑定状态筛选
        if (queryDto.getIsWechatBound() != null) {
            if (queryDto.getIsWechatBound()) {
                wrapper.isNotNull(UserEntity::getOpenId).and(w -> w.ne(UserEntity::getOpenId, ""));
            } else {
                wrapper.or(w -> w.isNull(UserEntity::getOpenId).or().eq(UserEntity::getOpenId, ""));
            }
        }

        // 排序
        wrapper.orderByDesc(UserEntity::getId);

        IPage<UserEntity> page = new Page<>(pageNum, pageSize);
        IPage<UserEntity> resultPage = userMapper.selectPage(page, wrapper);

        // 手动创建PageResponse
        return PageResponse.of(
            resultPage.getRecords(),
            resultPage.getTotal(),
            (int) resultPage.getCurrent(),
            (int) resultPage.getSize()
        );
    }

    @Override
    public UserEntity createEmployee(UserEntity employee) {
        // 检查手机号是否已存在
        UserEntity existingUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getPhone, employee.getPhone())
        );

        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该手机号已被占用");
        }

        // 设置默认值
        if (employee.getRole() == null) {
            employee.setRole("EMPLOYEE"); // 默认为员工角色
        }
        if (employee.getStatus() == null) {
            employee.setStatus(1); // 默认启用
        }

        // 设置默认密码（虽然微信登录不需要，但系统仍需存储）
        employee.setPassword("$2a$10$default_password_for_employee");

        userMapper.insert(employee);

        // 返回时不包含密码
        employee.setPassword(null);
        return employee;
    }

    @Override
    public UserEntity updateEmployee(UserEntity employee) {
        UserEntity existingUser = userMapper.selectById(employee.getId());
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查手机号是否被其他用户占用
        UserEntity phoneCheckUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getPhone, employee.getPhone())
                .ne(UserEntity::getId, employee.getId())
        );

        if (phoneCheckUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该手机号已被其他用户占用");
        }

        // 更新用户信息
        userMapper.updateById(employee);

        // 返回更新后的用户信息（不含密码）
        UserEntity updatedUser = userMapper.selectById(employee.getId());
        updatedUser.setPassword(null);
        return updatedUser;
    }

    @Override
    public boolean updateEmployeeStatus(Long userId, Integer status) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setStatus(status);
        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    @Override
    public boolean unbindEmployeeWechat(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 清除微信相关字段
        user.setOpenId(null);
        user.setUnionId(null);
        user.setWxNickname(null);
        user.setWxAvatar(null);

        int rows = userMapper.updateById(user);
        return rows > 0;
    }
}