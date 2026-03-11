package com.coldchain.guardian.app.service;

import com.coldchain.guardian.contract.dto.auth.WxLoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.WxLoginResponseDto;
import com.coldchain.guardian.contract.dto.auth.WxManualLoginRequestDto;
import com.coldchain.guardian.contract.dto.auth.WxUserInfoUpdateDto;

/**
 * 微信认证服务接口
 */
public interface WxAuthService {
    /**
     * 微信登录
     * @param request 微信登录请求
     * @return 登录响应
     */
    WxLoginResponseDto wxLogin(WxLoginRequestDto request);

    /**
     * 微信手动登录（通过手机号绑定）
     * @param request 手机号绑定登录请求
     * @return 登录响应
     */
    WxLoginResponseDto wxManualLogin(WxManualLoginRequestDto request);

    /**
     * 更新微信用户信息
     * @param openId 微信openId
     * @param userInfoUpdateDto 用户信息更新DTO
     * @return 是否成功
     */
    boolean updateWxUserInfo(String openId, WxUserInfoUpdateDto userInfoUpdateDto);

    /**
     * 绑定微信账号到系统用户
     * @param userId 系统用户ID
     * @param openId 微信openId
     * @return 是否成功
     */
    boolean bindWxAccount(Long userId, String openId);

    /**
     * 解绑微信账号
     * @param userId 系统用户ID
     * @return 是否成功
     */
    boolean unbindWxAccount(Long userId);

    /**
     * 根据openId获取系统用户ID
     * @param openId 微信openId
     * @return 系统用户ID
     */
    Long getUserIdByOpenId(String openId);
}