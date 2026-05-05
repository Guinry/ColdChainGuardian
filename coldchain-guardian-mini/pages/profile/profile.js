import request from '../../utils/request';
import { getNavMetrics } from '../../utils/domain';

const DEFAULT_SETTINGS = [
  { id: 'urgent-alert', name: '紧急告警推送', enabled: true },
  { id: 'workorder-update', name: '工单状态提醒', enabled: true },
  { id: 'daily-summary', name: '每日值班摘要', enabled: false }
];

Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    navHeight: 86,
    isRefreshing: false,
    userInfo: {
      id: null,
      name: '',
      phone: '',
      roleText: '',
      avatar: '',
      avatarText: ''
    },
    performance: {
      pendingCount: 0,
      processingCount: 0,
      overdueCount: 0,
      completedThisWeek: 0,
      completionRate: '--'
    },
    notificationSettings: DEFAULT_SETTINGS,
    connection: {
      apiBaseUrl: '',
      status: '已连接'
    }
  },

  onLoad() {
    this.setData(getNavMetrics());
    this.loadSettings();
    this.loadProfileData();
  },

  loadSettings() {
    const saved = wx.getStorageSync('notificationSettings');
    this.setData({
      notificationSettings: Array.isArray(saved) && saved.length ? saved : DEFAULT_SETTINGS,
      'connection.apiBaseUrl': wx.getStorageSync('apiBaseUrl') || 'http://127.0.0.1:8080'
    });
  },

  loadProfileData() {
    wx.showLoading({ title: '加载中...' });
    Promise.all([
      request({ url: '/api/user/me', method: 'GET' }),
      request({ url: '/api/work-orders/stats', method: 'GET' })
    ]).then(([userRes, statsRes]) => {
      wx.hideLoading();
      const user = userRes.data || {};
      const stats = statsRes.data || {};
      const roleTextMap = {
        ADMIN: '系统管理员',
        MANAGER: '库区主管',
        STOCK_MANAGER: '仓储管理员',
        TECHNICIAN: '现场运维'
      };
      const name = user.realName || user.username || '移动端用户';
      const total = (stats.pendingCount || 0) + (stats.processingCount || 0) + (stats.completedThisWeek || 0);
      const completionRate = total > 0 ? `${Math.round(((stats.completedThisWeek || 0) / total) * 100)}%` : '--';

      const userInfo = {
        id: user.id,
        name,
        phone: user.phone || user.username || '--',
        roleText: roleTextMap[user.role] || user.role || '移动端用户',
        avatar: user.wxAvatar || user.avatar || '',
        avatarText: name.charAt(0)
      };

      wx.setStorageSync('userInfo', {
        ...wx.getStorageSync('userInfo'),
        id: user.id,
        username: user.username,
        realName: user.realName,
        phone: user.phone,
        role: user.role,
        status: user.status
      });

      this.setData({
        userInfo,
        performance: {
          pendingCount: stats.pendingCount || 0,
          processingCount: stats.processingCount || 0,
          overdueCount: stats.overdueCount || 0,
          completedThisWeek: stats.completedThisWeek || 0,
          completionRate
        },
        'connection.status': '已连接',
        isRefreshing: false
      });
      wx.stopPullDownRefresh();
    }).catch((error) => {
      wx.hideLoading();
      this.setData({
        'connection.status': '连接异常',
        isRefreshing: false
      });
      console.error('[profile] load failed', error);
      wx.stopPullDownRefresh();
    });
  },

  onRefresh() {
    this.setData({ isRefreshing: true });
    this.loadProfileData();
  },

  toggleNotification(e) {
    const id = e.currentTarget.dataset.id;
    const settings = this.data.notificationSettings.map((item) => (
      item.id === id ? { ...item, enabled: !item.enabled } : item
    ));
    wx.setStorageSync('notificationSettings', settings);
    this.setData({ notificationSettings: settings });
  },

  manualSync() {
    this.loadProfileData();
  },

  showAbout() {
    wx.showModal({
      title: '冷链守护',
      content: '移动端用于现场告警处置、工单接收、处理记录和状态跟踪。当前连接：' + this.data.connection.apiBaseUrl,
      showCancel: false
    });
  },

  handleLogout() {
    wx.showModal({
      title: '退出确认',
      content: '确认要退出当前账号吗？',
      confirmColor: '#E5484D',
      success: (res) => {
        if (!res.confirm) return;
        wx.clearStorageSync();
        wx.showToast({ title: '已退出', icon: 'success' });
        setTimeout(() => {
          wx.reLaunch({ url: '/pages/login/login' });
        }, 600);
      }
    });
  }
});
