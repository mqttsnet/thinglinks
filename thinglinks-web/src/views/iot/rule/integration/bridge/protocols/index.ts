/**
 * 桥接规则 - 出站动作协议模块 Registry。
 *
 * <p>每种 sourceType 对应一个 BridgeOutboundActionModule 实例，由本文件 registry 路由：
 * <ul>
 *   <li>{@link getActionFields} ── 按 sourceType 取出站 action 表单 schema</li>
 *   <li>{@link assembleActionByType} ── 提交时 flat → nested 子对象</li>
 *   <li>{@link flattenActionByType} ── 编辑时 nested → flat</li>
 *   <li>{@link presetToFlatByType} ── 加载示例时 preset 子对象 → flat</li>
 *   <li>{@link allActionFieldNamesAcrossProtocols} ── 全协议字段集合（Edit.vue 提交切片用）</li>
 * </ul>
 *
 * <h3>新增协议步骤</h3>
 * <ol>
 *   <li>建 {@code XxxBridgeAction.ts}</li>
 *   <li>本文件加 1 行 import + registry 注册</li>
 *   <li>0 改其它协议文件</li>
 * </ol>
 *
 * @author mqttsnet
 */

import type { FormSchema } from '/@/components/Form';
import type { BridgeOutboundActionModule } from './types';

import { kafkaBridgeAction } from './KafkaBridgeAction';
import { redisBridgeAction } from './RedisBridgeAction';
import { rocketmqBridgeAction } from './RocketmqBridgeAction';
import { rabbitmqBridgeAction } from './RabbitmqBridgeAction';
import { mysqlBridgeAction } from './MysqlBridgeAction';
import { httpBridgeAction } from './HttpBridgeAction';
import { webhookBridgeAction } from './WebhookBridgeAction';
import { mqttBridgeAction } from './MqttBridgeAction';
import { tdengineBridgeAction } from './TDengineBridgeAction';
import { clickHouseBridgeAction } from './ClickHouseBridgeAction';
import { influxDbBridgeAction } from './InfluxDbBridgeAction';
import { ioTDbBridgeAction } from './IoTDbBridgeAction';
import { postgreSqlBridgeAction } from './PostgreSqlBridgeAction';
import { mongoDbBridgeAction } from './MongoDbBridgeAction';
import { pulsarBridgeAction } from './PulsarBridgeAction';
import { dmBridgeAction } from './DmBridgeAction';
import { kingBaseBridgeAction } from './KingBaseBridgeAction';

const registry: Record<string, BridgeOutboundActionModule> = {
  KAFKA: kafkaBridgeAction,
  REDIS: redisBridgeAction,
  ROCKETMQ: rocketmqBridgeAction,
  RABBITMQ: rabbitmqBridgeAction,
  MYSQL: mysqlBridgeAction,
  HTTP: httpBridgeAction,
  WEBHOOK: webhookBridgeAction,
  MQTT: mqttBridgeAction,
  TDENGINE: tdengineBridgeAction,
  CLICKHOUSE: clickHouseBridgeAction,
  INFLUXDB: influxDbBridgeAction,
  IOTDB: ioTDbBridgeAction,
  POSTGRESQL: postgreSqlBridgeAction,
  MONGODB: mongoDbBridgeAction,
  PULSAR: pulsarBridgeAction,
  DM: dmBridgeAction,
  KINGBASE: kingBaseBridgeAction,
};

/** 取协议模块；未注册返回 undefined */
export function getBridgeAction(sourceType?: string): BridgeOutboundActionModule | undefined {
  return sourceType ? registry[sourceType] : undefined;
}

/** 按 sourceType 取出站 action 段表单 schema */
export function getActionFields(sourceType?: string): FormSchema[] {
  return getBridgeAction(sourceType)?.actionFields() ?? [];
}

/** 提交时 flat → 协议子对象 */
export function assembleActionByType(
  sourceType: string | undefined,
  values: Record<string, any>,
): unknown {
  return getBridgeAction(sourceType)?.assembleAction(values);
}

/** 编辑时 协议子对象 → flat */
export function flattenActionByType(
  sourceType: string | undefined,
  dto: unknown,
): Record<string, any> {
  return getBridgeAction(sourceType)?.flattenAction(dto as any) ?? {};
}

/** "加载示例"时 preset 子对象 → flat */
export function presetToFlatByType(
  sourceType: string | undefined,
  preset: unknown,
): Record<string, any> {
  return getBridgeAction(sourceType)?.presetToFlat(preset as any) ?? {};
}

/** 全协议出站 ac_*_* 字段名集合（Edit.vue 提交时切片用） */
export function allActionFieldNamesAcrossProtocols(): string[] {
  const names: string[] = [];
  for (const proto of Object.values(registry)) {
    proto.actionFields().forEach((s) => {
      if (s.field) names.push(s.field);
    });
  }
  return names;
}

/** 已注册的全部 sourceType（调试 / 字典-注册一致性校验用） */
export function listBridgeActionTypes(): string[] {
  return Object.keys(registry);
}

export type { BridgeOutboundActionModule } from './types';
