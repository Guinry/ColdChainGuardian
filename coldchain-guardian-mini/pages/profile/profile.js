// pages/profile/profile.js
Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    navHeight: 80,
    userInfo: {
      name: '张三',
      department: '运维部',
      avatar: '', // 留空则显示首字母头像
      avatarText: '张'
    },
    performance: {
      completionRate: '98.5%',
      processedCount: 126
    },
    notificationSettings: [{
        id: 'area-a-alert',
        name: '冷冻A区告警推送',
        enabled: true
      },
      {
        id: 'area-b-alert',
        name: '冷藏B区告警推送',
        enabled: true
      },
      {
        id: 'area-c-alert',
        name: '恒温库告警推送',
        enabled: false
      }
    ],
    offlineData: {
      pendingWorkOrders: 2,
      syncStatus: '已连接'
    }
  },

  onLoad() {
    // 1. 动态头像首字母兜底
    if (this.data.userInfo && this.data.userInfo.name) {
      this.setData({
        'userInfo.avatarText': this.data.userInfo.name.charAt(0)
      });
    }

    // 2. 获取胶囊位置，适配顶部导航栏
    const menuInfo = wx.getMenuButtonBoundingClientRect();
    this.setData({
      paddingTop: menuInfo.top,
      capsuleHeight: menuInfo.height,
      navHeight: menuInfo.bottom + 10
    });
  },

  // 切换通知开关
  toggleNotification(e) {
    const id = e.currentTarget.dataset.id;
    const settings = this.data.notificationSettings;
    const setting = settings.find(item => item.id === id);
    if (setting) {
      setting.enabled = !setting.enabled;
      this.setData({
        notificationSettings: settings
      });
      wx.showToast({
        title: setting.enabled ? '已开启推送' : '已关闭推送',
        icon: 'none'
      });
    }
  },

  // 手动同步离线数据
  manualSync() {
    if (this.data.offlineData.pendingWorkOrders === 0) return;

    wx.showLoading({
      title: '数据同步中...'
    });

    // 模拟同步过程
    setTimeout(() => {
      wx.hideLoading();
      this.setData({
        'offlineData.pendingWorkOrders': 0,
        'offlineData.syncStatus': '已连接'
      });
      wx.showToast({
        title: '同步成功',
        icon: 'success'
      });
    }, 1500);
  },

  // 退出登录
  handleLogout() {
    wx.showModal({
      title: '退出确认',
      content: '确认要退出当前账号吗？',
      confirmColor: '#F54A45',
      success: (res) => {
        if (res.confirm) {
          wx.showToast({
            title: '已退出',
            icon: 'success'
          });
          // 实际项目中这里应清除 Token 并跳转到登录页
        }
      }
    });
  }
})