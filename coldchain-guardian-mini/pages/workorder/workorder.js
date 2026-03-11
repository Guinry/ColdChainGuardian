// pages/workorder/workorder.js
Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    isRefreshing: false,
    activeTab: 'pending', // 'pending', 'processing', 'completed'
    workOrders: [],
    allWorkOrders: [{
        id: 1,
        title: '冷冻A区温度异常处理',
        type: '告警消缺',
        location: 'A-01',
        deadline: '2小时后到期',
        priority: 'high',
        status: 'pending'
      },
      {
        id: 2,
        title: '日常巡检 - 恒温库',
        type: '日常巡检',
        location: 'C-01 to C-10',
        deadline: '1天后到期',
        priority: 'normal',
        status: 'pending'
      },
      {
        id: 3,
        title: '制冷设备维护',
        type: '设备保养',
        location: 'B-05',
        deadline: '已逾期2小时',
        priority: 'urgent',
        status: 'processing'
      }
    ]
  },

  onLoad() {
    // 🌟 动态计算胶囊高度，防遮挡
    const menuInfo = wx.getMenuButtonBoundingClientRect();
    this.setData({
      paddingTop: menuInfo.top,
      capsuleHeight: menuInfo.height
    });
    this.filterOrders();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      activeTab: tab
    }, () => {
      this.filterOrders();
    });
  },

  filterOrders() {
    const filtered = this.data.allWorkOrders.filter(item => item.status === this.data.activeTab);
    this.setData({
      workOrders: filtered
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/workorder/detail/detail?id=${id}`
    });
  },

  // 🌟 处理下拉刷新
  onRefresh() {
    this.setData({
      isRefreshing: true
    });
    setTimeout(() => {
      this.setData({
        isRefreshing: false
      });
      wx.showToast({
        title: '列表已刷新',
        icon: 'success'
      });
    }, 1000);
  }
})