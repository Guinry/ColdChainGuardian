const isPresent = (value) => value !== undefined && value !== null && value !== '';

const toDate = (value) => {
  if (!isPresent(value)) return null;
  if (typeof value === 'number') return new Date(value);
  if (value instanceof Date) return value;
  const normalized = String(value).replace(' ', 'T');
  const date = new Date(normalized);
  return Number.isNaN(date.getTime()) ? null : date;
};

const pad2 = (value) => `${value}`.padStart(2, '0');

const coalesce = (...values) => values.find(isPresent);

export const getNavMetrics = () => {
  try {
    const menuInfo = wx.getMenuButtonBoundingClientRect();
    return {
      paddingTop: menuInfo.top || 44,
      capsuleHeight: menuInfo.height || 32,
      navHeight: (menuInfo.bottom || 76) + 10
    };
  } catch (error) {
    return {
      paddingTop: 44,
      capsuleHeight: 32,
      navHeight: 86
    };
  }
};

export const getCachedUser = () => wx.getStorageSync('userInfo') || {};

export const buildQuery = (params = {}) => Object.keys(params)
  .filter((key) => isPresent(params[key]))
  .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
  .join('&');

export const formatDateTime = (value, fallback = '--') => {
  const date = toDate(value);
  if (!date) return fallback;
  return `${date.getMonth() + 1}月${date.getDate()}日 ${pad2(date.getHours())}:${pad2(date.getMinutes())}`;
};

export const formatDate = (value, fallback = '--') => {
  const date = toDate(value);
  if (!date) return fallback;
  return `${date.getMonth() + 1}月${date.getDate()}日`;
};

export const formatRelativeTime = (value) => {
  const date = toDate(value);
  if (!date) return '--';

  const diff = Date.now() - date.getTime();
  if (diff < 0) return formatDateTime(value);

  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return '刚刚';
};

export const priorityMap = {
  LOW: { className: 'low', text: '低', rank: 1 },
  MEDIUM: { className: 'medium', text: '中', rank: 2 },
  HIGH: { className: 'high', text: '高', rank: 3 },
  URGENT: { className: 'urgent', text: '紧急', rank: 4 },
  CRITICAL: { className: 'urgent', text: '严重', rank: 4 }
};

export const normalizePriority = (value, fallback = 'MEDIUM') => {
  const key = String(value || fallback).toUpperCase();
  return priorityMap[key] || priorityMap[fallback] || priorityMap.MEDIUM;
};

export const alertTypeText = {
  TEMP_HIGH: '温度过高',
  TEMP_LOW: '温度过低',
  HUMI_HIGH: '湿度过高',
  HUMI_LOW: '湿度过低',
  HUMIDITY_HIGH: '湿度过高',
  HUMIDITY_LOW: '湿度过低',
  DEVICE_OFFLINE: '设备离线',
  SENSOR_ERROR: '传感器异常',
  POWER_FAILURE: '供电异常'
};

export const alertStatusText = {
  UNHANDLED: '未处理',
  HANDLING: '处理中',
  RESOLVED: '已恢复',
  IGNORED: '已忽略',
  CLOSED: '已关闭'
};

export const workOrderStatusText = {
  PENDING: '待接收',
  PROCESSING: '处理中',
  VERIFYING: '待验收',
  COMPLETED: '已完成',
  CLOSED: '已关闭'
};

export const workTypeText = {
  ALERT_FIX: '告警消缺',
  ALERT_DEFECT: '告警消缺',
  INSPECTION: '巡检',
  ROUTINE_INSPECTION: '巡检',
  MAINTENANCE: '维修保养',
  EQUIPMENT_MAINTENANCE: '设备维护'
};

