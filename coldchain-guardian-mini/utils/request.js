// utils/request.js
const BASE_URL = 'http://192.168.1.3:8080'; 

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 从本地缓存获取 JWT Token
    const token = wx.getStorageSync('token'); 
    
    wx.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        // 如果有 Token，按 Spring Security JWT 的标准格式拼接 Bearer
        'Authorization': token ? `Bearer ${token}` : '', 
        ...options.header
      },
      success: (res) => {
        // 假设你的 ApiResponse 结构是 { code: 200, data: {...}, message: "..." }
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data);
        } 
        // JWT 过期或未授权 (根据你的 ErrorCode 配置)
        else if (res.statusCode === 401 || res.data.code === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          // 强制跳转回登录页
          wx.redirectTo({ url: '/pages/login/login' });
          reject(new Error('认证已失效，请重新登录'));
        } 
        else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' });
          reject(res.data);
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络请求失败，请检查网络', icon: 'none' });
        reject(err);
      }
    });
  });
};

export default request;