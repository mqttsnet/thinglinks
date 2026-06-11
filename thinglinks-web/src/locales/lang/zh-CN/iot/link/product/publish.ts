export default {
  modalTitle: '发布产品版本',
  currentVersion: '当前版本',
  hint: '系统将基于当前物模型生成一个不可变快照,设备数据按所选策略路由。',

  // 步骤标题
  stepStrategy: '选择发布策略',
  stepConfig: '策略配置',
  stepRemark: '发布说明',

  // 策略
  strategyLabel: {
    0: '全量发布',
    1: '灰度发布',
    2: '影子发布',
  },
  strategyDesc: {
    0: '一次性切给所有设备',
    1: '按设备白名单放量',
    2: '旁路验证,不影响生产',
  },

  // FULL / SHADOW 提示卡
  fullTipTitle: '全量切流',
  fullTipDesc: '新版本将立即生效到该产品下的全部在线设备,请确认变更影响范围。',
  shadowTipTitle: '影子模式',
  shadowTipDesc: '新版本仅作影子写入,设备真实流量仍走当前版本,适合上线前对照验证。',

  // 灰度配置
  canaryConfig: '灰度配置',
  canarySourceGroup: '按分组',
  canarySourceManual: '按设备识别码',
  canarySourcePercent: '按比例',
  canarySourceGroupTip: '选择设备分组,系统会自动拉取该分组下的全部设备作为灰度白名单。',
  canarySourceManualTip: '手动粘贴设备标识,换行或逗号分隔。',
  canarySourcePercentTip:
    '按设备总量的百分比一致性哈希挑选(同设备在相同比例下结果稳定,逐步放量不会反复横跳)。',

  // 分组面板
  groupSelectPlaceholder: '请选择设备分组',
  groupDevicePreview: '将放量到该分组下的 {count} 台设备',
  groupEmpty: '该分组下暂无设备,请重新选择',
  groupLoadFailed: '分组设备加载失败',

  // 手动面板
  deviceWhitelist: '设备白名单',
  deviceWhitelistTip: '设备标识用换行或逗号分隔',

  // 汇总
  canarySummary: '已选 {count} 台设备',
  canaryPercentSummary: '将按 {percent}% 一致性哈希命中产品下的设备',
  canaryPercentQuick: '快捷',
  canaryEmpty: '尚未选择灰度设备',

  // 发布说明
  publishRemark: '发布说明',
  publishRemarkPlaceholder: '可选,描述本次变更的目的',

  // 校验 / 反馈
  noStrategy: '请先选择发布策略',
  noCanaryDevice: '灰度发布至少需要 1 台设备',
  noCanaryPercent: '灰度比例需在 1% ~ 99% 之间',
  success: '发布成功',
  failed: '发布失败',
};
