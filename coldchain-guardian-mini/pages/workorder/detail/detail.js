// pages/workorder/detail/detail.js
Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    workOrder: {
      id: 1,
      title: '冷冻A区温度异常处理',
      description: '冷冻A区温度持续高于正常范围，需要检查制冷设备运行状态和管道是否有泄漏。',
      location: 'A-01',
      type: '告警消缺',
      priority: 'high',
      deadline: '2小时后到期',
      status: 'processing'
    },
    photos: [],
    recordText: ''
  },

  onLoad(options) {
    const menuInfo = wx.getMenuButtonBoundingClientRect();
    this.setData({
      paddingTop: menuInfo.top,
      capsuleHeight: menuInfo.height
    });
    console.log('加载工单详情，ID:', options.id);
  },

  // 🌟 自定义返回上一页逻辑
  goBack() {
    wx.navigateBack();
  },

  scanCheckIn() {
    wx.scanCode({
      success: (res) => wx.showToast({
        title: '签到成功',
        icon: 'success'
      }),
      fail: () => wx.showToast({
        title: '签到取消',
        icon: 'none'
      })
    });
  },

  takePhoto() {
    const that = this;
    wx.chooseImage({
      count: 1,
      sourceType: ['camera', 'album'],
      success(res) {
        that.setData({
          photos: [...that.data.photos, ...res.tempFilePaths]
        });
      }
    });
  },

  deletePhoto(e) {
    const index = e.currentTarget.dataset.index;
    const photos = this.data.photos;
    photos.splice(index, 1);
    this.setData({
      photos: photos
    });
  },

  inputRecord(e) {
    this.setData({
      recordText: e.detail.value
    });
  },

  voiceInput() {
    wx.vibrateShort();
    wx.startRecord({
      success: (res) => {
        this.setData({
          recordText: this.data.recordText + ' [语音输入记录]'
        });
      }
    });
    setTimeout(() => wx.stopRecord(), 3000);
  },

  submitForReview() {
    wx.showModal({
      title: '提交验收',
      content: '确认处理完毕并提交验收吗？',
      confirmColor: '#3A7AFE',
      success: (res) => {
        if (res.confirm) {
          wx.showToast({
            title: '提交成功',
            icon: 'success'
          });
          setTimeout(() => wx.navigateBack(), 1500);
        }
      }
    });
  }
})