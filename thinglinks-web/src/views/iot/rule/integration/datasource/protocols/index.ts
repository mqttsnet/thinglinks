/**
 * 协议模块 Registry（前端 Strategy + Registry 模式）。
 *
 * <p>每种数据源类型对应一个独立 .ts 文件实现 {@link ProtocolModule} 接口，
 * 在本文件 {@link registry} 注册一行即可生效。
 *
 * <h3>修改 / 新增协议的标准动作</h3>
 * <ol>
 *   <li>修改：直接编辑对应的 XxxProtocol.ts 文件，<b>0 改其它协议</b></li>
 *   <li>新增：建一个新 XxxProtocol.ts 实现 ProtocolModule 接口 → 在本文件 registry 加 1 行 import + 注册</li>
 * </ol>
 *
 * <h3>调用方</h3>
 * <ul>
 *   <li>{@code Edit.vue.composeSchema()} → 调 {@link getConnectionFields}/{@link getCredentialFields}</li>
 *   <li>{@code Edit.vue.onPresetClick()} → 调 {@link getProtocolPresets}</li>
 *   <li>{@code Edit.vue.onFieldChange('sourceType')} → 调 {@link getRecommendedDefaults}</li>
 * </ul>
 *
 * @author mqttsnet
 */

import type { FormSchema } from '/@/components/Form';
import type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';
import { defaultParseConnection, defaultParseCredential } from './base';

// ============================== 已有 8 协议 ==============================
import { kafkaProtocol } from './KafkaProtocol';
import { redisProtocol } from './RedisProtocol';
import { rocketmqProtocol } from './RocketmqProtocol';
import { rabbitmqProtocol } from './RabbitmqProtocol';
import { mysqlProtocol } from './MysqlProtocol';
import { httpProtocol } from './HttpProtocol';
import { webhookProtocol } from './WebhookProtocol';
import { mqttProtocol } from './MqttProtocol';

// ============================== 新增 7 协议（time-series / OLAP / NoSQL / 流平台扩展）==============================
import { tdengineProtocol } from './TDengineProtocol';
import { clickHouseProtocol } from './ClickHouseProtocol';
import { influxDbProtocol } from './InfluxDbProtocol';
import { ioTDbProtocol } from './IoTDbProtocol';
import { postgreSqlProtocol } from './PostgreSqlProtocol';
import { mongoDbProtocol } from './MongoDbProtocol';
import { pulsarProtocol } from './PulsarProtocol';
import { dmProtocol } from './DmProtocol';
import { kingBaseProtocol } from './KingBaseProtocol';

/** 全部协议模块的索引表（type → module） */
const registry: Record<string, ProtocolModule> = {
  KAFKA: kafkaProtocol,
  REDIS: redisProtocol,
  ROCKETMQ: rocketmqProtocol,
  RABBITMQ: rabbitmqProtocol,
  MYSQL: mysqlProtocol,
  HTTP: httpProtocol,
  WEBHOOK: webhookProtocol,
  MQTT: mqttProtocol,
  TDENGINE: tdengineProtocol,
  CLICKHOUSE: clickHouseProtocol,
  INFLUXDB: influxDbProtocol,
  IOTDB: ioTDbProtocol,
  POSTGRESQL: postgreSqlProtocol,
  MONGODB: mongoDbProtocol,
  PULSAR: pulsarProtocol,
  DM: dmProtocol,
  KINGBASE: kingBaseProtocol,
};

/** 取协议模块；未注册返回 undefined */
export function getProtocol(sourceType?: string): ProtocolModule | undefined {
  return sourceType ? registry[sourceType] : undefined;
}

/** 取连接表单 schema；未选 sourceType 时返回空数组 */
export function getConnectionFields(sourceType?: string): FormSchema[] {
  return getProtocol(sourceType)?.connectionFields() ?? [];
}

/** 取凭证表单 schema；未选 sourceType 时返回空数组 */
export function getCredentialFields(sourceType?: string): FormSchema[] {
  return getProtocol(sourceType)?.credentialFields() ?? [];
}

/** 取协议示例预设列表（"加载示例"下拉用） */
export function getProtocolPresets(sourceType?: string): ProtocolPreset[] {
  return getProtocol(sourceType)?.examplePresets?.() ?? [];
}

/** 取协议级智能预填策略（ADD 模式选定 sourceType 后自动填默认策略字段用） */
export function getRecommendedDefaults(sourceType?: string): ProtocolDefaultPolicy {
  return getProtocol(sourceType)?.recommendedDefaults?.() ?? {};
}

/** 反序列化 connection_json（按协议路由；fallback 通用 parser） */
export function parseConnection(
  sourceType: string | undefined,
  json: string | null | undefined,
): Record<string, any> {
  const proto = getProtocol(sourceType);
  if (proto?.parseConnection) {
    return proto.parseConnection(json);
  }
  return defaultParseConnection(json);
}

/** 反序列化 credential_json（按协议路由） */
export function parseCredential(
  sourceType: string | undefined,
  json: string | null | undefined,
): Record<string, any> {
  const proto = getProtocol(sourceType);
  if (proto?.parseCredential) {
    return proto.parseCredential(json);
  }
  return defaultParseCredential(json);
}

/** 协议级提交前校验（返回错误信息则阻止提交） */
export function validateProtocol(
  sourceType: string | undefined,
  values: Record<string, any>,
): string | null {
  return getProtocol(sourceType)?.validate?.(values) ?? null;
}

/** 已注册的全部协议 type 列表（调试用 / 字典 - 注册一致性校验用） */
export function listProtocolTypes(): string[] {
  return Object.keys(registry);
}

// 重新导出公共工具，调用方直接 from './protocols' 即可拿到
export { assembleJson } from './base';
export type { ProtocolModule, ProtocolPreset, ProtocolDefaultPolicy } from './types';
