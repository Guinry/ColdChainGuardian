// pages/login/login.js
import request from '../../utils/request';

Page({
  data: {
    phone: ''
  },

  onLoad() {
    this.checkAccountStatus();
  },

  // 🌟 核心：每次进入小程序，验证 Token 并检查账号是否可用
  checkAccountStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      wx.showLoading({
        title: '环境安全检测中'
      });

      // 调用后端获取当前用户信息的接口
      request({
        url: '/api/user/me', // TODO: 替换为你后端获取当前登录用户信息的真实接口
        method: 'GET'
      }).then(res => {
        wx.hideLoading();
        // 假设后端返回的数据里包含 status (1代表正常，0代表禁用)
        if (res.data.status === 1) {
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
    const {
      phone
    } = this.data;

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

    // 获取微信的临时凭证 code
    wx.login({
      success: (loginRes) => {
        if (loginRes.code) {
          this.sendAuthRequest(loginRes.code, phone);
        } else {
          wx.hideLoading();
          wx.showToast({
            title: '获取微信环境失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        wx.hideLoading();
        wx.showToast({
          title: '网络异常，请重试',
          icon: 'none'
        });
      }
    });
  },

  // 向后端发起绑定与登录请求
  sendAuthRequest(loginCode, phone) {
    request({
      url: '/api/wx/auth/login-manual', // TODO: 需要你在后端的 WxAuthController 写一个接收 phone 和 loginCode 的新接口
      method: 'POST',
      data: {
        loginCode: loginCode,
        phone: phone
      }
    }).then(res => {
      wx.hideLoading();

      // 保存认证信息
      wx.setStorageSync('token', res.data.token);
      wx.setStorageSync('userInfo', res.data.userInfo);

      wx.showToast({
        title: '绑定成功',
        icon: 'success'
      });

      setTimeout(() => {
        wx.switchTab({
          url: '/pages/workbench/workbench'
        });
      }, 1000);

    }).catch(err => {
      wx.hideLoading();
      wx.showModal({
        title: '认证失败',
        content: err.message || '未找到该员工档案或账号已停用，请核对手机号。',
        showCancel: false
      });
    });
  }
});