package com.coldchain.guardian.app.service;

import com.coldchain.guardian.app.security.JwtUtil;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
import com.coldchain.guardian.contract.dto.auth.LoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.LoginResponseDto;
import com.coldchain.guardian.contract.dto.user.CurrentUserInfoResponseDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.mapper.UserMapper; // 更改为使用Mapper
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper; // 更改为使用Mapper

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        try {
            // 先从数据库获取用户信息
            UserEntity user = userMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                    .eq(UserEntity::getUsername, loginRequestDto.getUsername())
            );

            if (user == null) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }

            // 直接验证密码
            if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
                throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
            }

            // 检查用户状态
            if (user.getStatus() != 1) {
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
            }

            // 手动创建认证对象（因为上面跳过了AuthenticationManager）
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    null,
                    java.util.Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 生成JWT Token - 添加错误处理
            String token;
            try {
                token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
            } catch (Exception jwtException) {
                throw new BusinessException(ErrorCode.TOKEN_GENERATION_FAILED);
            }

            // 构建响应
            LoginResponseDto response = new LoginResponseDto();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());      // 设置角色信息
            response.setRealName(user.getRealName()); // 设置真实姓名

            return response;
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (RuntimeException e) {
            // 处理运行时异常
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误: " + e.getMessage());
        } catch (Exception e) {
            // JWT生成失败或其他系统错误
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    public CurrentUserInfoResponseDto getCurrentUserInfo(Long userId) {
        UserEntity user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用");
        }

        CurrentUserInfoResponseDto response = new CurrentUserInfoResponseDto();
        BeanUtils.copyProperties(user, response);

        return response;
    }

    /**
     * 用户注册
     */
    public UserEntity register(UserEntity user) {
        // 检查用户名是否已存在
        UserEntity existingUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, user.getUsername())
        );

        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 密码加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("USER"); // 默认角色为普通用户 (大写以匹配SecurityConfig)
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用状态
        }

        // 保存用户
        userMapper.insert(user);

        // 返回时不包含密码
        user.setPassword(null);
        return user;
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(String username) {
        UserEntity user = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUsername, username)
        );
        return user != null;
    }
}