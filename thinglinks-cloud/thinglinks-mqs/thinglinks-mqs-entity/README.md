# 设备事件字段契约（协议接入 → Kafka → mqs）

本模块定义所有「设备协议事件」的实体（`entity.protocol.base.*`）。事件由**协议接入层**产出、经 **Kafka** 投递、由 **mqs bus 流水线
**消费。本文档统一各字段定义,并**区分协议场景与事件类型场景**,供接入端(broker / BifroMQ 插件)与消费端(mqs)对齐。

> 改字段前先读本文 + `DeviceActionTypeEnum`(事件类型↔topic↔连接状态影响的权威表)。

---

## 1. 事件流与生产者(协议场景)

```
                          ┌──────────────────────────── 同一套字段契约 ───────────────────────────┐
MQTT  设备 ── BifroMQ ── BifroMQ event-collector 插件 ──┐
WS    设备 ── broker  ── WebSocketDeviceOpenAccessProtocolEndpoint ──┼── Kafka(每事件类型独立 topic)── mqs bus 流水线
TCP   设备 ── broker  ── (TCP 接入端点) ───────────────┘                 ProtocolKafkaPayloadParser → … → DeviceEventHook/Processor
```

| 协议       | 生产者                         | `eventHlc`/`eventUtc` 来源                                                                           |
|----------|-----------------------------|----------------------------------------------------------------------------------------------------|
| **MQTT** | BifroMQ event-collector 插件  | 插件 report 入口同步抓 `Event.hlc()` / `Event.utc()`(BifroMQ 内核时钟)                                        |
| **WS**   | broker `@ServerEndpoint` 端点 | `BaseEvent` 的 `@Builder.Default`:`HybridLogicalClockUtil.nextHlc()` / `System.currentTimeMillis()` |
| **TCP**  | broker TCP 端点               | 同 WS(走 `BaseEvent` 默认盖戳)                                                                           |

**关键**:接入端只管「产出事件 + 盖时钟」,mqs `ProtocolKafkaPayloadParser` **协议中立直读** `eventHlc`/`eventUtc`,不按协议特判。

### 1.1 mqs 协议层统一事件入口(bus inbound,三协议对称)

mqs 不持有连接,只在 bus 传输层消费 Kafka。**每协议 1 个 inbound 消费者 + 4 个 `@TopicRoute` edge adapter**,把事件归并成 4
组投同一条流水线 —— 三协议结构**完全对称**(改一个协议参数不影响另一个):

| 事件组      | edge adapter(每协议各一)                   | 收的 topic(WS 示例,MQTT/TCP 同构换前缀 `mqtt.`/`tcp.`)                                                           | 归并事件                                         |
|----------|---------------------------------------|---------------------------------------------------------------------------------------------------------|----------------------------------------------|
| **生命周期** | `{Mqtt\|Ws\|Tcp}LifecycleEdgeAdapter` | `*.client.connected` · `*.client.disconnect` · `*.server.disconnect` · `*.device.kicked` · `*.ping.req` | CONNECT / DISCONNECT / CLOSE / KICKED / PING |
| **数据上行** | `{…}DeviceDataEdgeAdapter`            | `*.distribution.completed`                                                                              | PUBLISH                                      |
| **分发失败** | `{…}DistributionEdgeAdapter`          | `*.distribution.error`                                                                                  | DISPATCH_ERROR                               |
| **订阅确认** | `{…}ControlAckEdgeAdapter`            | `*.subscription.acked` · `*.unsubscription.acked`                                                       | SUBSCRIBE / UNSUBSCRIBE(**WS 无**)            |

链路:`{Mqtt\|Ws\|Tcp}KafkaInboundConsumer`(恢复 traceId/tenantId)→ `BusPipelineDispatcher` → 命中 `@TopicRoute` 的
adapter `canonicalize` → `DeviceProtocolEvent` → PRE 富化(`DeviceCacheEnricher`)→ CORE 组装派发 → 处理器 / 钩子(
连接状态 / 心跳 / 落库)。

> 接入端只要往**对应前缀 + 后缀**的 topic 投符合下文结构的 JSON,mqs 即零改动消费;新增协议照搬这 4 组 adapter 即可。

