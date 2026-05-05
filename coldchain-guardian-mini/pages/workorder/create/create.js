import request, { unwrapPageData } from '../../../utils/request';
import { getCachedUser, getNavMetrics } from '../../../utils/domain';

const flattenAreas = (nodes = [], depth = 0) => nodes.reduce((result, node) => {
  result.push({
    id: node.id,
    name: `${'　'.repeat(depth)}${node.areaName || node.name || `库区 ${node.id}`}`
  });
  if (node.children && node.children.length) {
    result.push(...flattenAreas(node.children, depth + 1));
  }
  return result;
}, []);

Page({
  data: {
    paddingTop: 44,
    capsuleHeight: 32,
    navHeight: 86,
    loadingOptions: true,
    submitting: false,
    areaOptions: [],
    deviceOptions: [],
    filteredDevices: [],
    areaIndex: -1,
    deviceIndex: -1,
    form: {
      title: '',
      description: '',
      priority: 'MEDIUM',
      workType: 'MAINTENANCE',
      warehouseId: null,
      deviceId: null,
      locationDetail: ''
    },
    priorityOptions: [
      { label: '低', value: 'LOW', className: 'low' },
      { label: '中', value: 'MEDIUM', className: 'medium' },
      { label: '高', value: 'HIGH', className: 'high' },
      { label: '紧急', value: 'URGENT', className: 'urgent' }
    ],
    typeOptions: [
      { label: '维修保养', value: 'MAINTENANCE' },
      { label: '告警消缺', value: 'ALERT_FIX' },
      { label: '巡检', value: 'INSPECTION' }
    ],
    selectedAreaName: '',
    selectedDeviceName: ''
  },

  onLoad() {
    this.setData(getNavMetrics());
    this.loadOptions();
  },

  loadOptions() {
    this.setData({ loadingOptions: true });
    Promise.all([
      request({ url: '/api/areas', method: 'GET' }),
      request({ url: '/api/devices?page=1&size=200', method: 'GET' })
    ]).then(([areasRes, devicesRes]) => {
      const areaOptions = flattenAreas(areasRes.data || []);
      const { list } = unwrapPageData(devicesRes);
      const deviceOptions = list.map((device) => ({
        id: device.id,
        name: device.deviceName || device.name || `设备 ${device.id}`,
        areaId: device.areaId,
        locationDesc: device.locationDesc || device.location || ''
      }));

      this.setData({
        areaOptions,
        deviceOptions,
        filteredDevices: deviceOptions,
        loadingOptions: false
      });
    }).catch((error) => {
      this.setData({ loadingOptions: false });
      console.error('[create-order] options failed', error);
    });
  },

  goBack() {
    wx.navigateBack({
      fail: () => wx.switchTab({ url: '/pages/workbench/workbench' })
    });
  },

  onTitleInput(e) {
    this.setData({ 'form.title': e.detail.value });
  },

  onDescriptionInput(e) {
    this.setData({ 'form.description': e.detail.value });
  },

  onLocationInput(e) {
    this.setData({ 'form.locationDetail': e.detail.value });
  },

  choosePriority(e) {
    this.setData({ 'form.priority': e.currentTarget.dataset.value });
  },

  chooseType(e) {
    this.setData({ 'form.workType': e.currentTarget.dataset.value });
  },

  onAreaChange(e) {
    const areaIndex = Number(e.detail.value);
    const area = this.data.areaOptions[areaIndex];
    let filteredDevices = this.data.deviceOptions.filter((device) => !area || device.areaId === area.id);
    if (!filteredDevices.length && area) {
      filteredDevices = this.data.deviceOptions;
    }

    this.setData({
      areaIndex,
      deviceIndex: -1,
      filteredDevices,
      'form.warehouseId': area ? area.id : null,
      'form.deviceId': null,
      selectedAreaName: area ? area.name.replace(/\u3000/g, '') : '',
      selectedDeviceName: ''
    });
  },

  onDeviceChange(e) {
    const deviceIndex = Number(e.detail.value);
    const device = this.data.filteredDevices[deviceIndex];
    this.setData({
      deviceIndex,
      'form.deviceId': device ? device.id : null,
      selectedDeviceName: device ? device.name : '',
      'form.locationDetail': device && !this.data.form.locationDetail ? device.locationDesc : this.data.form.locationDetail
    });
  },

  validateForm() {
    const form = this.data.form;
    if (!form.title.trim()) return '请输入工单标题';
    if (!form.warehouseId) return '请选择发生库区';
    if (!form.description.trim() || form.description.trim().length < 6) return '请填写至少6个字的情况描述';
    return '';
  },

  submitOrder() {
    const message = this.validateForm();
    if (message) {
      wx.showToast({ title: message, icon: 'none' });
      return;
    }

    const user = getCachedUser();
    const form = this.data.form;
    const body = {
      title: form.title.trim(),
      description: form.description.trim(),
      priority: form.priority,
      workType: form.workType,
      warehouseId: form.warehouseId,
      deviceId: form.deviceId,
      locationDetail: form.locationDetail.trim(),
      assigneeId: user.id || user.userId,
      reporterId: user.id || user.userId
    };

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });

    request({
      url: '/api/work-orders',
      method: 'POST',
      data: body
    }).then((res) => {
      wx.hideLoading();
      this.setData({ submitting: false });
      wx.setStorageSync('workOrderListDirty', true);
      wx.showToast({ title: '报修已提交', icon: 'success' });
      const id = res.data && res.data.id;
      setTimeout(() => {
        if (id) {
          wx.redirectTo({ url: `/pages/workorder/detail/detail?id=${id}` });
        } else {
          wx.switchTab({ url: '/pages/workorder/workorder' });
        }
      }, 500);
    }).catch((error) => {
      wx.hideLoading();
      this.setData({ submitting: false });
      console.error('[create-order] submit failed', error);
    });
  }
});
