// pages/profile/profile.js
Page({
  data: {
    userInfo: {
      name: '张三',
      department: '运维部',
      avatar: '/images/avatar.png'
    },
    performance: {
      completionRate: '98.5%',
      processedCount: 126
    },
    notificationSettings: [
      {
        id: 'area-a-alert',
        name: 'A区告警推送',
        enabled: true
      },
      {
        id: 'area-b-alert',
        name: 'B区告警推送',
        enabled: true
      },
      {
        id: 'area-c-alert',
        name: 'C区告警推送',
        enabled: false
      }
    ],
    offlineData: {
      pendingWorkOrders: 2,
      syncStatus: '已连接'
    }
  },

  onLoad() {

  },

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
        title: setting.enabled ? '开启成功' : '关闭成功',
        icon: 'none'
      });
    }
  },

  manualSync() {
    wx.showLoading({
      title: '同步中...'
    });

    // 模拟同步过程
    setTimeout(() => {
      wx.hideLoading();
      wx.showToast({
        title: '同步完成',
        icon: 'success'
      });

      // 更新本地数据
      this.setData({
        'offlineData.pendingWorkOrders': 0,
        'offlineData.syncStatus': '已连接'
      });
    }, 2000);
  },

  viewOfflineData() {
    wx.showModal({
      title: '离线数据详情',
      content: '当前没有待同步的离线数据',
      showCancel: false
    });
  }
})