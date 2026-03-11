// pages/alert/alert.js
Page({
  data: {
    paddingTop: 44,    
    capsuleHeight: 32, 
    activeTab: 'unhandled', 
    unhandledCount: 3,
    processingCount: 0,
    currentAlerts: [], 
    isRefreshing: false, // 🌟 增加下拉刷新状态控制
    allAlerts: [
      {
        id: 1,
        title: '冷冻A区温度异常升高',
        currentValue: '-12.5℃',
        thresholdValue: '-18.0℃',
        time: '10分钟前',
        priority: 'urgent', 
        location: '库区 A-01',
        status: 'unhandled'
      },
      {
        id: 2,
        title: '冷藏B区湿度超标',
        currentValue: '85%',
        thresholdValue: '70%',
        time: '25分钟前',
        priority: 'high',
        location: '库区 B-03',
        status: 'unhandled'
      },
      {
        id: 3,
        title: '恒温库设备离线',
        currentValue: '离线',
        thresholdValue: '在线',
        time: '1小时前',
        priority: 'normal',
        location: '库区 C-05',
        status: 'unhandled'
      }
    ]
  },

  onLoad() {
    // 🌟 核心适配：获取右上角胶囊按钮的位置信息
    const menuInfo = wx.getMenuButtonBoundingClientRect();
    this.setData({
      paddingTop: menuInfo.top,      // 状态栏的高度（把内容往下推）
      capsuleHeight: menuInfo.height // 胶囊本身的高度（用于文字居中对齐）
    });
    
    this.filterAlerts();
  },

  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({ activeTab: tab }, () => {
      this.filterAlerts();
    });
  },

  filterAlerts() {
    const filtered = this.data.allAlerts.filter(item => item.status === this.data.activeTab);
    this.setData({ currentAlerts: filtered });
  },

  dismissAlert(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认误报',
      content: '确认该告警为误报并消除吗？',
      confirmColor: '#3A7AFE',
      success: (res) => {
        if (res.confirm) {
          wx.showToast({ title: '已消除', icon: 'success' });
          const newAlerts = this.data.allAlerts.filter(a => a.id !== id);
          this.setData({ 
            allAlerts: newAlerts,
            unhandledCount: newAlerts.filter(a => a.status === 'unhandled').length
          });
          this.filterAlerts();
        }
      }
    });
  },

  transferToOrder(e) {
    wx.showModal({
      title: '转派工单',
      content: '确认将此告警转为维修工单？',
      confirmColor: '#3A7AFE',
      success: (res) => {
        if (res.confirm) {
          wx.showToast({ title: '转单成功', icon: 'success' });
        }
      }
    });
  },

  onPullDownRefresh() {
    setTimeout(() => {
      wx.stopPullDownRefresh();
      wx.showToast({ title: '已更新', icon: 'none' });
    }, 1000);
  },

  onRefresh() {
    this.setData({ isRefreshing: true }); // 开启顶部 loading 动画
    
    // 模拟重新向后端请求数据，1秒后结束动画
    setTimeout(() => {
      this.setData({ isRefreshing: false }); // 隐藏动画
      wx.showToast({ title: '数据已最新', icon: 'success' });
    }, 1000);
  }
})