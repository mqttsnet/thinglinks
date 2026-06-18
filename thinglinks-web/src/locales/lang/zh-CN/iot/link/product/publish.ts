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
    1: '按分组 / 指定设备放量',
    2: '配合 OTA,升级后自动切到此版本',
  },

  // FULL / SHADOW 提示卡
  fullTipTitle: '全量切流',
  fullTipDesc: '新版本将立即生效到该产品下的全部在线设备,请确认变更影响范围。',
  shadowTipTitle: '影子版本:配合 OTA 升级使用',
  shadowTipDesc:
    '影子发布只预建好新版本的物模型表占位,不改动任何设备的当前版本。设备通过 OTA 升级到该版本对应的固件 / 软件后,自动切换绑定到此影子版本生效 —— 先备好新模型,设备随固件灰度逐台迁入。',
  // 影子版本使用流程(发布建表 → 关联 OTA → 升级后自动生效)
  shadowFlowTitle: '怎么用影子版本',
  shadowFlowStep1: '发布:生成影子版本并建好数据表占位,暂不动任何设备,现有设备照旧运行。',
  shadowFlowStep2:
    '关联 OTA:到「OTA 资源」里,把对应固件 / 软件升级包的「目标产品版本」选成这个影子版本。',
  shadowFlowStep3:
    '自动生效:设备经 OTA 升级成功(或上报到对应固件 / 软件版本)后,自动切换绑定到此影子版本,数据落入新表。',

  // 策略执行结果(发布记录 / 版本列表战报)
  result: {
    fullSwitched: '台已全量切换',
    canaryHit: '台命中改绑 · 目标 {target} 台',
    shadowPreBuilt: '张超表已预建 · 发布不改绑',
    ofTotalLabel: '占发布时全部设备',
    sourceGroup: '来源 {n} 个分组',
    sourceManual: '来源 设备名单',
    sourcePercent: '比例 {percent}%',
    expandGroups: '展开 {n} 个分组明细',
  },

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
  groupMultiPreview: '已选 {groups} 个分组,合并去重 {count} 台设备',
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

  // 灰度影响范围(发布前明确 blast radius;命中→新版本,其余 + 灰度期新设备→稳定版)
  canaryImpactTitle: '灰度影响范围',
  canaryImpactHitDevices: '命中的 {count} 台设备升级到新版本',
  canaryImpactHitPercent: '约 {percent}% 的设备升级到新版本',
  canaryImpactRest: '其余设备保持当前稳定版 {version}',
  canaryImpactNewDevice: '灰度期间新建 / 导入的设备自动绑定稳定版,不进灰度组',

  // 手填白名单解析反馈
  manualParsed: '已识别 {count} 台设备',
  manualDedupedSuffix: ',已自动去重 {dup} 个重复项',

  // 发布说明
  publishRemark: '发布说明',
  publishRemarkPlaceholder: '可选,描述本次变更的目的',

  // 最大兜底重试次数
  maxRetryLabel: '最大兜底重试次数',
  maxRetryHint: '异步执行失败时后台自动重试上限(默认 3,最多 10)',

  // 校验 / 反馈
  noStrategy: '请先选择发布策略',
  noCanaryDevice: '灰度发布至少需要 1 台设备',
  noCanaryPercent: '灰度比例需在 1% ~ 99% 之间',
  success: '发布成功',
  failed: '发布失败',
};
