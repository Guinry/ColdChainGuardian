// pages/alert/alert.js
Page({
  data: {
    activeTab: 'unhandled', // 'unhandled' or 'processing'
    alerts: [
      {
        id: 1,
        title: '【冷冻A区】温度异常升高',
        currentValue: '-12℃',
        thresholdValue: '-18℃',
        time: '10分钟前',
        priority: 'urgent', // 'urgent', 'high', 'normal'
        location: 'A-01'
      },
      {
        id: 2,
        title: '【冷藏B区】湿度超标',
        currentValue: '85%',
        thresholdValue: '70%',
        time: '25分钟前',
        priority: 'high',
        location: 'B-03'
      },
      {
        id: 3,
        title: '【恒温库】设备离线',
        currentValue: '离线',
        thresholdValue: '在线',
        time: '1小时前',
        priority: 'normal',
        location: 'C-05'
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

  dismissAlert(e) {
    const id = e.currentTarget.dataset.id;
    console.log('误报消除:', id);
    wx.showModal({
      title: '确认误报',
      content: '确认该告警为误报吗？',
      success(res) {
        if (res.confirm) {
          // 处理误报消除逻辑
          wx.showToast({
            title: '已标记为误报',
            icon: 'success'
          });
        }
      }
    });
  },

  createWorkOrder(e) {
    const id = e.currentTarget.dataset.id;
    console.log('创建工单:', id);
    // 跳转到创建工单页面
    wx.navigateTo({
      url: `/pages/workorder/create?alertId=${id}`
    });
  }
})