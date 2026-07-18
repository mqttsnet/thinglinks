export default {
  table: { title: 'Bridge Rule List' },
  id: 'ID',
  appId: 'AppId',
  ruleName: 'Rule Name',
  ruleCode: 'Rule Code',
  direction: 'Direction',
  dataSourceId: 'Data Source',
  matchConfigJson: 'Match Conditions',
  actionConfigJson: 'Action Config',
  qos: 'QoS',
  rateLimitQps: 'Rate Limit QPS',
  retryMaxTimes: 'Retry Max Times',
  retryBackoffMs: 'Retry Backoff (ms)',
  timeoutMs: 'Timeout (ms)',
  deadLetterDataSourceId: 'Dead Letter Source',
  enable: 'Enabled',
  priority: 'Priority',
  extendParams: 'Extra Params',
  remark: 'Remark',
  createdTime: 'Created Time',
  createdBy: 'Created By',
  updatedTime: 'Updated Time',
  updatedBy: 'Updated By',
  switchView: 'Switch View',
  group: {
    basic: 'Basic Info',
    match: 'Match Conditions',
    action: 'Action Config',
    fallback: 'Flow Control / Retry Override',
    extras: 'Advanced',
  },
  helpMessage: {
    ruleName: 'Friendly rule name shown in lists & cards',
    ruleCode: 'Business code referenced by external systems; auto-generated if blank',
    appId: 'Owning application scope, for multi-tenant rule isolation',
    direction: 'Outbound = platform → 3rd party; Inbound = 3rd party → platform',
    dataSourceId: 'Pick existing data source; the "Action Config" section adapts to its protocol type',
    qos: 'Rule-level QoS override; NULL = inherit data source default_qos',
    rateLimitQps: 'Rule-level QPS limit override; NULL = inherit; 0 = unlimited',
    retryMaxTimes: 'Rule-level max retries override; NULL = inherit data source default',
    retryBackoffMs: 'Rule-level initial backoff override; NULL = inherit data source default',
    timeoutMs: 'Rule-level send timeout override; NULL = inherit data source default',
    priority: 'Lower = matched first; ties broken by this value',
    remark: 'Optional, for business reference',
    overrideTip: 'NULL fields inherit data source defaults; the "Effective" column shows runtime values',
  },
  placeholder: {
    ruleName: 'e.g. Temp Alarm → Business A Kafka',
    ruleCode: 'Auto-generated if blank',
    fallbackNullDefault: 'NULL = inherit data source default',
    remark: 'Optional, for business reference',
  },
  card: {
    nameFallback: 'Unnamed Rule',
  },
  detail: {
    title: 'Bridge Rule Details',
    metric: {
      direction: 'Direction',
      dataSource: 'Data Source',
      dataSourceName: 'Source name',
      priority: 'Priority',
      enable: 'Status',
    },
    tabs: {
      match: 'Match',
      action: 'Action',
      override: 'Override',
      stats: '24h Stats',
      log: 'Recent Logs',
      meta: 'Metadata',
    },
    runTest: 'Test Send',
    refresh: 'Refresh',
    edit: 'Edit',
    enableSuccess: 'Enabled',
    disableSuccess: 'Disabled',
    overrideHint:
      'NULL = inherit the data source default; non-empty overrides it. The "Effective" column shows the runtime value.',
    actualEffective: 'Effective',
    field: 'Field',
    ruleValue: 'Rule Value',
    dsDefault: 'Data Source Default',
    statsEmpty: 'No 24h statistics',
    stats: {
      title: 'Execution distribution (by hour)',
      success: 'Success',
      failed: 'Failed',
      deadLetter: 'Dead Letter',
      avgLatency: 'Avg Latency',
      count: 'Count',
    },
    logEmpty: 'No logs',
    viewAllLogs: 'View All Logs',
    encryptedHint: 'Action config is encrypted at rest; sensitive fields are masked in production',
  },
  action: {
    add: 'New Rule',
    edit: 'Edit Rule',
    delete: 'Delete Rule',
    enable: 'Enable',
    disable: 'Disable',
    test: 'Test Send',
    copy: 'Copy',
    detail: 'Detail',
  },
  status: {
    enabled: 'Enabled',
    disabled: 'Disabled',
  },
  tips: {
    enableMustTestPass: 'Please run "Test Send" successfully before enabling.',
    enableSourceRequired: 'Linked data source is not enabled. Enable the data source first.',
    deleteEnabled: 'Cannot delete an enabled rule. Disable it first.',
    actionConfigEncrypted:
      'Note: Action Config is encrypted on disk; list API masks plaintext (prevents inline token leak).',
    testSinkSuccess: 'Send successful',
    testSinkFailed: 'Send failed',
  },
  testSink: {
    sampleLabel: 'Sample envelope (JSON, sent as byte[] body to the sink)',
    samplePlaceholder: 'Enter a JSON envelope with tenantId / productIdentification / deviceIdentification / actionType / topic / payload / ts fields',
    result: 'Send Result',
    latency: 'Latency',
    unknownError: 'Unknown error',
  },
  // ============================== match config schema segments ==============================
  matchConfig: {
    outbound: {
      productIdentifications: {
        label: 'Product identifiers',
        placeholder: 'Type identifier and press Enter; "all" matches all products',
        help: 'At least one; ["all"] = all products (also accepts "*"); comma- or space-separated',
      },
      actionTypes: {
        label: 'Event types',
        placeholder: 'Multi-select; all or empty = match every action',
        help: 'PUBLISH / CONNECT / CLOSE / DISCONNECT etc.; at least one or pick all',
      },
      topicPatterns: {
        label: 'MQTT topic patterns',
        // contains literal ${} placeholders — message function avoids vue-i18n interpolation
        help: () => 'Multi-select; pick from product base topics via the "Pick Topic" button or write a custom MQTT pattern with +/# wildcards and ${app_id}/${device_identification} placeholders. Empty = no topic constraint, any topic matches.',
      },
      payloadKinds: {
        label: 'Payload kinds',
        placeholder: 'Multi-select; empty = consume RAW payload only by default',
        help: 'Controls which payload kind the rule consumes. Empty = consume RAW payload only by default; tick "Thing-model data" (THING_MODEL) to bridge thing-model data.',
      },
      deviceIdentifications: {
        label: 'Device identifier filter',
        placeholder: 'Specific identifiers; empty = no restriction',
        help: 'Optional; only matches listed devices when filled',
      },
      deviceTagsAny: {
        label: 'Device tag (any)',
        placeholder: 'Match if device has any of these tags',
        help: 'Optional; requires the device to carry at least one of the tags',
      },
      payloadFilterType: {
        label: 'Payload filter type',
        help: 'Advanced; filter events by payload content',
      },
      payloadFilterExpression: {
        label: 'Payload filter expression',
        placeholder: '$.temperature > 80',
        help: 'JSON_PATH / SpEL / Groovy expression',
      },
      timeWindowCronExpr: {
        label: 'Time window (cron)',
        placeholder: '0 0 9-18 ? * MON-FRI',
        help: 'Optional; rule only matches when cron condition holds',
      },
    },
    inbound: {
      subscriptionSourceIds: {
        label: 'Subscription sources',
        placeholder: 'Pick the subscription sources this rule consumes (multi)',
        help: 'Multi-select; rule consumes messages from these sources',
      },
      messageFilterType: { label: 'Message filter type' },
      messageFilterExpression: {
        label: 'Filter expression',
        placeholder: "$.event_type == 'data_report'",
      },
    },
    payloadFilterOpts: {
      NONE: 'NONE (no filter)',
      JSON_PATH: 'JSON_PATH',
      SPEL: 'SPEL',
      GROOVY: 'GROOVY',
    },
  },
  // ============================== action config schema segments ==============================
  actionForm: {
    overrideSuffix: ' (override)',
    dsDefaultPlaceholder: 'Empty = inherit data source default',
    enums: {
      INSERT: 'INSERT',
      UPDATE: 'UPDATE',
      UPSERT: 'UPSERT',
      IGNORE: 'IGNORE',
      SYNC: 'SYNC',
      ASYNC: 'ASYNC',
      LPUSH: 'LPUSH',
      RPUSH: 'RPUSH',
      XADD: 'XADD',
      PUBLISH: 'PUBLISH',
      SET: 'SET',
    },
    common: {
      payloadFormat: {
        label: 'Payload format',
        opt_JSON: 'JSON',
        opt_STRING: 'STRING',
        opt_HEX: 'HEX',
        opt_RAW: 'RAW',
      },
      payloadTemplate: {
        label: 'Payload template',
        placeholder: '[event.payload] (default passthrough)',
        help: 'Placeholder template; empty = pass envelope through; e.g. [event.payload] / [tenantId] / [productId]',
      },
    },
    inbound: {
      targetHandler: {
        label: 'Handler',
        help: 'Defines the downstream path for the inbound message',
        opt_MQTT_FORWARD: 'MQTT_FORWARD (impersonate device publish)',
        opt_RAW_INSERT: 'RAW_INSERT (write DeviceAction directly)',
        opt_RULE_TRIGGER: 'RULE_TRIGGER (trigger linkage rule)',
      },
      mqttForward: {
        targetProductIdentification: {
          label: 'Target product identifier',
          placeholder: 'productA',
          help: 'Product the impersonated device belongs to',
        },
        targetTopicTemplate: {
          label: 'Target topic template',
          placeholder: '$thing/up/property/[productId]/[msg.deviceId]',
          help: 'Placeholders: [productId] [msg.xxx]',
        },
        fieldMapping: {
          label: 'Field mapping',
          placeholder: '["sourceField":"device_id","targetField":"deviceIdentification","sourceField":"ts","targetField":"timestamp","transform":"TIMESTAMP_TO_LONG"]',
          help: 'JSON array: 3rd-party field → platform field',
        },
      },
      rawInsert: {
        fieldMapping: {
          label: 'Field mapping',
          placeholder: '["sourceField":"device_id","targetField":"deviceIdentification"]',
          help: 'JSON array; mapping for the DeviceAction table',
        },
      },
      ruleTrigger: {
        triggerRuleId: {
          label: 'Trigger rule ID',
          placeholder: 'Linkage rule ID',
          help: 'Inbound message is dispatched to the rule engine; fill the rule primary key',
        },
      },
    },
    proto: {
      kafka: {
        partitionKey: { label: 'Partition key template', help: 'For consistent-hash partitioning by device ID' },
        headers: { label: 'Custom headers', help: 'JSON' },
      },
      redis: {
        command: { label: 'Write command' },
        keyTemplate: { label: 'Key template' },
        ttlSeconds: { label: 'TTL seconds', placeholder: 'For SET / XADD; 0 = no expiry' },
      },
      rocketmq: {
        tag: { label: 'Tag template' },
        hashKey: { label: 'Ordered hashKey', help: 'Same hashKey routes to the same queue; ensures ordered consumption' },
      },
      rabbitmq: {
        routingKey: { label: 'Routing key' },
        properties: { label: 'Custom properties', help: 'AMQP message properties JSON' },
      },
      mysql: {
        columnMapping: {
          label: 'Column mapping',
          placeholder: '"device_id":"[deviceIdentification]","ts":"[timestamp]","payload":"[payload]"',
          help: 'JSON; empty = inherit data source default',
        },
        onDuplicate: { label: 'Conflict strategy' },
      },
      http: {
        headers: { label: 'Custom headers' },
        queryParams: { label: 'Query params' },
        bodyWrap: { label: 'Body wrap', help: 'false = send payload directly; true = wrap as data: payload' },
      },
      webhook: {
        headers: { label: 'Custom headers' },
      },
      mqtt: {
        topicTemplate: { label: 'Topic template' },
        qos: {
          label: 'QoS',
          opt_0: '0 - fire-forget',
          opt_1: '1 - at-least-once',
          opt_2: '2 - exactly-once',
        },
        retained: { label: 'Retained message' },
      },
      tdengine: {
        superTableOverride: { label: 'Super table' },
        childTableTemplate: { label: 'Child table template' },
        tagsMapping: { label: 'TAGS mapping' },
        columnMapping: { label: 'Column mapping' },
      },
      clickhouse: {
        tableOverride: { label: 'Target table' },
        columnMapping: { label: 'Column mapping' },
      },
      influxdb: {
        measurementOverride: { label: 'Measurement' },
        tagsMapping: { label: 'Tags mapping' },
        fieldsMapping: { label: 'Fields mapping' },
      },
      iotdb: {
        timeseriesTemplateOverride: { label: 'Timeseries template' },
        columnMapping: { label: 'Column mapping' },
      },
      postgresql: {
        tableOverride: { label: 'Target table', placeholder: 'Empty = inherit data source default (schema.table)' },
        columnMapping: {
          label: 'Column mapping',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onConflict: { label: 'Conflict strategy' },
      },
      mongodb: {
        collectionOverride: { label: 'Collection' },
        writeModeOverride: { label: 'Write mode' },
      },
      pulsar: {
        topicOverride: { label: 'Topic' },
        messageKey: { label: 'Message key', help: 'Affects Pulsar routing / Key_Shared subscription' },
        messageProperties: { label: 'Message properties' },
        sendModeOverride: { label: 'Send mode' },
      },
      dm: {
        tableOverride: {
          label: 'Target table',
          placeholder: 'Empty = inherit data source default (schema.table, e.g. IOT.iot_data)',
        },
        columnMapping: {
          label: 'Column mapping',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onDuplicate: { label: 'Conflict strategy' },
      },
      kingbase: {
        tableOverride: { label: 'Target table', placeholder: 'Empty = inherit data source default (schema.table)' },
        columnMapping: {
          label: 'Column mapping',
          placeholder: '"device_id":"[deviceIdentification]","payload":"[payload]"',
        },
        onConflict: { label: 'Conflict strategy' },
      },
    },
  },
};