---

## 2. 通用字段(所有事件,`BaseEvent`)

| 字段         | 类型     | 含义                                                 | 备注                                           |
|------------|--------|----------------------------------------------------|----------------------------------------------|
| `tenantId` | String | 租户 ID                                              | 连接时带入;Kafka header 也透传一份                     |
| `clientId` | String | 设备客户端标识(含 `@租户` 后缀)                                | 分区 key / 设备查找                                |
| `event`    | String | 事件类型(`DeviceActionTypeEnum`,如 `CONNECT`/`PUBLISH`) | 插件侧 JSON 字段名 `eventType`;mqs 解析时缺省按 topic 兜底 |
| `success`  | String | 结果 `success` / `failure`                           |                                              |
| `traceId`  | String | 全链路追踪 ID                                           | 接入端生成,贯穿 mqs→rule→sink                       |

### 2.1 三个时间字段（语义分离,务必区分）

| 字段                        | 类型              | 语义                                                   | 用途                                                                                                       | 禁忌                                     |
|---------------------------|-----------------|------------------------------------------------------|----------------------------------------------------------------------------------------------------------|----------------------------------------|
| `timestamp` / `eventTime` | Long(epoch ms)  | **处理/构建时刻**(接入端 build 事件 / 插件 worker 取值的 wall clock) | debug / 调度时延观测                                                                                           | 非事件真实发生时间                              |
| `eventUtc`                | Long(epoch ms)  | **事件真实发生瞬间**(物理 UTC 锚点)                              | 业务展示、时序入库、桥接下游数据源时间字段一律用它                                                                                | —                                      |
| `eventHlc`                | Long(**远超 ms**) | **因果时钟 HLC**,严格单调递增                                  | 下游 `DeviceConnectStatusSyncHook` / `DevicePingProcessor` 做 **event-time LWW CAS 单调写键**,防异步/乱序/抖动重连导致状态回退 | **禁止当 epoch ms 用**,误写 datetime/时序索引会撑爆 |

---

## 3. HLC 编码（一致性保证）

两侧**编码完全一致**,保证同一设备的事件可单调比较:

```
HLC = (物理毫秒 << 16) | 16 位逻辑计数器
```

| 来源       | 实现                                                                 | 编码                                 |
|----------|--------------------------------------------------------------------|------------------------------------|
| MQTT     | `com.baidu.bifromq.basehlc.HLC`                                    | `物理ms << 16 \| 16位logical`         |
| WS / TCP | `com.mqttsnet.basic.utils.HybridLogicalClockUtil`（util 工程,与雪花算法同处) | `物理ms << 16 \| 16位counter`（**同款**) |

- 数值约为 epoch ms 的 6.5 万倍,**远超 ms** —— 与 `eventUtc`(真实 ms)严格区分。
- 同一毫秒内多事件靠低 16 位计数器保证严格递增。
- 一台设备只走一种协议 → 单一 HLC 源 → 每设备单调,CAS 成立。

---

## 4. 按事件类型（业务场景）

业务事件类型见 `DeviceActionTypeEnum`;每类有**对称的 mqtt.* / websocket.* topic**:

