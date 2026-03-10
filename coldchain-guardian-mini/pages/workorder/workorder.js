// pages/workorder/workorder.js
Page({
  data: {
    activeTab: 'pending', // 'pending', 'processing', 'completed'
    workOrders: [
      {
        id: 1,
        title: '冷冻A区温度异常处理',
        type: '告警消缺',
        location: 'A-01',
        deadline: '2小时后到期',
        priority: 'high',
        status: 'pending' // pending, processing, completed
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

  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      activeTab: tab
    });
  },

  goToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/workorder/detail?id=${id}`
    });
  }
})