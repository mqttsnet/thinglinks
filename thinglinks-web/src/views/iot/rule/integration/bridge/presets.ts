/**
 * 桥接规则的"加载示例配置"预设。
 *
 * <p>按 direction 分两组：
 * <ul>
 *   <li>{@link outboundPresets} ── 平台 → 第三方常见场景（设备数据透传 / 告警分流 / 状态监听等）</li>
 *   <li>{@link inboundPresets} ── 第三方 → 平台常见场景（数据回写 / 跨平台联动）</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 2026-04-28
 */

import type { OutboundExamplePreset, InboundExamplePreset } from './dto';
import { BridgeWildcard } from './dto';

// ============================== 出站预设 ==============================

export const outboundPresets: OutboundExamplePreset[] = [
  {
    key: 'all_publish_kafka',
    label: '所有 PUBLISH → Kafka',
    description: '设备数据全量旁路到第三方 Kafka（最常用，0 配置门槛）',
    requireSourceType: 'KAFKA',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
      // topicPatterns 留空 = 不约束 topic,任意 topic 都命中
    },
    action: {
      payloadFormat: 'JSON',
      payloadTemplate: '${event.payload}',
      kafka: {
        partitionKey: '${deviceIdentification}',
        headers: '{"X-Source":"thinglinks","X-Tenant":"${tenantId}"}',
      },
    },
  },
  {
    key: 'alarm_temperature_kafka',
    label: '温度告警 → Kafka',
    description: 'JSONPath 过滤温度 > 80，触发告警通道',
    requireSourceType: 'KAFKA',
    match: {
      productIdentifications: ['productA'],
      actionTypes: ['PUBLISH'],
      topicPatterns: ['$thing/up/property/+/+'],
      payloadFilter: {
        type: 'JSON_PATH',
        expression: '$.temperature > 80',
      },
    },
    action: {
      payloadFormat: 'JSON',
      kafka: {
        partitionKey: '${productIdentification}',
      },
    },
  },
  {
    key: 'heartbeat_redis',
    label: '心跳事件 → Redis Stream',
    description: 'CONNECT/DISCONNECT 事件写 Redis Stream，给监控大屏消费',
    requireSourceType: 'REDIS',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['CONNECT', 'CLOSE', 'DISCONNECT'],
    },
    action: {
      payloadFormat: 'JSON',
      redis: {
        command: 'XADD',
        keyTemplate: 'iot:heartbeat:${tenantId}',
        ttlSeconds: 86400,
      },
    },
  },
  {
    key: 'all_to_http',
    label: '全量 → HTTP 业务接口',
    description: '把所有 PUBLISH 事件 POST 到业务方 HTTP API',
    requireSourceType: 'HTTP',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
      // topicPatterns 留空 = 不约束 topic
    },
    action: {
      payloadFormat: 'JSON',
      http: {
        headers: '{"X-Source":"thinglinks"}',
        bodyWrap: false,
      },
    },
  },
  {
    key: 'webhook_dingtalk',
    label: '告警事件 → 钉钉 Webhook',
    description: '把高温告警发钉钉机器人；带 HMAC_SHA256 签名',
    requireSourceType: 'WEBHOOK',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
      payloadFilter: {
        type: 'JSON_PATH',
        expression: '$.alarm == true',
      },
    },
    action: {
      payloadFormat: 'JSON',
      webhook: {
        headers: '{"Content-Type":"application/json"}',
      },
    },
  },
  {
    key: 'storage_mysql',
    label: '设备数据 → MySQL 历史表',
    description: '所有 PUBLISH 事件按产品分表落 MySQL（数据归档）',
    requireSourceType: 'MYSQL',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      mysql: {
        columnMapping:
          '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
        onDuplicate: 'INSERT',
      },
    },
  },
  {
    key: 'iot_to_tdengine',
    label: 'IoT 数据 → TDengine 时序',
    description: '设备 PUBLISH 数据写入 TDengine 超表（IoT 时序首选）',
    requireSourceType: 'TDENGINE',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      tdengine: {
        childTableTemplate: 'd_${deviceIdentification}',
        tagsMapping: '{"productId":"${productId}","deviceId":"${deviceIdentification}"}',
        columnMapping: '{"ts":"${timestamp}","value":"${payload}"}',
      },
    },
  },
  {
    key: 'iot_to_clickhouse',
    label: 'IoT 数据 → ClickHouse OLAP',
    description: '设备数据写入 ClickHouse 大宽表（实时分析报表场景）',
    requireSourceType: 'CLICKHOUSE',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      clickhouse: {
        columnMapping:
          '{"ts":"${timestamp}","device_id":"${deviceIdentification}","payload":"${payload}"}',
      },
    },
  },
  {
    key: 'iot_to_influxdb',
    label: 'IoT 数据 → InfluxDB v2',
    description: '设备数据写入 InfluxDB v2 measurement（OSS 时序生态）',
    requireSourceType: 'INFLUXDB',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      influxdb: {
        tagsMapping: '{"productId":"${productId}","deviceId":"${deviceIdentification}"}',
        fieldsMapping: '{"value":"${payload}"}',
      },
    },
  },
  {
    key: 'iot_to_iotdb',
    label: 'IoT 数据 → Apache IoTDB',
    description: '设备数据写入 IoTDB 时序（边缘 + 云时序场景）',
    requireSourceType: 'IOTDB',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      iotdb: {
        timeseriesTemplateOverride: 'root.iot.${productId}.${deviceIdentification}',
        columnMapping: '{"value":"${payload}"}',
      },
    },
  },
  {
    key: 'iot_to_postgresql',
    label: 'IoT 数据 → PostgreSQL UPSERT',
    description: '设备数据 UPSERT 写入 PostgreSQL（按 device_id+ts 主键幂等）',
    requireSourceType: 'POSTGRESQL',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      postgresql: {
        columnMapping:
          '{"device_id":"${deviceIdentification}","ts":"${timestamp}","payload":"${payload}"}',
        onConflictOverride: 'UPSERT',
      },
    },
  },
  {
    key: 'iot_to_mongodb',
    label: 'IoT 数据 → MongoDB UPSERT',
    description: '按设备 UPSERT 到 MongoDB（保留每设备最新一条）',
    requireSourceType: 'MONGODB',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      mongodb: { writeModeOverride: 'UPSERT' },
    },
  },
  {
    key: 'iot_to_pulsar',
    label: 'IoT 数据 → Apache Pulsar',
    description: '设备数据投递到 Pulsar topic（多租户高吞吐场景）',
    requireSourceType: 'PULSAR',
    match: {
      productIdentifications: [BridgeWildcard.ALL],
      actionTypes: ['PUBLISH'],
    },
    action: {
      payloadFormat: 'JSON',
      pulsar: {
        messageKey: '${deviceIdentification}',
        sendModeOverride: 'ASYNC',
      },
    },
  },
];

