/**
 * 桥接规则的 match_config_json / action_config_json DTO 定义。
 *
 * <p>对应方案 §5.2.3 / §5.2.4。结构按 direction 异构（出站 vs 入站），
 * 出站的 action_config 还按所选 DataSource 的 sourceType 异构。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */

import { BizConstant } from '/@/enums/biz/common';

// ============================== 通用常量 ==============================

/**
 * 桥接规则通配符常量(本模块为兼容历史命名保留,实际值复用全局 {@link BizConstant})。
 *
 * <p>{@code 'all'} 是主形式,与后端 {@code BizConstant.ALL} 对齐(项目全局通用,Video/Bridge 共用)。
 * <p>{@code '*'} 是 MQTT 行业惯例兼容形式。前后端统一小写,后端严格匹配。
 */
export const BridgeWildcard = {
  /** 匹配所有(主形式,与后端 BizConstant.ALL 对齐) */
  ALL: BizConstant.ALL,
  /** 匹配所有(MQTT 行业惯例形式) */
  STAR: BizConstant.STAR,
} as const;

// ============================== 通用嵌套 DTO ==============================

/** 出站规则 ── 设备过滤器 */
export interface DeviceFilterDto {
  deviceIdentifications?: string[];
  tagsAny?: string[];
  groupIds?: number[];
}

/** 内容过滤器（payload 级 / 入站消息过滤） */
export interface PayloadFilterDto {
  type?: 'JSON_PATH' | 'SPEL' | 'GROOVY' | 'NONE';
  expression?: string;
}

/** 时间窗口过滤器 */
export interface TimeWindowDto {
  cronExpr?: string;
}

/** 入站规则 ── 字段映射条目 */
export interface FieldMappingDto {
  sourceField: string;
  targetField: string;
  transform?: string;
}

// ============================== 出站 match_config_json ==============================

export interface OutboundMatchConfigDto {
  productIdentifications?: string[];
  actionTypes?: string[];
  /**
   * Topic 模式列表(对齐 ACL DeviceAclRule.topicPattern 设计)。
   * <p>每条是 MQTT 主题模板字符串,可含 {@code +/#} 通配符 + {@code ${app_id}/${device_identification}/...} 占位符;
   * 空列表 / undefined 表示不约束 topic;非空时只要 envelope.topic 匹配任一模式即视为命中。
   */
  topicPatterns?: string[];
  /**
   * 数据形态过滤(对应字典 BRIDGE_PAYLOAD_KIND)。
   * <p>取值 RAW(原始报文) / THING_MODEL(物模型数据);控制规则消费哪种数据形态。
   * 空 / undefined 表示后端默认只消费 RAW;要桥接物模型数据须显式含 THING_MODEL。
   */
  payloadKinds?: string[];
  deviceFilter?: DeviceFilterDto;
  payloadFilter?: PayloadFilterDto;
  timeWindow?: TimeWindowDto;
}

// ============================== 入站 match_config_json ==============================

export interface InboundMatchConfigDto {
  subscriptionSourceIds?: number[];
  messageFilter?: PayloadFilterDto;
}

// ============================== 出站 action_config_json ==============================

/**
 * 出站动作配置（按 sourceType 子段异构；最终落 action_config_json）。
 *
 * <p>共有字段（payloadFormat / payloadTemplate）所有 sourceType 通用；
 * 协议特异字段（kafka / redis / http / ...）按所选 DataSource.sourceType 渲染对应子表单。
 */
export interface OutboundActionConfigDto {
  payloadFormat?: 'JSON' | 'STRING' | 'HEX' | 'RAW';
  payloadTemplate?: string;

  // sourceType 特异（前端根据所选 DataSource 渲染其中之一）
  kafka?: KafkaActionConfigDto;
  redis?: RedisActionConfigDto;
  rocketmq?: RocketmqActionConfigDto;
  rabbitmq?: RabbitmqActionConfigDto;
  mysql?: MysqlActionConfigDto;
  http?: HttpActionConfigDto;
  webhook?: WebhookActionConfigDto;
  mqtt?: MqttActionConfigDto;
  tdengine?: TDengineActionConfigDto;
  clickhouse?: ClickHouseActionConfigDto;
  influxdb?: InfluxDbActionConfigDto;
  iotdb?: IoTDbActionConfigDto;
  postgresql?: PostgreSqlActionConfigDto;
  mongodb?: MongoDbActionConfigDto;
  pulsar?: PulsarActionConfigDto;
  dm?: DmActionConfigDto;
  kingbase?: KingBaseActionConfigDto;
}

export interface KafkaActionConfigDto {
  /** 分区键模板 */
  partitionKey?: string;
  /** 自定义 headers JSON 字符串（key-value） */
  headers?: string;
}

export interface RedisActionConfigDto {
  /** 写入命令（覆盖数据源默认） */
  command?: 'LPUSH' | 'RPUSH' | 'XADD' | 'PUBLISH' | 'SET';
  /** Key 模板（覆盖数据源默认） */
  keyTemplate?: string;
  /** TTL 秒（SET / XADD 时） */
  ttlSeconds?: number;
}

