import request, { unwrapPageData } from '../../utils/request';
import {
  buildQuery,
  getCachedUser,
  getNavMetrics,
  normalizeAlert
} from '../../utils/domain';

Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    activeTab: 'UNHANDLED',
    tabs: [
      { key: 'UNHANDLED', label: '未处理', count: 0 },
      { key: 'HANDLING', label: '处理中', count: 0 },
      { key: 'RESOLVED', label: '已恢复', count: 0 }
    ],
    currentAlerts: [],
    isRefreshing: false,
    currentPage: 1,
    pageSize: 8,
    total: 0,
    hasMore: true,
    loading: false,
    keyword: '',
    focusAlertId: null
  },

  onLoad() {
    this.setData(getNavMetrics());
    this.loadAlertsData(true);
    this.refreshStats();
  },

  onShow() {
    const focusAlertId = wx.getStorageSync('focusAlertId');
    if (focusAlertId) {
      wx.removeStorageSync('focusAlertId');
      this.setData({ focusAlertId });
    }
  },

  loadAlertsData(refresh = false) {
    if (this.data.loading) return;

    const nextPage = refresh ? 1 : this.data.currentPage;
    if (!refresh && !this.data.hasMore) return;

    const query = buildQuery({
      status: this.data.activeTab,
      keyword: this.data.keyword,
      page: nextPage,
      size: this.data.pageSize
    });

    this.setData({ loading: true });

    request({
      url: `/api/alerts/search?${query}`,
      method: 'GET'
    }).then((res) => {
      const { list, total } = unwrapPageData(res);
      const incoming = list.map((item) => normalizeAlert(item, this.data.activeTab));
      const currentAlerts = refresh ? incoming : this.data.currentAlerts.concat(incoming);

      this.setData({
        currentAlerts,
        total,
        hasMore: currentAlerts.length < total,
        currentPage: nextPage + 1,
        loading: false,
        isRefreshing: false
      });

      this.patchActiveTabCount(total);
      wx.stopPullDownRefresh();
    }).catch((error) => {
      this.setData({ loading: false, isRefreshing: false });
      console.error('[alert] load failed', error);
      wx.stopPullDownRefresh();
    });
  },

  refreshStats() {
    Promise.all([
      request({ url: '/api/alerts/search?status=UNHANDLED&page=1&size=1', method: 'GET' }),
      request({ url: '/api/alerts/search?status=HANDLING&page=1&size=1', method: 'GET' }),
      request({ url: '/api/alerts/search?status=RESOLVED&page=1&size=1', method: 'GET' })
    ]).then(([unhandled, handling, resolved]) => {
      const totals = {
        UNHANDLED: unwrapPageData(unhandled).total,
        HANDLING: unwrapPageData(handling).total,
        RESOLVED: unwrapPageData(resolved).total
      };
      this.setData({
        tabs: this.data.tabs.map((tab) => ({
          ...tab,
          count: totals[tab.key] || 0
        }))
      });
    }).catch((error) => {
      console.warn('[alert] stats failed', error);
    });
  },

  patchActiveTabCount(total) {
    this.setData({
      tabs: this.data.tabs.map((tab) => tab.key === this.data.activeTab ? { ...tab, count: total } : tab)
    });
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    if (!tab || tab === this.data.activeTab) return;

    this.setData({
      activeTab: tab,
      currentAlerts: [],
      total: 0,
      currentPage: 1,
      hasMore: true
    });
    this.loadAlertsData(true);
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  searchAlerts() {
    this.loadAlertsData(true);
  },

  clearSearch() {
    if (!this.data.keyword) return;
    this.setData({ keyword: '' });
    this.loadAlertsData(true);
  },

  onRefresh() {
    this.setData({ isRefreshing: true });
    this.loadAlertsData(true);
    this.refreshStats();
  },

  onPullDownRefresh() {
    this.onRefresh();
  },

  loadMore() {
    this.loadAlertsData(false);
  },

  onReachBottom() {
    this.loadMore();
  },

  dismissAlert(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认误报',
      content: '确认该告警为误报并关闭？关闭后不会生成工单。',
      confirmText: '确认关闭',
      confirmColor: '#E5484D',
      success: (res) => {
        if (res.confirm) {
          this.updateAlertStatus(id, 'IGNORED', '小程序端确认误报消除');
        }
      }
    });
  },

  resolveAlert(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认恢复',
      content: '确认现场状态已恢复正常？',
      confirmText: '确认恢复',
      confirmColor: '#1565D8',
      success: (res) => {
        if (res.confirm) {
          this.updateAlertStatus(id, 'RESOLVED', '小程序端确认恢复');
        }
      }
    });
  },

  updateAlertStatus(id, status, handleRemark) {
    wx.showLoading({ title: '处理中...' });
    request({
      url: `/api/alerts/${id}/status`,
      method: 'PUT',
      data: { status, handleRemark }
    }).then(() => {
      wx.hideLoading();
      wx.showToast({ title: '已更新', icon: 'success' });
      this.loadAlertsData(true);
      this.refreshStats();
    }).catch((error) => {
      wx.hideLoading();
      console.error('[alert] status update failed', error);
    });
  },

  transferToOrder(e) {
    const id = e.currentTarget.dataset.id;
    const userInfo = getCachedUser();

    wx.showModal({
      title: '转为工单',
      content: '将该告警转为维修工单并分配给当前登录人员？',
      confirmText: '生成工单',
      confirmColor: '#1565D8',
      success: (res) => {
        if (!res.confirm) return;

        wx.showLoading({ title: '转单中...' });
        request({
          url: `/api/alerts/${id}/convert-to-work-order`,
          method: 'PUT',
          data: {
            assigneeId: userInfo.id || userInfo.userId || 1,
            assigneeName: userInfo.realName || userInfo.username || '移动端人员',
            description: '由小程序告警大厅转派生成'
          }
        }).then((result) => {
          wx.hideLoading();
          wx.showToast({ title: '转单成功', icon: 'success' });
          this.loadAlertsData(true);
          this.refreshStats();

          const workOrderId = result.data && result.data.id;
          if (workOrderId) {
            setTimeout(() => {
              wx.navigateTo({
                url: `/pages/workorder/detail/detail?id=${workOrderId}`
              });
            }, 600);
          }
        }).catch((error) => {
          wx.hideLoading();
          console.error('[alert] convert failed', error);
        });
      }
    });
  }
});
