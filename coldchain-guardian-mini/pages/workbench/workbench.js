// pages/workbench/workbench.js
Page({
  data: {
    navHeight: 80, // 固定导航栏高度
    isRefreshing: false, // 下拉刷新状态控制
    userInfo: {
      name: '张工',
      shift: '白班',
      avatarText: '张'
    },
    weather: '晴 -2℃',
    pendingTasks: 3,
    completedToday: 5,
    urgentTasks: [
      {
        id: 1,
        title: '冷冻A区温度严重异常',
        location: '库区 A-01',
        time: '10分钟前',
        priority: 'urgent'
      },
      {
        id: 2,
        title: '制冷压缩机离线报警',
        location: '机房 B-05',
        time: '15分钟前',
        priority: 'high'
      }
    ]
  },

  onLoad() {
    // 1. 动态头像
    if (this.data.userInfo && this.data.userInfo.name) {
      this.setData({
        'userInfo.avatarText': this.data.userInfo.name.charAt(0)
      });
    }

    // 2. 获取胶囊底部位置，作为固定导航蓝条的高度
    const menuInfo = wx.getMenuButtonBoundingClientRect();
    this.setData({
      navHeight: menuInfo.bottom + 10 
    });
  },

  // 🌟 新增：处理下拉刷新
  onRefresh() {
    this.setData({ isRefreshing: true });
    
    // 模拟网络请求
    setTimeout(() => {
      this.setData({ isRefreshing: false });
      wx.showToast({ title: '数据已最新', icon: 'success' });
    }, 1000);
  },

  scanQRCode() {
    wx.scanCode({
      success: (res) => {
        wx.navigateTo({ url: `/pages/device-detail/device-detail?id=${res.result}` });
      }
    });
  },

  voiceRepair() {
    wx.vibrateShort(); 
    wx.startRecord({
      success: () => wx.showToast({ title: '语音记录成功', icon: 'success' })
    });
  },

  goToTask(e) {
    const taskId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/workorder/detail/detail?id=${taskId}` });
  }
})