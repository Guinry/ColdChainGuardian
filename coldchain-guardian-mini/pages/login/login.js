// pages/login/login.js
import request from '../../utils/request';

Page({
  data: {
    phone: '',
    loading: false,
    isDevtools: false
  },

  getRuntimeInfo() {
    if (wx.getDeviceInfo && wx.getWindowInfo) {
      const deviceInfo = wx.getDeviceInfo();
      return {
        platform: deviceInfo.platform || '',
        model: deviceInfo.model || ''
      };
    }
    return wx.getSystemInfoSync ? wx.getSystemInfoSync() : {};
  },

  onLoad() {
    this.detectRuntime();
    this.checkAccountStatus();
  },

  detectRuntime() {
    try {
      const systemInfo = this.getRuntimeInfo();
      const isDevtools = systemInfo.platform === 'devtools';
      this.setData({
        isDevtools,
        phone: isDevtools && !this.data.phone ? '13188751661' : this.data.phone
      });
      console.log('[login runtime]', systemInfo.platform, systemInfo.model);
    } catch (error) {
      console.warn('[login runtime] 获取运行环境失败', error);
    }
  },

  // 🌟 核心：每次进入小程序，验证 Token 并检查账号是否可用
  checkAccountStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      wx.showLoading({
        title: '环境安全检测中'
      });

      request({
        url: '/api/user/me',
        method: 'GET'
      }).then(res => {
        wx.hideLoading();
        const user = res.data || {};
        if (user.status === 1) {
          wx.setStorageSync('userInfo', user);
          wx.switchTab({
            url: '/pages/workbench/workbench'
          });
        } else {
          wx.showModal({
            title: '账号已停用',
            content: '您的账号已被管理员禁用，请联系相关负责人。',
            showCancel: false
          });
          wx.clearStorageSync();
        }
      }).catch(() => {
        wx.hideLoading();
        // Token 失效或被踢下线，清空缓存留在登录页
        wx.clearStorageSync();
      });
    }
  },

  // 监听输入框变化
  onPhoneInput(e) {
    this.setData({
      phone: e.detail.value
    });
  },

  // 提交绑定并登录
  handleManualLogin() {
    const { phone, isDevtools } = this.data;

    // 简单的手机号正则验证
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({
        title: '请输入正确的11位手机号',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({
      title: '身份核验中...'
    });
    this.setData({ loading: true });

    if (isDevtools) {
      this.sendAuthRequest('devtools-mock-code', phone);
      return;
    }

    // 获取微信的临时凭证 code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          this.sendAuthRequest(loginRes.code, phone);
        } else {
          wx.hideLoading();
          this.setData({ loading: false });
          wx.showToast({
            title: '获取微信环境失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        this.setData({ loading: false });
        wx.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        });
      }
    });
  },

  // 向后端发起绑定与登录请求
  sendAuthRequest(loginCode, phone) {
    const systemInfo = this.getRuntimeInfo();
    const isDevtools = systemInfo.platform === 'devtools' || this.data.isDevtools;

    request({
      url: '/api/wx/auth/login-manual',
      method: 'POST',
      data: {
        loginCode: loginCode,
        phone: phone,
        platform: systemInfo.platform || '',
        devtools: isDevtools
      }
    }).then(res => {
      wx.hideLoading();
      this.setData({ loading: false });

      // 保存认证信息
      const loginData = res.data || {};
      wx.setStorageSync('token', loginData.token);
      wx.setStorageSync('userInfo', {
        id: loginData.userId,
        username: loginData.username,
        realName: loginData.realName,
        role: loginData.role,
        avatar: loginData.avatar,
        status: 1
      });

      wx.showToast({
        title: isDevtools ? '调试登录成功' : '绑定成功',
        icon: 'success'
      });

      setTimeout(() => {
        wx.switchTab({
          url: '/pages/workbench/workbench'
        });
      }, 1000);

    }).catch(err => {
      wx.hideLoading();
      this.setData({ loading: false });
      const message = err.message || err.msg || '未找到该员工档案或账号已停用，请核对手机号。';
      console.error('[login failed]', err);
      wx.showModal({
        title: '认证失败',
        content: message,
        showCancel: false
      });
    });
  }
});
