package com.coldchain.guardian.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coldchain.guardian.app.service.ManagerService;
import com.coldchain.guardian.common.api.PageResponse;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
import com.coldchain.guardian.contract.dto.user.EmployeeQueryDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理员管理服务实现类
 */
@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final UserMapper userMapper;

    @Override
    public PageResponse<UserEntity> getManagerList(EmployeeQueryDto queryDto, int pageNum, int pageSize) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();

        // 🌟 核心逻辑：只查询管理员和超级管理员角色
        wrapper.and(w -> w.eq(UserEntity::getRole, "ADMIN")
                         .or()
                         .eq(UserEntity::getRole, "SUPER_ADMIN"));

        // 关键词查询（姓名或手机号）
        if (StringUtils.hasText(queryDto.getKeyword())) {
            wrapper.and(w -> w.like(UserEntity::getRealName, queryDto.getKeyword())
                    .or()
                    .like(UserEntity::getPhone, queryDto.getKeyword()));
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
    public UserEntity createManager(UserEntity manager) {
        // 检查手机号是否已存在
        UserEntity existingUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getPhone, manager.getPhone())
        );

        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该手机号已被占用");
        }

        // 设置角色为管理员（强制设置）
        manager.setRole("ADMIN");

        // 设置默认值
        if (manager.getStatus() == null) {
            manager.setStatus(1); // 默认启用
        }

        // 设置默认密码
        manager.setPassword("$2a$10$default_password_for_admin");

        userMapper.insert(manager);

        // 返回时不包含密码
        manager.setPassword(null);
        return manager;
    }

    @Override
    public UserEntity updateManager(UserEntity manager) {
        UserEntity existingUser = userMapper.selectById(manager.getId());
        if (existingUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证该用户确实是管理员角色
        if (!"ADMIN".equals(existingUser.getRole()) && !"SUPER_ADMIN".equals(existingUser.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作非管理员账户");
        }

        // 检查手机号是否被其他用户占用
        UserEntity phoneCheckUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getPhone, manager.getPhone())
                .ne(UserEntity::getId, manager.getId())
        );

        if (phoneCheckUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "该手机号已被其他用户占用");
        }

        // 更新用户信息
        userMapper.updateById(manager);

        // 返回更新后的用户信息（不含密码）
        UserEntity updatedUser = userMapper.selectById(manager.getId());
        updatedUser.setPassword(null);
        return updatedUser;
    }

    @Override
    public boolean updateManagerStatus(Long userId, Integer status) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证该用户确实是管理员角色
        if (!"ADMIN".equals(user.getRole()) && !"SUPER_ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作非管理员账户");
        }

        user.setStatus(status);
        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    @Override
    public boolean unbindManagerWechat(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证该用户确实是管理员角色
        if (!"ADMIN".equals(user.getRole()) && !"SUPER_ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作非管理员账户");
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