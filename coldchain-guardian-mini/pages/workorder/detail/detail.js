import request from '../../../utils/request';
import {
  getCachedUser,
  getNavMetrics,
  normalizeLog,
  normalizeWorkOrder
} from '../../../utils/domain';

Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    workOrderId: null,
    workOrder: null,
    logs: [],
    photos: [],
    recordText: '',
    checkedIn: false,
    submitting: false,
    loading: false
  },

  onLoad(options) {
    this.setData(getNavMetrics());

    if (!options.id) {
      wx.showModal({
        title: '缺少工单ID',
        content: '无法打开工单详情，请从工单列表重新进入。',
        showCancel: false,
        success: () => wx.navigateBack()
      });
      return;
    }

    this.setData({ workOrderId: options.id });
    this.loadDetail();
  },

  loadDetail() {
    const id = this.data.workOrderId;
    if (!id) return;

    this.setData({ loading: true });

    Promise.all([
      request({ url: `/api/work-orders/${id}`, method: 'GET' }),
      request({ url: `/api/work-orders/${id}/logs`, method: 'GET' })
    ]).then(([detailRes, logsRes]) => {
      this.setData({
        workOrder: normalizeWorkOrder(detailRes.data || {}),
        logs: (logsRes.data || []).map((item) => normalizeLog(item)),
        loading: false
      });
    }).catch((error) => {
      this.setData({ loading: false });
      console.error('[workorder-detail] load failed', error);
    });
  },

  goBack() {
    wx.navigateBack({
      fail: () => wx.switchTab({ url: '/pages/workorder/workorder' })
    });
  },

  scanCheckIn() {
    wx.scanCode({
      success: (res) => {
        this.setData({ checkedIn: true });
        wx.showToast({ title: '签到成功', icon: 'success' });
        const prefix = this.data.recordText ? `${this.data.recordText}\n` : '';
        this.setData({
          recordText: `${prefix}现场签到：${res.result || '已扫码确认'}`
        });
      },
      fail: () => {
        this.setData({ checkedIn: true });
        wx.showToast({ title: '已记录到场', icon: 'success' });
      }
    });
  },

  takePhoto() {
    if (this.data.workOrder && this.data.workOrder.readonly) {
      wx.showToast({ title: '当前状态不可上传照片', icon: 'none' });
      return;
    }

    wx.chooseImage({
      count: Math.max(1, 9 - this.data.photos.length),
      sourceType: ['camera', 'album'],
      success: (res) => {
        this.setData({
          photos: this.data.photos.concat(res.tempFilePaths).slice(0, 9)
        });
      }
    });
  },

  deletePhoto(e) {
    const index = e.currentTarget.dataset.index;
    const photos = this.data.photos.slice();
    photos.splice(index, 1);
    this.setData({ photos });
  },

  previewPhoto(e) {
    const current = e.currentTarget.dataset.src;
    wx.previewImage({
      current,
      urls: this.data.photos
    });
  },

  inputRecord(e) {
    this.setData({ recordText: e.detail.value });
  },

  voiceInput() {
    wx.vibrateShort();
    wx.showToast({
      title: 'PC模拟器请手动输入',
      icon: 'none'
    });
  },

  handlePrimaryAction() {
    const order = this.data.workOrder;
    if (!order || this.data.submitting) return;

    if (order.status === 'PENDING') {
      this.updateStatus('PROCESSING', '接收并开始处理', '已接收并开始处理');
      return;
    }

    if (order.status === 'PROCESSING') {
      if (!this.data.recordText.trim()) {
        wx.showToast({ title: '请填写处理记录', icon: 'none' });
        return;
      }
      wx.showModal({
        title: '提交验收',
        content: '确认已完成现场处理并提交验收？',
        confirmText: '提交',
        confirmColor: '#1565D8',
        success: (res) => {
          if (res.confirm) {
            this.updateStatus('VERIFYING', '提交验收', this.data.recordText.trim());
          }
        }
      });
    }
  },

  updateStatus(status, loadingTitle, remark) {
    const userInfo = getCachedUser();

    this.setData({ submitting: true });
    wx.showLoading({ title: loadingTitle });

    request({
      url: `/api/work-orders/${this.data.workOrder.id}/status`,
      method: 'PUT',
      data: {
        status,
        remark,
        operatorId: userInfo.id || userInfo.userId || null,
        operatorName: userInfo.realName || userInfo.username || '移动端人员'
      }
    }).then((res) => {
      wx.hideLoading();
      wx.showToast({ title: '状态已更新', icon: 'success' });
      wx.setStorageSync('workOrderListDirty', true);
      this.setData({
        submitting: false,
        workOrder: normalizeWorkOrder(res.data || this.data.workOrder),
        recordText: status === 'PROCESSING' ? this.data.recordText : ''
      });
      this.loadDetail();
    }).catch((error) => {
      wx.hideLoading();
      this.setData({ submitting: false });
      console.error('[workorder-detail] status failed', error);
    });
  }
});
