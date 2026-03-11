package com.coldchain.guardian.app.service.impl;

import com.coldchain.guardian.app.security.JwtUtil;
import com.coldchain.guardian.app.service.WxAuthService;
import com.coldchain.guardian.app.service.WxLoginResult;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
import com.coldchain.guardian.contract.dto.auth.WxLoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.WxLoginResponseDto;
import com.coldchain.guardian.contract.dto.auth.WxManualLoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.WxUserInfoUpdateDto;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 微信登录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthServiceImpl implements WxAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wx.miniprogram.appid:}")
    private String appid;

    @Value("${wx.miniprogram.secret:}")
    private String secret;

    /**
     * 微信登录实现
     * @param request 微信登录请求
     * @return 登录响应
     */
    @Override
    @Transactional
    public WxLoginResponseDto wxLogin(WxLoginRequestDto request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录凭证(code)不能为空");
        }

        // 1. 通过code换取session_key和openid
        String sessionUrl = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid={appid}&secret={secret}&js_code={js_code}&grant_type=authorization_code";

        try {
            // 发送HTTP请求到微信服务器
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

            String url = sessionUrl
                    .replace("{appid}", appid)
                    .replace("{secret}", secret)
                    .replace("{js_code}", request.getCode());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String responseBody = response.getBody();
            log.debug("微信登录接口返回: {}", responseBody);

            // 解析微信返回的JSON数据
            WxLoginResult loginResult = objectMapper.readValue(responseBody, WxLoginResult.class);

            if (loginResult.getErrcode() != null && loginResult.getErrcode() != 0) {
                throw new BusinessException(ErrorCode.AUTH_FAILED, "微信登录失败：" + loginResult.getErrmsg());
            }

            // 2. 根据openid查找或创建用户
            UserEntity user = findOrCreateUserByOpenId(loginResult.getOpenid(), loginResult.getUnionid());

            // 3. 生成JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            // 4. 构建响应
            WxLoginResponseDto wxLoginResponse = new WxLoginResponseDto();
            wxLoginResponse.setToken(token);
            wxLoginResponse.setUserId(user.getId());
            wxLoginResponse.setUsername(user.getUsername());
            wxLoginResponse.setRealName(user.getRealName());
            wxLoginResponse.setRole(user.getRole());
            wxLoginResponse.setAvatar(user.getWxAvatar());

            // 判断是否为首次登录（根据用户是否已经有完整信息）
            boolean isFirstLogin = user.getRealName() == null || user.getRealName().startsWith("微信用户_");
            wxLoginResponse.setIsFirstLogin(isFirstLogin);

            boolean isProfileComplete = user.getRealName() != null && user.getPhone() != null;
            wxLoginResponse.setIsProfileComplete(isProfileComplete);

            log.info("微信登录成功，用户ID: {}, OpenID: {}", user.getId(), user.getOpenId());

            return wxLoginResponse;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new BusinessException(ErrorCode.AUTH_FAILED, "微信登录失败");
        }
    }

    /**
     * 微信手动登录（通过手机号绑定）
     * @param request 手机号绑定登录请求
     * @return 登录响应
     */
    @Override
    @Transactional
    public WxLoginResponseDto wxManualLogin(WxManualLoginRequestDto request) {
        if (request.getLoginCode() == null || request.getLoginCode().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "登录凭证不能为空");
        }

        if (request.getPhone() == null || request.getPhone().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号不能为空");
        }

        // 1. 通过code换取session_key和openid
        String sessionUrl = "https://api.weixin.qq.com/sns/jscode2session?" +
                "appid={appid}&secret={secret}&js_code={js_code}&grant_type=authorization_code";

        try {
            // 发送HTTP请求到微信服务器
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

            String url = sessionUrl
                    .replace("{appid}", appid)
                    .replace("{secret}", secret)
                    .replace("{js_code}", request.getLoginCode());

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            String responseBody = response.getBody();
            log.debug("微信登录接口返回: {}", responseBody);

            // 解析微信返回的JSON数据
            WxLoginResult loginResult = objectMapper.readValue(responseBody, WxLoginResult.class);

            if (loginResult.getErrcode() != null && loginResult.getErrcode() != 0) {
                throw new BusinessException(ErrorCode.AUTH_FAILED, "微信登录失败：" + loginResult.getErrmsg());
            }

            // 2. 根据手机号查找用户
            UserEntity user = userMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                    .eq(UserEntity::getPhone, request.getPhone())
            );

            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "未找到该手机号对应的员工档案");
            }

            // 3. 检查该手机号是否已被其他微信账号绑定
            if (user.getOpenId() != null && !user.getOpenId().equals(loginResult.getOpenid())) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该手机号已被其他微信账号绑定，请联系管理员");
            }

            // 4. 绑定当前微信账号到用户
            user.setOpenId(loginResult.getOpenid());
            // 注意：这里不再设置unionId，因为我们已经将其标记为不存在
            user.setUpdateTime(LocalDateTime.now());

            userMapper.updateById(user);

            // 5. 检查用户状态
            if (user.getStatus() != 1) {
                throw new BusinessException(ErrorCode.ACCOUNT_DISABLED, "账号已被禁用，请联系管理员");
            }

            // 6. 生成JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            // 7. 构建响应
            WxLoginResponseDto wxLoginResponse = new WxLoginResponseDto();
            wxLoginResponse.setToken(token);
            wxLoginResponse.setUserId(user.getId());
            wxLoginResponse.setUsername(user.getUsername());
            wxLoginResponse.setRealName(user.getRealName());
            wxLoginResponse.setRole(user.getRole());
            wxLoginResponse.setAvatar(user.getWxAvatar());

            boolean isProfileComplete = user.getRealName() != null && user.getPhone() != null;
            wxLoginResponse.setIsProfileComplete(isProfileComplete);
            wxLoginResponse.setIsFirstLogin(false); // 手动绑定不视为首次登录

            log.info("微信手动登录成功，用户ID: {}, OpenID: {}, Phone: {}", user.getId(), user.getOpenId(), user.getPhone());

            return wxLoginResponse;
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("微信手动登录异常", e);
            throw new BusinessException(ErrorCode.AUTH_FAILED, "微信登录失败");
        }
    }

    /**
     * 更新微信用户信息
     * @param openId 微信openId
     * @param userInfoUpdateDto 用户信息更新DTO
     * @return 是否成功
     */
    @Override
    public boolean updateWxUserInfo(String openId, WxUserInfoUpdateDto userInfoUpdateDto) {
        UserEntity user = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getOpenId, openId)
        );

        if (user == null) {
            return false;
        }

        // 根据DTO更新用户信息
        if (userInfoUpdateDto.getRealName() != null) {
            user.setRealName(userInfoUpdateDto.getRealName());
        }
        if (userInfoUpdateDto.getPhone() != null) {
            user.setPhone(userInfoUpdateDto.getPhone());
        }
        if (userInfoUpdateDto.getNickname() != null) {
            user.setWxNickname(userInfoUpdateDto.getNickname());
        }
        if (userInfoUpdateDto.getAvatar() != null) {
            user.setWxAvatar(userInfoUpdateDto.getAvatar());
        }
        if (userInfoUpdateDto.getEmail() != null) {
            user.setEmail(userInfoUpdateDto.getEmail());
        }
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    /**
     * 绑定微信账号到系统用户
     * @param userId 系统用户ID
     * @param openId 微信openId
     * @return 是否成功
     */
    @Override
    public boolean bindWxAccount(Long userId, String openId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 检查该openId是否已经被其他账号绑定
        UserEntity existingUser = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getOpenId, openId)
        );
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "该微信账号已被其他用户绑定");
        }

        user.setOpenId(openId);
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    /**
     * 解绑微信账号
     * @param userId 系统用户ID
     * @return 是否成功
     */
    @Override
    public boolean unbindWxAccount(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setOpenId(null);
        user.setUnionId(null);  // 仍然设置为null以防万一
        user.setWxNickname(null);
        user.setWxAvatar(null);
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    /**
     * 根据openId获取系统用户ID
     * @param openId 微信openId
     * @return 系统用户ID
     */
    @Override
    public Long getUserIdByOpenId(String openId) {
        UserEntity user = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getOpenId, openId)
        );

        return user != null ? user.getId() : null;
    }

    /**
     * 根据openId查找或创建用户
     * @param openId 微信openId
     * @param unionId 微信unionId
     * @return 用户实体
     */
    private UserEntity findOrCreateUserByOpenId(String openId, String unionId) {
        UserEntity user = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getOpenId, openId)
        );

        if (user == null) {
            // 创建新用户
            user = new UserEntity();
            user.setOpenId(openId);
            // 注意：这里不再设置unionId，因为我们已经将其标记为不存在
            user.setUsername("wx_user_" + openId.substring(openId.length() - 8)); // 自动生成用户名
            user.setRealName("微信用户_" + openId.substring(openId.length() - 6));
            user.setRole("EMPLOYEE"); // 微信用户默认角色为员工
            user.setStatus(1); // 默认启用
            // 设置一个默认密码（虽然是微信登录，但系统中仍需存储）
            user.setPassword("$2a$10$default_password_for_wx_users"); // BCrypt加密后的默认密码

            userMapper.insert(user);

            log.info("创建新微信用户，ID: {}, OpenID: {}", user.getId(), user.getOpenId());
        } else {
            // 更新用户最后登录时间
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        return user;
    }
}