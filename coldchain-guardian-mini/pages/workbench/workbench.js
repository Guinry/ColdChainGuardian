// pages/workbench/workbench.js
Page({
  data: {
    userInfo: {
      name: '张工',
      shift: '白班'
    },
    weather: '晴 -2℃',
    pendingTasks: 3,
    completedToday: 5,
    urgentTasks: [
      {
        id: 1,
        title: '冷冻A区温度异常',
        location: 'A-01',
        time: '10分钟前',
        priority: 'high'
      },
      {
        id: 2,
        title: '制冷设备报警',
        location: 'B-05',
        time: '15分钟前',
        priority: 'urgent'
      }
    ]
  },

  onLoad() {

  },

  scanQRCode() {
    wx.scanCode({
      success: (res) => {
        console.log('扫描结果:', res.result);
        // 扫码后的逻辑处理
        wx.navigateTo({
          url: `/pages/device-detail/device-detail?id=${res.result}`
        });
      },
      fail: (err) => {
        console.error('扫码失败:', err);
        wx.showToast({
          title: '扫码失败',
          icon: 'none'
        });
      }
    });
  },

  voiceRepair() {
    // 语音报修功能
    wx.startRecord({
      success: (res) => {
        const tempFilePath = res.tempFilePath;
        console.log('录音成功:', tempFilePath);
        // 语音转文字并生成工单的逻辑
        wx.navigateTo({
          url: `/pages/workorder/create?voice=${tempFilePath}`
        });
      },
      fail: (err) => {
        console.error('录音失败:', err);
        wx.showToast({
          title: '录音失败',
          icon: 'none'
        });
      }
    });

    setTimeout(() => {
      wx.stopRecord();
    }, 10000); // 最多录制10秒
  }
})