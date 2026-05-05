// utils/request.js
const DEFAULT_BASE_URL = 'http://127.0.0.1:8080';

const getBaseUrl = () => wx.getStorageSync('apiBaseUrl') || DEFAULT_BASE_URL;

export const unwrapPageData = (payload) => {
  const data = payload && payload.data;
  if (Array.isArray(data)) {
    return { list: data, total: data.length };
  }
  if (data && Array.isArray(data.data)) {
    return { list: data.data, total: data.total || data.data.length };
  }
  if (data && Array.isArray(data.records)) {
    return { list: data.records, total: data.total || data.records.length };
  }
  return { list: [], total: 0 };
};

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 从本地缓存获取 JWT Token
    const token = wx.getStorageSync('token'); 
    const baseUrl = getBaseUrl();
    
    console.log('[request]', options.method || 'GET', baseUrl + options.url);

    wx.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        // 如果有 Token，按 Spring Security JWT 的标准格式拼接 Bearer
        'Authorization': token ? `Bearer ${token}` : '', 
        ...options.header
      },
      success: (res) => {
        // ApiResponse 结构是 { success, code, data, message }
        const body = res.data || {};
        if (res.statusCode >= 200 && res.statusCode < 300 && (body.code === 200 || body.success === true)) {
          resolve(body);
        } 
        // JWT 过期或未授权 (根据你的 ErrorCode 配置)
        else if (res.statusCode === 401 || body.code === 401) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          // 强制跳转回登录页
          wx.redirectTo({ url: '/pages/login/login' });
          reject(new Error('认证已失效，请重新登录'));
        } 
        else {
          const message = body.message || body.msg || `请求失败(${res.statusCode})`;
          console.error('[request failed]', options.method || 'GET', options.url, res.statusCode, body);
          wx.showToast({ title: message, icon: 'none' });
          reject({ ...body, message, statusCode: res.statusCode });
        }
      },
      fail: (err) => {
        const message = `网络请求失败: ${err.errMsg || '请检查后端服务和本机地址'}`;
        console.error('[request error]', options.method || 'GET', baseUrl + options.url, err);
        wx.showToast({ title: message, icon: 'none' });
        reject({ ...err, message });
      }
    });
  });
};

export default request;
