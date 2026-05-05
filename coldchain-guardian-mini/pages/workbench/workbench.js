import request from '../../utils/request';
import {
  buildQuery,
  getCachedUser,
  getNavMetrics,
  normalizeAlert,
  normalizeWorkOrder
} from '../../utils/domain';

Page({
  data: {
    navHeight: 86,
    isRefreshing: false,
    loading: false,
    userInfo: {
      id: null,
      name: '',
      roleText: '',
      avatarText: ''
    },
    summary: {
      pendingTasks: 0,
      processingTasks: 0,
      urgentAlerts: 0,
      onlineDevices: 0,
      totalDevices: 0
    },
    urgentAlerts: [],
    pendingOrders: [],
    lastUpdated: '--'
  },

  onLoad() {
    this.setData(getNavMetrics());
    this.initUserInfo();
    this.loadDashboardData();
  },

  onShow() {
    const cachedUser = getCachedUser();
    if (cachedUser && cachedUser.id && cachedUser.id !== this.data.userInfo.id) {
      this.initUserInfo();
      this.loadDashboardData();
    }
  },

  initUserInfo() {
    const cachedUser = getCachedUser();
    const realName = cachedUser.realName || cachedUser.name || cachedUser.username || '值班人员';
    const roleTextMap = {
      ADMIN: '系统管理员',
      MANAGER: '库区主管',
      STOCK_MANAGER: '仓储管理员',
      TECHNICIAN: '现场运维'
    };

    this.setData({
      userInfo: {
        id: cachedUser.id || cachedUser.userId || null,
        name: realName,
        roleText: roleTextMap[cachedUser.role] || cachedUser.role || '移动端用户',
        avatarText: realName ? realName.charAt(0) : '冷'
      }
    });
  },

  loadDashboardData() {
    if (this.data.loading) return;

    const userId = this.data.userInfo.id;
    const statsQuery = buildQuery({ assigneeId: userId });
    const orderQuery = buildQuery({
      status: 'PENDING',
      page: 1,
      size: 5,
      assigneeId: userId
    });

    this.setData({ loading: true });

    Promise.all([
      request({ url: `/api/dashboard/stats${statsQuery ? `?${statsQuery}` : ''}`, method: 'GET' }),
      request({ url: '/api/alerts/urgent', method: 'GET' }),
      request({ url: `/api/work-orders?${orderQuery}`, method: 'GET' })
    ]).then(([statsRes, alertsRes, ordersRes]) => {
      const stats = statsRes.data || {};
      const urgentAlerts = (alertsRes.data || []).slice(0, 4).map((item) => normalizeAlert(item));
      const pageData = ordersRes.data || {};
      const orders = (pageData.data || pageData.records || []).slice(0, 4).map((item) => normalizeWorkOrder(item));

      this.setData({
        loading: false,
        isRefreshing: false,
        summary: {
          pendingTasks: stats.pendingWorkOrders || orders.length || 0,
          processingTasks: stats.processingWorkOrders || stats.processingCount || 0,
          urgentAlerts: stats.criticalAlerts || stats.highAlerts || urgentAlerts.length || 0,
          onlineDevices: stats.onlineDevices || 0,
          totalDevices: stats.totalDevices || 0
        },
        urgentAlerts,
        pendingOrders: orders,
        lastUpdated: this.formatClock(new Date())
      });

      wx.stopPullDownRefresh();
    }).catch((error) => {
      this.setData({ loading: false, isRefreshing: false });
      console.error('[workbench] load failed', error);
      wx.stopPullDownRefresh();
    });
  },

  formatClock(date) {
    const pad = (value) => `${value}`.padStart(2, '0');
    return `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  },

  onRefresh() {
    this.setData({ isRefreshing: true });
    this.loadDashboardData();
  },

  scanQRCode() {
    wx.scanCode({
      success: (res) => {
        const code = res.result || '';
        if (!code) {
          wx.showToast({ title: '未识别到设备码', icon: 'none' });
          return;
        }

        wx.showModal({
          title: '设备识别成功',
          content: `设备标识：${code}\n可在告警或工单中继续处理现场问题。`,
          confirmText: '查告警',
          cancelText: '关闭',
          success: (modalRes) => {
            if (modalRes.confirm) {
              wx.switchTab({ url: '/pages/alert/alert' });
            }
          }
        });
      },
      fail: () => {
        wx.showToast({ title: '扫码已取消', icon: 'none' });
      }
    });
  },

  quickReport() {
    wx.navigateTo({
      url: '/pages/workorder/create/create'
    });
  },

  goToAlert(e) {
    const id = e.currentTarget.dataset.id;
    wx.switchTab({ url: '/pages/alert/alert' });
    if (id) {
      wx.setStorageSync('focusAlertId', id);
    }
  },

  goToWorkOrder(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) {
      wx.switchTab({ url: '/pages/workorder/workorder' });
      return;
    }

    wx.navigateTo({
      url: `/pages/workorder/detail/detail?id=${id}`
    });
  },

  goAllAlerts() {
    wx.switchTab({ url: '/pages/alert/alert' });
  },

  goAllOrders() {
    wx.switchTab({ url: '/pages/workorder/workorder' });
  }
});