export const normalizeAlert = (raw = {}, fallbackStatus = 'UNHANDLED') => {
  const alertType = coalesce(raw.alertType, raw.type);
  const level = coalesce(raw.alertLevel, raw.alert_level, raw.level, raw.severityLevel);
  const priority = normalizePriority(level === 4 ? 'CRITICAL' : level === 3 ? 'HIGH' : level === 2 ? 'MEDIUM' : level);
  const status = coalesce(raw.status, raw.alertStatus, raw.alert_status, raw.resolved === true ? 'RESOLVED' : '', fallbackStatus);
  const temperature = coalesce(raw.temperature, raw.temp);
  const humidity = coalesce(raw.humidity, raw.humi);
  const thresholdValue = coalesce(raw.thresholdValue, raw.threshold_value);
  const createdTime = coalesce(raw.createdTime, raw.createTime, raw.created_at, raw.createdAt, raw.timestamp, raw.time);
  const areaName = coalesce(raw.areaName, raw.area, raw.warehouseName, raw.location);
  const deviceName = coalesce(raw.deviceName, raw.device, raw.deviceCode);
  const location = [areaName, deviceName].filter(isPresent).join(' - ')
    || raw.description
    || [raw.warehouseId ? `库区 ${raw.warehouseId}` : '', raw.deviceId ? `设备 ${raw.deviceId}` : ''].filter(Boolean).join('，')
    || '未标注位置';

  let currentValue = '--';
  if (alertType === 'TEMP_HIGH' || alertType === 'TEMP_LOW') {
    currentValue = isPresent(temperature) ? `${temperature}℃` : '--';
  } else if (alertType === 'HUMI_HIGH' || alertType === 'HUMI_LOW' || alertType === 'HUMIDITY_HIGH' || alertType === 'HUMIDITY_LOW') {
    currentValue = isPresent(humidity) ? `${humidity}%` : '--';
  }

  return {
    ...raw,
    id: raw.id,
    title: `${alertTypeText[alertType] || raw.description || '系统告警'}告警`,
    typeText: alertTypeText[alertType] || '系统告警',
    status,
    statusText: alertStatusText[status] || status || '未知',
    priority: priority.className,
    priorityText: priority.text,
    currentValue,
    thresholdValue: isPresent(thresholdValue) ? thresholdValue : '--',
    time: formatRelativeTime(createdTime),
    fullTime: formatDateTime(createdTime),
    location
  };
};

export const normalizeWorkOrder = (raw = {}) => {
  const status = coalesce(raw.status, 'PENDING');
  const priority = normalizePriority(raw.priority);
  const due = coalesce(raw.dueDate, raw.dueTime);
  const dueDate = toDate(due);
  const isOverdue = dueDate && dueDate.getTime() < Date.now() && !['COMPLETED', 'CLOSED'].includes(status);
  const location = coalesce(
    raw.locationDetail,
    [raw.warehouseName || (raw.warehouseId ? `库区 ${raw.warehouseId}` : ''), raw.deviceName || (raw.deviceId ? `设备 ${raw.deviceId}` : '')].filter(Boolean).join('，'),
    '未指定位置'
  );

  return {
    ...raw,
    id: raw.id,
    type: workTypeText[raw.workType || raw.orderType] || raw.workType || '工单',
    status,
    statusText: workOrderStatusText[status] || status,
    statusClass: String(status).toLowerCase(),
    priority: priority.className,
    priorityText: priority.text,
    title: raw.title || '未命名工单',
    description: raw.description || '暂无描述',
    location,
    deadline: dueDate ? `${formatDateTime(due)} 截止` : '暂无截止时间',
    createdTimeText: formatDateTime(coalesce(raw.createdAt, raw.createTime)),
    updatedTimeText: formatDateTime(coalesce(raw.updatedAt, raw.updateTime)),
    isOverdue,
    canAccept: status === 'PENDING',
    canSubmit: status === 'PROCESSING',
    readonly: ['VERIFYING', 'COMPLETED', 'CLOSED'].includes(status)
  };
};

export const normalizeLog = (raw = {}) => ({
  ...raw,
  actionText: {
    CREATED: '创建工单',
    ACCEPTED: '接收工单',
    STARTED: '开始处理',
    COMPLETED: '提交验收',
    VERIFIED: '验收通过',
    CLOSED: '关闭工单',
    REJECTED: '退回处理',
    STATUS_CHANGED: '状态变更'
  }[raw.action] || raw.action || '操作记录',
  timeText: formatDateTime(coalesce(raw.createdAt, raw.createTime, raw.create_time)),
  operatorText: raw.operatorName || '系统',
  remarkText: raw.remark || '无备注'
});
