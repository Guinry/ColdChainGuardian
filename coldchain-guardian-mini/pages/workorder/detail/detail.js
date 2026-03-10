// pages/workorder/detail/detail.js
Page({
  data: {
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
    photos: [], // 现场照片数组
    recordText: '' // 处理记录文本
  },

  onLoad(options) {
    const id = options.id;
    // 根据ID加载工单详情
    console.log('加载工单详情，ID:', id);
  },

  scanCheckIn() {
    // 扫码签到功能
    wx.scanCode({
      success: (res) => {
        console.log('签到二维码:', res.result);
        wx.showToast({
          title: '签到成功',
          icon: 'success'
        });
      },
      fail: (err) => {
        console.error('签到失败:', err);
        wx.showToast({
          title: '签到失败',
          icon: 'none'
        });
      }
    });
  },

  takePhoto() {
    const that = this;
    wx.chooseImage({
      count: 1, // 只允许选择一张图片
      sourceType: ['camera'], // 只允许拍照
      success(res) {
        const tempFilePaths = res.tempFilePaths;
        that.setData({
          photos: [...that.data.photos, ...tempFilePaths]
        });
        console.log('拍摄的照片:', tempFilePaths);
      },
      fail(err) {
        console.error('拍照失败:', err);
        wx.showToast({
          title: '拍照失败',
          icon: 'none'
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
    // 语音输入功能
    wx.startRecord({
      success: (res) => {
        const tempFilePath = res.tempFilePath;
        console.log('录音成功:', tempFilePath);
        // 这里应该是语音转文字的逻辑，暂时模拟
        this.setData({
          recordText: this.data.recordText + ' [语音输入]'
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
  },

  submitForReview() {
    // 提交验收功能
    wx.showModal({
      title: '确认提交',
      content: '确认提交工单进行验收吗？',
      success(res) {
        if (res.confirm) {
          console.log('提交工单验收');
          wx.showToast({
            title: '提交成功',
            icon: 'success'
          });

          // 返回上级页面
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        }
      }
    });
  }
})