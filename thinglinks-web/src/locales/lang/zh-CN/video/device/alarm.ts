export default {
  table: { title: '设备告警信息列表' },
  pageTitle: '告警管理',
  listTitle: '告警列表',
  alarmFallback: '告警',

  id: '唯一标识符',
  deviceIdentification: '设备国标编号',
  channelIdentification: '通道国标编号',
  alarmPriority: '告警级别',
  alarmMethod: '告警方式',
  alarmTime: '告警时间',
  alarmDescription: '告警描述',
  alarmType: '告警类型',
  alarmTypeParam: '告警类型参数',
  longitude: '经度',
  latitude: '纬度',
  handleStatus: '处理状态',
  handleUserId: '处理人',
  handleTime: '处理时间',
  placeholderAllPriority: '全部级别',
  placeholderAllStatus: '全部状态',
  createdTime: '创建时间',

  // 操作按钮
  handle: '处理',
  ignore: '忽略',
  confirm: '确认',
  resolve: '解决',
  back: '返回',
  submit: '提交',
  cancel: '取消',

  // Tooltip
  tooltipView: '查看详情',
  tooltipConfirm: '确认告警',
  tooltipResolve: '解决告警',
  tooltipIgnore: '忽略告警',

  // 处理状态
  status: {
    pending: '待处理',
    processing: '处理中',
    resolved: '已解决',
    ignored: '已忽略',
  },

  // 清空
  clearAll: '清空告警',
  confirmClear: '确认清空该设备的所有告警记录？',
  clearSuccess: '告警已清空',
  handleSuccess: '告警处理成功',
  inputDeviceId: '请输入设备国标编号',
  search: '查询',

  // 处理弹窗
  confirmTitle: '确认告警',
  resolveTitle: '告警处理结果',
  ignoreTitle: '忽略告警',
  inputHandleDesc: '请输入处理说明（必填）',
  inputResolveDesc: '请输入处理结果描述（必填）',
  inputIgnoreReason: '请输入忽略原因（必填）',
  confirmed: '已确认',
  resolved: '已解决',
  ignoredSuccess: '已忽略',
  batchIgnored: '已批量忽略 {count} 条',
  selectAtLeastOne: '请至少选择一条告警',

  // 卡片字段
  cards: {
    deviceId: '设备编号',
    alarmPriority: '告警级别',
    alarmMethod: '告警方式',
    alarmTime: '告警时间',
    alarmDescription: '告警描述',
  },

  // 统计页
  stats: {
    totalCount: '告警总数',
    pendingCount: '待处理',
    todayCount: '今日新增',
    priorityDistribution: '级别分布',
    noData: '暂无',
    onlineLabel: '待处理',
    offlineLabel: '已处理',
    batchIgnore: '批量忽略',
    switchView: '切换视图',
  },

  // WebSocket
  ws: {
    newAlarm: '新告警',
    device: '设备',
    priority: '级别',
    viewDetail: '查看',
  },

  // 详情页
  detail: '告警详情',
  defaultTitle: '告警事件',
  baseInfo: '告警信息',
  handleInfo: '处理信息',
  handleUser: '处理人',
  handleResult: '处理结果',
  noHandleRecord: '暂无处理记录',
  location: '位置',
  viewOnMap: '在地图中查看',
  viewDetail: '查看详情',

  // 相对时间
  justNow: '刚刚',
  minutesAgo: '分钟前',
  hoursAgo: '小时前',
  daysAgo: '天前',
};
