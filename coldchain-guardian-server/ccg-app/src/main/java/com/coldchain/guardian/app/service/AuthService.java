package com.coldchain.guardian.app.service;

import com.coldchain.guardian.app.security.JwtUtil;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
import com.coldchain.guardian.contract.dto.auth.LoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.LoginResponseDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.repository.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        try {
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );

            // 设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            String username = authentication.getName();
            UserEntity user = userRepository.findByUsername(username);

            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
            }

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            // 构建响应
            LoginResponseDto response = new LoginResponseDto();
            response.setToken(token);
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());

            return response;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    /**
     * 用户注册
     */
    public UserEntity register(UserEntity user) {
        // 检查用户名是否已存在
        UserEntity existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        // 密码加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 设置默认值
        if (user.getRole() == null) {
            user.setRole("employee"); // 默认角色为员工
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用状态
        }

        // 保存用户
        userRepository.save(user);

        // 返回时不包含密码
        user.setPassword(null);
        return user;
    }

    /**
     * 检查用户是否存在
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username) != null;
    }
}