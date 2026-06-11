/**
 * 北向集成 / 数据桥接 SVG 图标组件聚合导出。
 *
 * 与后端 ConnectorType 枚举 1:1 对齐：
 *   KAFKA / REDIS / ROCKETMQ / RABBITMQ / MYSQL / HTTP / WEBHOOK / MQTT
 *   TDENGINE / CLICKHOUSE / INFLUXDB / IOTDB / POSTGRESQL / MONGODB / PULSAR
 *   DM / KINGBASE
 *
 * 用法：
 *   import { KafkaSvg, getSourceTypeSvg } from '/@/components/iot/integration/svg';
 *   <component :is="getSourceTypeSvg(record.sourceType)" />
 */
import type { Component } from 'vue';

import KafkaSvg from './KafkaSvg.vue';
import RedisSvg from './RedisSvg.vue';
import RocketmqSvg from './RocketmqSvg.vue';
import RabbitmqSvg from './RabbitmqSvg.vue';
import MysqlSvg from './MysqlSvg.vue';
import HttpSvg from './HttpSvg.vue';
import WebhookSvg from './WebhookSvg.vue';
import MqttSvg from './MqttSvg.vue';
import TDengineSvg from './TDengineSvg.vue';
import ClickHouseSvg from './ClickHouseSvg.vue';
import InfluxDbSvg from './InfluxDbSvg.vue';
import IoTDbSvg from './IoTDbSvg.vue';
import PostgreSqlSvg from './PostgreSqlSvg.vue';
import MongoDbSvg from './MongoDbSvg.vue';
import PulsarSvg from './PulsarSvg.vue';
import DmSvg from './DmSvg.vue';
import KingBaseSvg from './KingBaseSvg.vue';
import DataBridgeSvg from './DataBridgeSvg.vue';
import CaCertLicenseSvg from './CaCertLicenseSvg.vue';

export {
  CaCertLicenseSvg,
  KafkaSvg,
  RedisSvg,
  RocketmqSvg,
  RabbitmqSvg,
  MysqlSvg,
  HttpSvg,
  WebhookSvg,
  MqttSvg,
  TDengineSvg,
  ClickHouseSvg,
  InfluxDbSvg,
  IoTDbSvg,
  PostgreSqlSvg,
  MongoDbSvg,
  PulsarSvg,
  DmSvg,
  KingBaseSvg,
  DataBridgeSvg,
};

/** sourceType → SVG 组件 映射表 */
const SOURCE_TYPE_SVG_MAP: Record<string, Component> = {
  KAFKA: KafkaSvg,
  REDIS: RedisSvg,
  ROCKETMQ: RocketmqSvg,
  RABBITMQ: RabbitmqSvg,
  MYSQL: MysqlSvg,
  HTTP: HttpSvg,
  WEBHOOK: WebhookSvg,
  MQTT: MqttSvg,
  TDENGINE: TDengineSvg,
  CLICKHOUSE: ClickHouseSvg,
  INFLUXDB: InfluxDbSvg,
  IOTDB: IoTDbSvg,
  POSTGRESQL: PostgreSqlSvg,
  MONGODB: MongoDbSvg,
  PULSAR: PulsarSvg,
  DM: DmSvg,
  KINGBASE: KingBaseSvg,
};

/**
 * 按 sourceType 字符串取对应的 SVG 组件。
 *
 * @param sourceType 数据源协议类型，与 ConnectorType 枚举 1:1（KAFKA/REDIS/ROCKETMQ/...）
 * @returns 对应的 Vue SVG 组件；未匹配时返回通用 DataBridgeSvg 兜底
 */
export function getSourceTypeSvg(sourceType?: string): Component {
  if (!sourceType) return DataBridgeSvg;
  return SOURCE_TYPE_SVG_MAP[sourceType.toUpperCase()] ?? DataBridgeSvg;
}
