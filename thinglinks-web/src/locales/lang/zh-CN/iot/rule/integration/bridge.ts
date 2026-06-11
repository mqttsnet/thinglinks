export default {
  table: { title: '桥接规则列表' },
  id: '主键',
  appId: '应用场景',
  ruleName: '规则名称',
  ruleCode: '规则编码',
  direction: '方向',
  dataSourceId: '关联数据源',
  matchConfigJson: '匹配条件',
  actionConfigJson: '动作配置',
  qos: '可靠性级别',
  rateLimitQps: '限流 QPS',
  retryMaxTimes: '最大重试次数',
  retryBackoffMs: '初始退避时长(ms)',
  timeoutMs: '发送超时(ms)',
  deadLetterDataSourceId: '死信数据源',
  enable: '是否启用',
  priority: '优先级',
  extendParams: '扩展参数',
  remark: '备注',
  createdTime: '创建时间',
  createdBy: '创建人',
  updatedTime: '最后修改时间',
  updatedBy: '最后修改人',
  switchView: '切换视图',
  // 表单分组
  group: {
    basic: '基础信息',
    match: '匹配条件',
    action: '动作配置',
    fallback: '流控/重试覆盖',
    extras: '高级配置',
  },
  // 字段帮助提示
  helpMessage: {
    ruleName: '规则的友好名称，列表 / 卡片视图展示用',
    ruleCode: '规则业务唯一编码，可暴露给外部系统引用；不填则后端自动生成',
    appId: '所属应用场景，用于多租户应用维度的规则隔离',
    direction: '出站=平台 → 第三方；入站=第三方 → 平台',
    dataSourceId: '选择已建数据源；下方"动作配置"按所选数据源的协议类型动态显示',
    qos: '规则级 QoS 覆盖；NULL = 沿用数据源 default_qos',
    rateLimitQps: '规则级 QPS 限流覆盖；NULL = 沿用数据源 default；0 = 不限',
    retryMaxTimes: '规则级最大重试次数覆盖；NULL = 沿用数据源 default',
    retryBackoffMs: '规则级初始退避时长覆盖；NULL = 沿用数据源 default',
    timeoutMs: '规则级单次发送超时覆盖；NULL = 沿用数据源 default',
    priority: '数字越小越先匹配；同事件命中多条时按此排序',
    remark: '可选，便于业务区分用途',
    overrideTip: '所有覆盖项 NULL 时沿用数据源默认值，仅"实际生效"列展示运行时取值',
  },
  placeholder: {
    ruleName: '如：温度告警 → 业务方A Kafka',
    ruleCode: '不填后端自动生成',
    fallbackNullDefault: 'NULL = 沿用数据源默认',
    remark: '可选，便于业务区分用途',
  },
  // 卡片视图
  card: {
    nameFallback: '未命名规则',
  },
  // 详情页
  detail: {
    title: '桥接规则详情',
    metric: {
      direction: '方向',
      dataSource: '关联数据源',
      dataSourceName: '数据源名称',
      priority: '优先级',
      enable: '启用状态',
    },
    tabs: {
      match: '匹配条件',
      action: '动作配置',
      override: '流控覆盖',
      stats: '24h 统计',
      log: '最近日志',
      meta: '元数据',
    },
    runTest: '测试发送',
    refresh: '刷新',
    edit: '编辑',
    enableSuccess: '已启用',
    disableSuccess: '已禁用',
    overrideHint: 'NULL = 沿用数据源同名默认值，非空则覆盖；右列"实际生效"展示最终运行时取值',
    actualEffective: '实际生效',
    field: '字段',
    ruleValue: '规则值',
    dsDefault: '数据源默认',
    statsEmpty: '暂无 24h 统计数据',
    stats: {
      title: '执行结果分布（按小时）',
      success: '成功',
      failed: '失败',
      deadLetter: '死信',
      avgLatency: '平均耗时',
      count: '次数',
    },
    logEmpty: '暂无日志',
    viewAllLogs: '查看全部日志',
    encryptedHint: '动作配置整体加密落盘；密钥相关字段在生产环境会自动脱敏',
  },
  // 操作 / 状态
  action: {
    add: '新增规则',
    edit: '编辑规则',
    delete: '删除规则',
    enable: '启用',
    disable: '禁用',
    test: '测试发送',
    copy: '复制',
    detail: '查看详情',
  },
  status: {
    enabled: '已启用',
    disabled: '未启用',
  },
  tips: {
    enableMustTestPass: '请先点击【测试发送】通过后才能启用',
    enableSourceRequired: '关联数据源未启用，请先启用数据源',
    deleteEnabled: '启用中的规则不允许删除，请先禁用',
    actionConfigEncrypted: '注：动作配置落盘时加密，列表接口已屏蔽明文（防内联 token 泄漏）',
    testSinkSuccess: '发送成功',
    testSinkFailed: '发送失败',
  },
  testSink: {
    sampleLabel: '样例 envelope（JSON 格式，作为 byte[] body 发送到下游）',
    samplePlaceholder: '请输入 JSON envelope,需含 tenantId / productIdentification / deviceIdentification / actionType / topic / payload / ts 字段',
    result: '发送结果',
    latency: '耗时',
    unknownError: '未知错误',
  },
  // ============================== match config schema 段 ==============================
  matchConfig: {
    outbound: {
      productIdentifications: {
        label: '产品标识',
        placeholder: '输入产品标识，回车确认；输入 all 表示所有产品',
        help: '至少 1 个；["all"] 表示所有产品（兼容 "*"）；多产品用逗号或空格分隔',
      },
      actionTypes: {
        label: '事件类型',
        placeholder: '复选；选 all 或不选 = 全部命中',
        help: 'PUBLISH / CONNECT / CLOSE / DISCONNECT 等；至少 1 个或选 all',
      },
      topicPatterns: {
        label: 'MQTT 主题模式',
        help: '多选;通过"选择主题"按钮从产品基础 topic 中挑选,或自定义带 +/# 通配符及 ${app_id}/${device_identification} 等占位符的主题模板。空 = 不约束 topic,任意 topic 都命中。',
      },
      payloadKinds: {
        label: '数据形态',
        placeholder: '多选;不选 = 默认只消费原始报文',
        help: '控制规则消费哪种数据形态。不选 = 默认只消费原始报文(RAW);要桥接物模型数据须勾选「物模型数据」(THING_MODEL)。',
      },
      deviceIdentifications: {
        label: '设备标识过滤',
        placeholder: '指定设备标识；不填 = 不限',
        help: '可选；填则只匹配列出的设备',
      },
      deviceTagsAny: {
        label: '设备标签命中',
        placeholder: '设备任一标签命中即可',
        help: '可选；填则要求设备至少含其中一个标签',
      },
      payloadFilterType: {
        label: '内容过滤类型',
        help: '高级；按 payload 内容过滤事件',
      },
      payloadFilterExpression: {
        label: '内容过滤表达式',
        placeholder: '$.temperature > 80',
        help: 'JSON_PATH / SpEL / Groovy 表达式',
      },
      timeWindowCronExpr: {
        label: '时间窗口 (cron)',
        placeholder: '0 0 9-18 ? * MON-FRI',
        help: '可选；填则仅匹配满足 cron 的时间窗口',
      },
    },
    inbound: {
      subscriptionSourceIds: {
        label: '关联订阅源',
        placeholder: '选择此规则要消费的订阅源（多选）',
        help: '多选；规则将消费这些订阅源拉来的消息',
      },
      messageFilterType: { label: '消息过滤类型' },
      messageFilterExpression: {
        label: '过滤表达式',
        placeholder: "$.event_type == 'data_report'",
      },
    },
    payloadFilterOpts: {
      NONE: 'NONE（不过滤）',
      JSON_PATH: 'JSON_PATH',
      SPEL: 'SPEL',
      GROOVY: 'GROOVY',
    },
  },
  // ============================== action config schema 段 ==============================
  actionForm: {
    // 反复出现的占位文案（统一抽出）
    overrideSuffix: '（覆盖）',
    dsDefaultPlaceholder: '不填 = 沿用数据源默认',
    // 共用 enum
    enums: {
      INSERT: 'INSERT',
      UPDATE: 'UPDATE',
      UPSERT: 'UPSERT',
      IGNORE: 'IGNORE',
      SYNC: 'SYNC（同步）',
      ASYNC: 'ASYNC（异步）',
      LPUSH: 'LPUSH',
      RPUSH: 'RPUSH',
      XADD: 'XADD',
      PUBLISH: 'PUBLISH',
      SET: 'SET',
    },
    // 出站通用 head
    common: {
      payloadFormat: {
        label: '负载格式',
        opt_JSON: 'JSON',
        opt_STRING: 'STRING',
        opt_HEX: 'HEX',
        opt_RAW: 'RAW',
      },
      payloadTemplate: {
        label: '负载模板',
        placeholder: '[event.payload]（默认透传）',
        help: '占位符模板；空则直传 envelope；常用：[event.payload] / [tenantId] / [productId]',
      },
    },
    // 入站 action
    inbound: {
      targetHandler: {
        label: '处理方式',
        help: '决定入站消息的下游链路',
        opt_MQTT_FORWARD: 'MQTT_FORWARD（伪装设备 publish）',
        opt_RAW_INSERT: 'RAW_INSERT（直写 DeviceAction）',
        opt_RULE_TRIGGER: 'RULE_TRIGGER（触发场景联动）',
      },
      mqttForward: {
        targetProductIdentification: {
          label: '目标产品标识',
          placeholder: 'productA',
          help: '伪装的设备所属产品',
        },
        targetTopicTemplate: {
          label: '目标 Topic 模板',
          placeholder: '$thing/up/property/[productId]/[msg.deviceId]',
          help: '占位符可用 [productId] [msg.xxx]',
        },
        fieldMapping: {
          label: '字段映射',
          placeholder: '["sourceField":"device_id","targetField":"deviceIdentification","sourceField":"ts","targetField":"timestamp","transform":"TIMESTAMP_TO_LONG"]',
          help: 'JSON 数组：第三方字段 → 平台字段映射',
        },
      },
      rawInsert: {
        fieldMapping: {
          label: '字段映射',
          placeholder: '["sourceField":"device_id","targetField":"deviceIdentification"]',
          help: 'JSON 数组；DeviceAction 表字段映射',
        },
      },
      ruleTrigger: {
        triggerRuleId: {
          label: '触发规则 ID',
          placeholder: '场景联动规则 ID',
          help: '把入站消息丢给规则引擎触发；填规则主键 ID',
        },
      },
    },
    // 各协议出站特异字段
    proto: {
      kafka: {
        partitionKey: { label: '分区键模板', help: '按设备 ID 一致性 hash 分区时用' },
        headers: { label: '自定义 Headers', help: 'JSON 格式' },
      },
      redis: {
        command: { label: '写入命令' },
        keyTemplate: { label: 'Key 模板' },
        ttlSeconds: { label: 'TTL 秒', placeholder: 'SET / XADD 时；秒；0 = 不过期' },
      },
      rocketmq: {
        tag: { label: 'Tag 模板' },
        hashKey: { label: '顺序消息 hashKey', help: '同 hashKey 进同一队列；保证顺序消费' },
      },
      rabbitmq: {
        routingKey: { label: 'Routing Key' },
        properties: { label: '自定义 Properties', help: 'AMQP message properties JSON' },
      },
      mysql: {
        columnMapping: {
          label: '字段映射',
          placeholder: '"device_id":"[deviceIdentification]","ts":"[timestamp]","payload":"[payload]"',
          help: 'JSON 格式；不填 = 沿用数据源默认',
        },
        onDuplicate: { label: '冲突策略' },
      },
      http: {
        headers: { label: '自定义 Headers' },
        queryParams: { label: 'Query Params' },
        bodyWrap: { label: 'Body 包装', help: 'false=直接发 payload；true=包一层 data: payload' },
      },
      webhook: {
        headers: { label: '自定义 Headers' },
      },
      mqtt: {
        topicTemplate: { label: 'Topic 模板' },
        qos: {
          label: 'QoS',
          opt_0: '0 - fire-forget',
          opt_1: '1 - at-least-once',
          opt_2: '2 - exactly-once',
        },
        retained: { label: '保留消息' },
      },
      tdengine: {
        superTableOverride: { label: '超表' },
        childTableTemplate: { label: '子表命名模板' },
        tagsMapping: { label: 'TAGS 映射' },
        columnMapping: { label: '字段映射' },
      },
      clickhouse: {
        tableOverride: { label: '目标表' },
        columnMapping: { label: '字段映射' },
      },
      influxdb: {
        measurementOverride: { label: 'Measurement' },
        tagsMapping: { label: 'Tags 映射' },
        fieldsMapping: { label: 'Fields 映射' },
      },
      iotdb: {
        timeseriesTemplateOverride: { label: '时序路径模板' },
        columnMapping: { label: '字段映射' },
      },
      postgresql: {
        tableOverride: { label: '目标表', placeholder: '不填 = 沿用数据源默认（schema.table）' },
        columnMapping: {
          label: '字段映射',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onConflict: { label: '冲突策略' },
      },
      mongodb: {
        collectionOverride: { label: 'Collection' },
        writeModeOverride: { label: '写入模式' },
      },
      pulsar: {
        topicOverride: { label: 'Topic' },
        messageKey: { label: '消息 Key', help: '影响 Pulsar 路由 / Key_Shared 订阅' },
        messageProperties: { label: 'Message Properties' },
        sendModeOverride: { label: '发送模式' },
      },
      dm: {
        tableOverride: {
          label: '目标表',
          placeholder: '不填 = 沿用数据源默认（schema.table，如 IOT.iot_data）',
        },
        columnMapping: {
          label: '字段映射',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onDuplicate: { label: '冲突策略' },
      },
      kingbase: {
        tableOverride: { label: '目标表', placeholder: '不填 = 沿用数据源默认（schema.table）' },
        columnMapping: {
          label: '字段映射',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onConflict: { label: '冲突策略' },
      },
    },
  },
};