// ============================== 入站预设 ==============================

export const inboundPresets: InboundExamplePreset[] = [
  {
    key: 'mqtt_forward',
    label: 'MQTT 转发（伪装设备 publish）',
    description: '把第三方消息伪装成平台设备 publish，复用现有数据链路',
    match: {
      subscriptionSourceIds: [],
    },
    action: {
      targetHandler: 'MQTT_FORWARD',
      targetProductIdentification: 'productA',
      targetTopicTemplate: '$thing/up/property/${productId}/${msg.deviceId}',
      fieldMapping: JSON.stringify(
        [
          { sourceField: 'device_id', targetField: 'deviceIdentification' },
          { sourceField: 'ts', targetField: 'timestamp', transform: 'TIMESTAMP_TO_LONG' },
        ],
        null,
        2,
      ),
    },
  },
  {
    key: 'raw_insert',
    label: '直接写 DeviceAction',
    description: '不走协议链路，直接写 device_action 表（适合外部历史数据回填）',
    match: {
      subscriptionSourceIds: [],
    },
    action: {
      targetHandler: 'RAW_INSERT',
      fieldMapping: JSON.stringify(
        [{ sourceField: 'device_id', targetField: 'deviceIdentification' }],
        null,
        2,
      ),
    },
  },
  {
    key: 'rule_trigger',
    label: '触发场景联动',
    description: '把入站消息丢给规则引擎，触发预定义场景联动',
    match: {
      subscriptionSourceIds: [],
    },
    action: {
      targetHandler: 'RULE_TRIGGER',
    },
  },
];