| 业务类型                      | MQTT topic                                                          | WS topic                                 | 影响连接状态     | 兜底状态    | 实体                      |
|---------------------------|---------------------------------------------------------------------|------------------------------------------|------------|---------|-------------------------|
| `CONNECT` 连接成功            | `mqtt.client.connected.topic`                                       | `websocket.client.connected.topic`       | ✅          | ONLINE  | `ClientConnectedEvent`  |
| `DISCONNECT` 主动断开         | `mqtt.client.disconnect.topic`                                      | `websocket.client.disconnect.topic`      | ✅          | OFFLINE | `ClientDisconnectEvent` |
| `CLOSE` 服务端关闭             | `mqtt.server.disconnect.topic`                                      | `websocket.server.disconnect.topic`      | ✅          | OFFLINE | `ClientDisconnectEvent` |
| `KICKED` 被踢线              | `mqtt.device.kicked.topic`                                          | `websocket.device.kicked.topic`          | ✅          | OFFLINE | —                       |
| `PING` 心跳                 | `mqtt.ping.req.topic`                                               | `websocket.ping.req.topic`               | ❌（续命,不改状态） | —       | `PingReqEvent`          |
| `PUBLISH` 数据上行            | `mqtt.distribution.completed.topic`                                 | `websocket.distribution.completed.topic` | ❌          | —       | `DistedEvent`           |
| `DISPATCH_ERROR` 分发失败     | `mqtt.distribution.error.topic`                                     | `websocket.distribution.error.topic`     | ❌          | —       | `DistErrorEvent`        |
| `HEART_TIMEOUT` 心跳超时      | —（MQTT keepalive 超时归 CLOSE）                                         | mqs 应用层判定                                | ✅          | OFFLINE | —                       |
| `ERROR` 协议异常              | —                                                                   | mqs `@OnError`                           | ✅          | OFFLINE | `DistErrorEvent`        |
| `SUBSCRIBE`/`UNSUBSCRIBE` | `mqtt.subscription.acked.topic` / `mqtt.unsubscription.acked.topic` | **WS 无**（订阅模型不适用）                        | ❌          | —       | —                       |

> **连接状态 / 心跳的写入,统一走 `eventHlc` HLC CAS 单调写**(`last_status_event_hlc < eventHlc`
> 才覆盖,防迟到/乱序把已离线翻回在线):
> - **生命周期事件**(`affectsConnectionStatus()=true`:CONNECT/DISCONNECT/CLOSE/KICKED/HEART_TIMEOUT/ERROR)→
    `DeviceConnectStatusSyncHook` → 据 `eventHlc` CAS 写 `device.connect_status`。
> - **PING** → `DevicePingProcessor` → `reportDeviceHeartbeat`:`last_heartbeat_time` 每条 PING 无条件续;
    `connect_status=ONLINE` 走 `eventHlc` CAS(60s 节流避免每条都写)。`eventHlc` 缺失则只续心跳不动状态。
> - ⭐ 全链路**无任何「直写状态」**:迟到 PING(hlc < 已写 DISCONNECT 的 hlc)被 CAS 拒绝,不会错把已离线设备翻回在线。

---

## 5. 各事件实体特有字段

| 实体                      | 特有字段                                                                                                                       | 说明                                                                                        |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| `ClientConnectedEvent`  | `version` / `userId` / `address` / `channelId` / `keepAliveTimeSeconds`                                                    | 连接握手信息;`keepAliveTimeSeconds` 仅 MQTT 有                                                    |
| `ClientDisconnectEvent` | `version` / `address` / `channelId`                                                                                        | DISCONNECT 与 CLOSE 共用                                                                     |
| `DistedEvent`(PUBLISH)  | `topic` / `messageId` / `qos` / `expiryInterval` / `payload`(Base64) / `payloadHex` / `encoding` / `originalSize` | 设备上行数据;**WS 统一协议**:报文体 `{"topic","payload"}`,`payload` 为统一消息体 JSON 字符串 → `DistedEvent.payload`=Base64(原始报文) |
| `PingReqEvent`(PING)    | `version` / `userId` / `address` / `channelId`                                                                             | 心跳续命                                                                                      |
| `DistErrorEvent`        | `reqId` / `code` / `errorMessage`                                                                                          | 主要场景:下行投设备失败(离线 / inbox 满 / QoS 路径异常)                                                     |

### 5.1 各事件 Kafka 报文结构(信封 + 特异字段)

每条 Kafka 消息 = **公共信封**(第 2 节:`tenantId` `clientId` `event` `success` `eventHlc` `eventUtc` `timestamp`;
`traceId` 走 Kafka header)+ 下列特异字段。MQTT 由 BifroMQ 插件产出、WS/TCP 由 broker 产出,**字段名一致**;topic 换协议前缀即可。

**连接 CONNECT** · `*.client.connected.topic` · `ClientConnectedEvent`

