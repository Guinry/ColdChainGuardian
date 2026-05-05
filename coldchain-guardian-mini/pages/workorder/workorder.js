import request, { unwrapPageData } from '../../utils/request';
import {
  buildQuery,
  getCachedUser,
  getNavMetrics,
  normalizeWorkOrder
} from '../../utils/domain';

Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    isRefreshing: false,
    activeTab: 'PENDING',
    tabs: [
      { key: 'PENDING', label: '待接收', count: 0 },
      { key: 'PROCESSING', label: '处理中', count: 0 },
      { key: 'VERIFYING', label: '待验收', count: 0 },
      { key: 'COMPLETED', label: '已完成', count: 0 }
    ],
    workOrders: [],
    currentPage: 1,
    pageSize: 8,
    total: 0,
    hasMore: true,
    loading: false,
    keyword: ''
  },

  onLoad() {
    this.setData(getNavMetrics());
    this.loadWorkOrders(true);
    this.refreshStats();
  },

  onShow() {
    if (wx.getStorageSync('workOrderListDirty')) {
      wx.removeStorageSync('workOrderListDirty');
      this.loadWorkOrders(true);
      this.refreshStats();
    }
  },

  loadWorkOrders(refresh = false) {
    if (this.data.loading) return;
    const nextPage = refresh ? 1 : this.data.currentPage;
    if (!refresh && !this.data.hasMore) return;

    const userInfo = getCachedUser();
    const query = buildQuery({
      status: this.data.activeTab,
      keyword: this.data.keyword,
      page: nextPage,
      size: this.data.pageSize,
      assigneeId: userInfo.id || userInfo.userId
    });

    this.setData({ loading: true });

    request({
      url: `/api/work-orders?${query}`,
      method: 'GET'
    }).then((res) => {
      const { list, total } = unwrapPageData(res);
      const incoming = list.map((item) => normalizeWorkOrder(item));
      const workOrders = refresh ? incoming : this.data.workOrders.concat(incoming);

      this.setData({
        workOrders,
        total,
        hasMore: workOrders.length < total,
        currentPage: nextPage + 1,
        loading: false,
        isRefreshing: false
      });

      this.patchActiveTabCount(total);
      wx.stopPullDownRefresh();
    }).catch((error) => {
      this.setData({ loading: false, isRefreshing: false });
      console.error('[workorder] load failed', error);
      wx.stopPullDownRefresh();
    });
  },

  refreshStats() {
    const userInfo = getCachedUser();
    const base = {
      page: 1,
      size: 1,
      assigneeId: userInfo.id || userInfo.userId
    };

    Promise.all(this.data.tabs.map((tab) => request({
      url: `/api/work-orders?${buildQuery({ ...base, status: tab.key })}`,
      method: 'GET'
    }))).then((results) => {
      this.setData({
        tabs: this.data.tabs.map((tab, index) => ({
          ...tab,
          count: unwrapPageData(results[index]).total || 0
        }))
      });
    }).catch((error) => {
      console.warn('[workorder] stats failed', error);
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
      workOrders: [],
      total: 0,
      currentPage: 1,
      hasMore: true
    });
    this.loadWorkOrders(true);
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  searchOrders() {
    this.loadWorkOrders(true);
  },

  clearSearch() {
    if (!this.data.keyword) return;
    this.setData({ keyword: '' });
    this.loadWorkOrders(true);
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: `/pages/workorder/detail/detail?id=${id}`
    });
  },

  goCreateOrder() {
    wx.navigateTo({
      url: '/pages/workorder/create/create'
    });
  },

  onRefresh() {
    this.setData({ isRefreshing: true });
    this.loadWorkOrders(true);
    this.refreshStats();
  },

  onPullDownRefresh() {
    this.onRefresh();
  },

  loadMore() {
    this.loadWorkOrders(false);
  },

  onReachBottom() {
    this.loadMore();
  }
});
