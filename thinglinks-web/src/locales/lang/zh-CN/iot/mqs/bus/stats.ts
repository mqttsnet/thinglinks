export default {
  title: '协议总线统计',
  subtitle: '租户级实时指标(集群汇总)',
  // 统计卡
  health: {
    total: '总事件',
    success: '成功',
    failed: '失败',
    dropped: '主动丢弃',
    noRoute: '路由未命中',
    successRate: '成功率',
  },
  // 维度切片表
  table: {
    label: '标签(protocol:action:group:status)',
    count: '计数',
    stage: '阶段',
    phase: '相位',
    status: '状态',
    targetMq: '目标 MQ',
    targetTopic: '目标 Topic',
    refresh: '刷新',
    autoRefresh: '自动刷新',
    autoRefreshInterval: '刷新间隔(秒)',
  },
  // 测试 tab
  test: {
    title: '协议总线测试',
    subtitle: '手动触发 dispatcher 跑一遍 pipeline,用于联调',
    sourceTopic: '来源 Topic',
    rawJson: '原始 JSON 报文',
    submit: '触发',
    placeholder: '粘贴 BifroMQ Kafka JSON,如 { "clientId":"xxx", "topic":"...", "payload":"base64..." }',
    outcome: '执行结果',
    outcomeStatus: '状态',
    outcomeLatency: '总耗时(ms)',
    outcomeStages: 'Stage 执行链',
    outcomeFailureReason: '失败原因',
    success: '触发成功',
    fail: '触发失败',
  },
  // 路由
  route: {
    stats: '总线统计',
    test: '总线测试',
  },
  empty: '暂无数据',
};
