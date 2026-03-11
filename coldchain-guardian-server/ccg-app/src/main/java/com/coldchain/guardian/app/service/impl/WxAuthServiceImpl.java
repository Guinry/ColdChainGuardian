package com.coldchain.guardian.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coldchain.guardian.app.security.JwtUtil;
import com.coldchain.guardian.app.service.WxAuthService;
import com.coldchain.guardian.app.service.WxLoginResult;
import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;
// 🌟 核心修复：一次性导入 auth 目录下所有的 DTO，防止报错
import com.coldchain.guardian.contract.dto.auth.*;
import com.coldchain.guardian.infra.persistence.entity.UserEntity;
import com.coldchain.guardian.infra.persistence.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WxAuthServiceImpl implements WxAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${wx.miniprogram.appid}")
    private String appId;

    @Value("${wx.miniprogram.secret}")
    private String appSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginResponseDto wxLogin(WxLoginRequestDto request) {
        WxLoginResult wxResult = code2Session(request.getCode());
        if (wxResult == null || !StringUtils.hasText(wxResult.getOpenid())) {
            throw new BusinessException(ErrorCode.AUTH_FAILED, "微信登录失败，无法获取 openId");
        }

        UserEntity user = findOrCreateUserByOpenId(wxResult.getOpenid(), wxResult.getUnionid());

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该账号已被停用");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
        return convertToWxLoginResponse(user, token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WxLoginResponseDto wxManualLogin(WxManualLoginRequestDto request) {
        WxLoginResult wxResult = code2Session(request.getLoginCode());
        if (wxResult == null || !StringUtils.hasText(wxResult.getOpenid())) {
            throw new BusinessException(ErrorCode.AUTH_FAILED, "获取微信 openId 失败");
        }

        String openId = wxResult.getOpenid();

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, request.getPhone());
        UserEntity user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该手机号对应的员工档案，请联系管理员");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该账号已被停用");
        }

        if (StringUtils.hasText(user.getOpenId()) && !user.getOpenId().equals(openId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该手机号已被其他微信绑定，请联系管理员解绑");
        }

        if (!StringUtils.hasText(user.getOpenId())) {
            user.setOpenId(openId);
            if (StringUtils.hasText(wxResult.getUnionid())) {
                user.setUnionId(wxResult.getUnionid());
            }
            userMapper.updateById(user);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        // 返回WxLoginResponseDto
        WxLoginResponseDto response = new WxLoginResponseDto();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setAvatar(user.getWxAvatar());
        response.setIsFirstLogin(false);
        response.setIsProfileComplete(user.getRealName() != null && user.getPhone() != null);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDto loginManual(WxManualLoginRequestDto request) {
        WxLoginResult wxResult = code2Session(request.getLoginCode());
        if (wxResult == null || !StringUtils.hasText(wxResult.getOpenid())) {
            throw new BusinessException(ErrorCode.AUTH_FAILED, "获取微信 openId 失败");
        }

        String openId = wxResult.getOpenid();

        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getPhone, request.getPhone());
        UserEntity user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到该手机号对应的员工档案，请联系管理员");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该账号已被停用");
        }

        if (StringUtils.hasText(user.getOpenId()) && !user.getOpenId().equals(openId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该手机号已被其他微信绑定，请联系管理员解绑");
        }

        if (!StringUtils.hasText(user.getOpenId())) {
            user.setOpenId(openId);
            if (StringUtils.hasText(wxResult.getUnionid())) {
                user.setUnionId(wxResult.getUnionid());
            }
            userMapper.updateById(user);
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setToken(token);
        responseDto.setUserId(user.getId());
        responseDto.setUsername(user.getUsername());
        responseDto.setRole(user.getRole());
        responseDto.setRealName(user.getRealName());

        return responseDto;
    }

    @Override
    public WxLoginResponseDto updateUserInfo(WxUserInfoUpdateDto request) {
        // 更新微信用户信息的预留方法
        return null;
    }

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

        int rows = userMapper.updateById(user);
        return rows > 0;
    }

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

        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    @Override
    public boolean unbindWxAccount(Long userId) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setOpenId(null);
        user.setUnionId(null);
        user.setWxNickname(null);
        user.setWxAvatar(null);

        int rows = userMapper.updateById(user);
        return rows > 0;
    }

    @Override
    public Long getUserIdByOpenId(String openId) {
        UserEntity user = userMapper.selectOne(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getOpenId, openId)
        );

        return user != null ? user.getId() : null;
    }

    // ================== 下面是私有工具方法 ==================

    /**
     * 🌟 修复：补充了确实的方法 findOrCreateUserByOpenId
     */
    private UserEntity findOrCreateUserByOpenId(String openId, String unionId) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getOpenId, openId);
        UserEntity user = userMapper.selectOne(wrapper);

        if (user == null) {
            // 如果查不到人，说明没用手机号绑定过，直接抛出异常拦截
            throw new BusinessException(ErrorCode.AUTH_FAILED, "尚未绑定员工账号，请首次登录时输入手机号绑定");
        }
        return user;
    }

    /**
     * 🌟 修复：补充了数据转换方法
     */
    private WxLoginResponseDto convertToWxLoginResponse(UserEntity user, String token) {
        WxLoginResponseDto response = new WxLoginResponseDto();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setAvatar(user.getWxAvatar());
        response.setIsFirstLogin(user.getRealName() != null && user.getRealName().startsWith("微信用户_"));
        response.setIsProfileComplete(user.getRealName() != null && user.getPhone() != null);
        return response;
    }

    /**
     * 调用微信 auth.code2Session 接口
     */
    private WxLoginResult code2Session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("secret", appSecret);
        params.put("code", code);

        try {
            // 🌟 核心修复：将返回值指定为 String.class，强制接收无论何种格式的数据
            String responseString = restTemplate.getForObject(url, String.class, params);

            if (!StringUtils.hasText(responseString)) {
                return null;
            }

            // 🌟 手动利用 Jackson 将 JSON 字符串解析为对象
            ObjectMapper objectMapper = new ObjectMapper();
            WxLoginResult result = objectMapper.readValue(responseString, WxLoginResult.class);

            // 顺便打印一下，方便排查微信返回了什么错误码（如 40029 code 无效等）
            if (result.getErrcode() != null && result.getErrcode() != 0) {
                log.error("微信接口返回错误: errcode={}, errmsg={}", result.getErrcode(), result.getErrmsg());
            }

            return result;

        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            return null;
        }
    }
}