export interface RocketmqActionConfigDto {
  /** Tag 模板（覆盖数据源默认 tag） */
  tag?: string;
  /** 顺序消息 hashKey 模板 */
  hashKey?: string;
}

export interface RabbitmqActionConfigDto {
  /** Routing key 模板（覆盖数据源默认） */
  routingKey?: string;
  /** 自定义 properties JSON */
  properties?: string;
}

export interface MysqlActionConfigDto {
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
  /** 冲突策略 */
  onDuplicate?: 'INSERT' | 'UPDATE' | 'IGNORE';
}

export interface HttpActionConfigDto {
  /** 自定义 headers JSON */
  headers?: string;
  /** 自定义 query params JSON */
  queryParams?: string;
  /** body 是否包一层 {data: payload} */
  bodyWrap?: boolean;
}

export interface WebhookActionConfigDto {
  /** 自定义 headers JSON */
  headers?: string;
}

export interface MqttActionConfigDto {
  /** Topic 模板（覆盖数据源默认） */
  topicTemplate?: string;
  /** QoS（覆盖数据源默认） */
  qos?: 0 | 1 | 2;
  /** 保留消息标志 */
  retained?: boolean;
}

// ============================== 新增 7 协议 action 配置（v2 扩展） ==============================

export interface TDengineActionConfigDto {
  /** 超表名覆盖 */
  superTableOverride?: string;
  /** 子表命名模板覆盖 */
  childTableTemplate?: string;
  /** TAGS 映射 JSON（覆盖数据源默认） */
  tagsMapping?: string;
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
}

export interface ClickHouseActionConfigDto {
  /** 目标表覆盖 */
  tableOverride?: string;
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
}

export interface InfluxDbActionConfigDto {
  /** Measurement 覆盖 */
  measurementOverride?: string;
  /** Tags 映射 JSON（覆盖数据源默认） */
  tagsMapping?: string;
  /** Fields 映射 JSON（覆盖数据源默认） */
  fieldsMapping?: string;
}

export interface IoTDbActionConfigDto {
  /** 时序路径模板覆盖 */
  timeseriesTemplateOverride?: string;
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
}

export interface PostgreSqlActionConfigDto {
  /** 目标表覆盖（含 schema 前缀如 public.iot_data） */
  tableOverride?: string;
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
  /** 冲突策略覆盖 */
  onConflictOverride?: 'INSERT' | 'UPSERT' | 'IGNORE';
}

export interface MongoDbActionConfigDto {
  /** Collection 覆盖 */
  collectionOverride?: string;
  /** 写入模式覆盖 */
  writeModeOverride?: 'INSERT' | 'UPSERT';
}

export interface PulsarActionConfigDto {
  /** Topic 覆盖（覆盖数据源默认） */
  topicOverride?: string;
  /** 消息 key 模板（影响 Pulsar 路由 / Key_Shared 订阅） */
  messageKey?: string;
  /** 自定义 message properties JSON */
  messageProperties?: string;
  /** 发送模式覆盖 */
  sendModeOverride?: 'SYNC' | 'ASYNC';
}

export interface DmActionConfigDto {
  /** 目标表覆盖（含 schema 前缀如 IOT.iot_data） */
  tableOverride?: string;
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
  /** 冲突策略覆盖 */
  onDuplicateOverride?: 'INSERT' | 'UPDATE' | 'IGNORE';
}

export interface KingBaseActionConfigDto {
  /** 目标表覆盖 */
  tableOverride?: string;
  /** 字段映射 JSON（覆盖数据源默认） */
  columnMapping?: string;
  /** 冲突策略覆盖 */
  onConflictOverride?: 'INSERT' | 'UPSERT' | 'IGNORE';
}

// ============================== 入站 action_config_json ==============================

export type InboundTargetHandler = 'MQTT_FORWARD' | 'RAW_INSERT' | 'RULE_TRIGGER';

export interface InboundActionConfigDto {
  /** 入站消息处理方式 */
  targetHandler?: InboundTargetHandler;
  /** MQTT_FORWARD：目标产品标识 */
  targetProductIdentification?: string;
  /** MQTT_FORWARD：目标 topic 模板 */
  targetTopicTemplate?: string;
  /** 字段映射列表（JSON 字符串展示给用户编辑） */
  fieldMapping?: string;
  /** RULE_TRIGGER：要触发的规则 ID */
  triggerRuleId?: number | null;
}

// ============================== ExamplePreset ==============================

/**
 * 桥接规则示例预设结构。
 * <p>不同方向用不同 preset 集（OutboundExample vs InboundExample）；前端按 direction 切换。
 */
export interface OutboundExamplePreset {
  key: string;
  label: string;
  description?: string;
  match: OutboundMatchConfigDto;
  action: OutboundActionConfigDto;
  /** 要求所选数据源的 sourceType（用于过滤显示）；不限时为 undefined */
  requireSourceType?: string;
}

export interface InboundExamplePreset {
  key: string;
  label: string;
  description?: string;
  match: InboundMatchConfigDto;
  action: InboundActionConfigDto;
}

// ============================== 联合 ==============================

export type AnyMatchConfig = OutboundMatchConfigDto | InboundMatchConfigDto;
export type AnyActionConfig = OutboundActionConfigDto | InboundActionConfigDto;