```json
{
    "tenantId": "1",
    "clientId": "3752151419551744@1",
    "event": "CONNECT",
    "success": "success",
    "eventHlc": 114413000000000,
    "eventUtc": 1746521094000,
    "timestamp": 1746521094123,
    "version": "v1",
    "userId": "dev001",
    "address": "10.0.0.5:51632",
    "channelId": "id-xxx",
    "keepAliveTimeSeconds": 60
}
```

**断开 DISCONNECT** · `*.client.disconnect.topic` ／ **服务端断开 CLOSE** · `*.server.disconnect.topic` ·
`ClientDisconnectEvent`

```json
{
    "tenantId": "1",
    "clientId": "3752151419551744@1",
    "event": "DISCONNECT",
    "success": "success",
    "eventHlc": 114413000000200,
    "eventUtc": 1746521098000,
    "version": "v1",
    "address": "10.0.0.5:51632",
    "channelId": "id-xxx"
}
```

**心跳 PING** · `*.ping.req.topic` · `PingReqEvent`

```json
{
    "tenantId": "1",
    "clientId": "3752151419551744@1",
    "event": "PING",
    "success": "success",
    "eventHlc": 114413000000300,
    "eventUtc": 1746521124000,
    "version": "v1",
    "userId": "dev001",
    "address": "10.0.0.5:51632",
    "channelId": "id-xxx"
}
```

> WS PING 报文是 `{"type":"PING"}`,不带 `heartbeatTime` → mqs 取 `eventUtc` 作心跳时间;MQTT 由插件带 `heartbeatTime`
> 字段。心跳与连接状态的 CAS 写入详见第 4 节末说明。

**数据上行 PUBLISH** · `*.distribution.completed.topic` · `DistedEvent`

```json
{
    "tenantId": "1",
    "clientId": "3752151419551744@1",
    "event": "PUBLISH",
    "success": "success",
    "eventHlc": 114413000000400,
    "eventUtc": 1746521130000,
    "topic": "/v1/devices/3752151419551744/datas",
    "messageId": 123,
    "qos": 0,
    "encoding": "UTF-8",
    "originalSize": 98,
    "payload": "<Base64(原始报文)>",
    "payloadHex": "7b22..."
}
```

> WS 统一协议:设备发 `{"topic":"...","payload":"..."}` → broker 取 `topic`→`DistedEvent.topic`、`payload`→`DistedEvent.payload`=Base64(原始报文)。

**分发失败 DISPATCH_ERROR** · `*.distribution.error.topic` · `DistErrorEvent`

```json
{
    "tenantId": "1",
    "clientId": "3752151419551744@1",
    "event": "DISPATCH_ERROR",
    "success": "success",
    "eventHlc": 114413000000500,
    "eventUtc": 1746521131000,
    "reqId": "req-xxx",
    "code": 1,
    "errorMessage": "device offline"
}
```

---

## 6. 协议场景差异速查

| 维度                    | MQTT(BifroMQ)                          | WS(broker)                                                                             |
|-----------------------|----------------------------------------|----------------------------------------------------------------------------------------|
| 时钟产出                  | 内核 `Event.hlc()/utc()`,插件 report 入口同步抓 | `BaseEvent @Builder.Default`(`HybridLogicalClockUtil.nextHlc()` / `currentTimeMillis`) |
| keepalive             | 协议级,超时归 `CLOSE`                        | 无;应用层心跳超时归 `HEART_TIMEOUT`                                                             |
| SUBSCRIBE/UNSUBSCRIBE | 有(SUB_ACKED / UNSUB_ACKED)             | 无                                                                                      |
| 上行报文                  | DISTED payload 带完整 PUBLISH 报文          | `{"topic","payload"}` 统一协议(topic 决定路由,payload 为数据载荷)                                   |
| 被动断细分                 | 18 种(BY_SERVER + 17）统一归 `CLOSE`        | `@OnError` 归 `ERROR` / `@OnClose` 归 `DISCONNECT`                                       |

---

> **新增协议接入端**:产出 `BaseEvent` 子类即可,`eventHlc/eventUtc/timestamp` 由 `@Builder.Default` 自动盖戳,无需手动设;mqs
> 侧零改动(协议中立直读)。
