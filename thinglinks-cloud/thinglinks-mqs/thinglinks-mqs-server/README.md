# thinglinks-mqs ── Kafka 事件消费契约

> **本 README 是面向第三方 Broker / 第三方业务系统接入 thinglinks 平台的"消息契约"标准**。
> MQTT 是规范基线;WebSocket / TCP / CoAP / 其他协议的接入端**必须**按本 README 的字段结构往
> 对应 Kafka topic 推送事件,mqs 即可零改动消费、走完原有的 DeviceAction 持久化 / 时序入库 /
> 物模型解析 / 北向桥接 等所有下游链路。

---

## 1. 数据流总览

```
                 ┌────────────────────────────────────────────────┐
   设备 ─MQTT─→  │ BifroMQ Broker                                  │
                 │   ↓ (bifromq-event-collector-plugin)            │
                 │   按事件类型 routing 投 Kafka 各 topic          │
                 └────────────────────────────────────────────────┘
                                       │
   设备 ─WS───→  ┌────────────────────────────────────────────────┐
                 │ WS / TCP / 第三方 Broker                         │
                 │   ↓ 按本 README 契约构造同款 JSON               │
                 │   投 Kafka 同前缀的 *.* topic                    │
                 └────────────────────────────────────────────────┘
                                       │
                                       ↓
                 ┌────────────────────────────────────────────────┐
                 │ thinglinks-mqs (本服务)                          │
                 │   ─ MqttMessageKafkaConsumerHandler             │
                 │   ─ WebSocketMessageKafkaConsumerHandler        │
                 │   ─ TcpMessageKafkaConsumerHandler              │
                 │   ↓                                              │
                 │   ProtocolHandlerFactory → 解析 + 持久化         │
                 │   DeviceAction / TDS / 物模型 / 北向桥接         │
                 └────────────────────────────────────────────────┘
```

参考实现源码:

- 推送侧 `bifromq-plugin-pro/bifromq-event-collector-plugin/`
    - `event-provider/.../BifromqEventCollectorPluginEventProvider.java` ── topic 路由表
    - `event-provider/.../processor/*EventProcessor.java` ── 各事件字段构造
- 消费侧 `thinglinks-mqs/thinglinks-mqs-biz/.../consumer/kafka/`
    - `MqttMessageKafkaConsumerHandler` / `WebSocketMessageKafkaConsumerHandler` / `TcpMessageKafkaConsumerHandler`
- 常量 `thinglinks-public/thinglinks-common/.../mq/KafkaConsumerTopicConstant.java`

---

## 2. Topic 命名约定(协议前缀 + 事件后缀)

| 事件类型(业务)           | MQTT(基线)                            | WebSocket                                | TCP                                |
|--------------------|-------------------------------------|------------------------------------------|------------------------------------|
| 设备主消息(数据上行入口主通道)   | `thinglinks-pro-mqs-mqttMsg`        | `thinglinks-pro-mqs-websocketMsg`        | `thinglinks-pro-mqs-tcpMsg`        |
| 设备上线 CONNECT       | `mqtt.client.connected.topic`       | `websocket.client.connected.topic`       | `tcp.client.connected.topic`       |
| 客户端主动断开 DISCONNECT | `mqtt.client.disconnect.topic`      | `websocket.client.disconnect.topic`      | `tcp.client.disconnect.topic`      |
| 服务端断开 CLOSE        | `mqtt.server.disconnect.topic`      | `websocket.server.disconnect.topic`      | `tcp.server.disconnect.topic`      |
| 设备被踢 CLOSE         | `mqtt.device.kicked.topic`          | `websocket.device.kicked.topic`          | `tcp.device.kicked.topic`          |
| 订阅成功 SUBSCRIBE     | `mqtt.subscription.acked.topic`     | `websocket.subscription.acked.topic`     | `tcp.subscription.acked.topic`     |
| 取消订阅 UNSUBSCRIBE   | `mqtt.unsubscription.acked.topic`   | `websocket.unsubscription.acked.topic`   | `tcp.unsubscription.acked.topic`   |
| 消息发布 PUBLISH       | `mqtt.distribution.completed.topic` | `websocket.distribution.completed.topic` | `tcp.distribution.completed.topic` |
| 分发错误 ERROR         | `mqtt.distribution.error.topic`     | `websocket.distribution.error.topic`     | `tcp.distribution.error.topic`     |
| 心跳 PING            | `mqtt.ping.req.topic`               | `websocket.ping.req.topic`               | `tcp.ping.req.topic`               |
| 未授权连接              | `mqtt.client.unauthorized`          | (按需补)                                    | (按需补)                              |
| Session 开始         | `mqtt.session.start`                | (按需补)                                    | (按需补)                              |
| Session 结束         | `mqtt.session.stop`                 | (按需补)                                    | (按需补)                              |

**契约要求**:第三方协议接入 ── 把上面 `mqtt.*` 中的 `mqtt` 前缀换成自己的协议名(`websocket` / `tcp` / `coap` / `自定义`)
,其余字段结构 1:1 对齐即可。

---

## 3. 公共信封字段(所有事件必含)

> 每条 Kafka 消息体是 **JSON 对象**(UTF-8 字符串)。下面字段由 `AbstractEventProcessor.enrichEventData` 注入,**所有事件**
> 统一携带。
>
> ⭐ **三个时间字段语义分离,务必区分**:`eventTime`(接入端处理时刻,debug)/ `eventUtc`(事件真实发生瞬间,权威业务时间)/ `eventHlc`(因果时钟,连接状态 / 心跳的 CAS 单调写键)。第三方接入端 **必须产出 `eventHlc` + `eventUtc`**,否则设备连上不显示在线。各事件实体字段、协议差异、HLC 编码见 **[事件字段契约 README](../thinglinks-mqs-entity/README.md)**。

| 字段                | 类型     | 必填 | 示例                          | 说明                                                          |
|-------------------|--------|----|-----------------------------|-------------------------------------------------------------|
| `eventType`       | string | ✅  | `"PUBLISH"`                 | **业务系统事件类型**(规范化值,见下表)。第三方接入时必须填本表的标准值,而不是协议原始事件名           |
| `eventTime`       | long   | ✅  | `1746521094123`             | 接入端**处理时刻**的 Unix 毫秒(plugin worker 取值瞬间);debug / 调度时延观测用,**非事件真实发生时间** |
| `eventTimeStr`    | string | ✅  | `"2026-05-06 22:44:54"`     | `eventTime` 的本地时区可读字符串(`yyyy-MM-dd HH:mm:ss`)               |
| ⭐ `eventUtc`      | long   | ✅  | `1746521094120`             | **事件真实发生瞬间(epoch ms)**。业务展示 / 时序入库 / 桥接下游的权威物理时间;与 `eventTime`(处理时刻)严格区分 |
| ⭐ `eventHlc`      | long   | ✅  | `114413000000000`           | **因果时钟 HLC**(数值远超 ms,**禁止当时间戳写 datetime / 时序索引**)。连接状态 / 心跳的 LWW CAS 单调写键 ── **缺失则 mqs 跳过状态同步,设备连上也不显示在线**。编码 `物理ms<<16 \| 16位逻辑计数器`,与 BifroMQ 内核 HLC 一致(平台侧 `HybridLogicalClockUtil`) |
| `success`         | string | ✅  | `"success"`                 | 兼容字段,常量值 `"success"`(用于下游统一序列化处理)                           |
| `tenantId`        | string | ✅  | `"thinglinks"`              | 多租户隔离键。MQTT 来自 `ClientInfo.tenantId`;第三方接入时取认证后的租户 ID       |
| `clientId`        | string | ✅  | `"5282358452289536_dev001"` | 设备唯一连接 ID(MQTT clientId / WS sessionId / TCP connection ID) |
| `userId`          | string | ⬛  | `"5282358452289536"`        | 关联用户/设备身份(取 metadata.userId,常用于设备身份标识)                      |
| `aclRule`         | string | ⬛  | `"acl-rule-001"`            | ACL 规则匹配标识(BifroMQ 鉴权插件注入,第三方可空)                            |
| `ver` / `version` | string | ⬛  | `"3.1.1"`                   | 协议版本(MQTT v3.1.1 / v5.0;WS 子协议版本;自定义协议版本号)                  |
| `channelId`       | string | ⬛  | `"0x12ab34cd"`              | 底层通信通道 ID(Netty channel id 或同等概念)                           |
| `address`         | string | ⬛  | `"10.0.0.5:51632"`          | 客户端连接地址 `ip:port`                                           |
| `broker`          | string | ⬛  | `"bifromq-node-1"`          | 接入节点标识(broker 节点名 / hostname)                               |
| `sessionType`     | string | ⬛  | `"clean"` / `"persistent"`  | 会话类型                                                        |

**`eventType` 规范化值**(对应 `EventTypeEnum.businessSystemEventType`):

| 标准值           | 触发场景                   |
|---------------|------------------------|
| `CONNECT`     | 设备上线成功                 |
| `DISCONNECT`  | 客户端主动发起断开              |
| `CLOSE`       | 服务端断开(被踢 / 异常 / 协议违规等) |
| `PUBLISH`     | 设备发布消息(数据上行)           |
| `SUBSCRIBE`   | 订阅成功                   |
| `UNSUBSCRIBE` | 取消订阅                   |
| `PING`        | 心跳请求                   |
| `ERROR`       | 分发错误 / 协议错误 等异常        |

下面按 **业务事件类型** 详细给出每个事件的事件特异字段 + 完整 JSON 示例。

---

## 4. CONNECT ── 设备上线

**Topic**: `mqtt.client.connected.topic`(MQTT)/ `websocket.client.connected.topic`(WS)/ `tcp.client.connected.topic`(
TCP)

**事件特异字段**(在公共字段基础上增加):

| 字段                     | 类型      | 必填 | 示例                           | 说明                               |
|------------------------|---------|----|------------------------------|----------------------------------|
| `serverId`             | string  | ⬛  | `"server-1"`                 | 接入服务器实例 ID                       |
| `userSessionId`        | string  | ⬛  | `"sess-7f8e..."`             | Session 唯一 ID                    |
| `cleanSession`         | boolean | ✅  | `true`                       | 是否清空旧会话(MQTT cleanSession 标志)    |
| `sessionPresent`       | boolean | ✅  | `false`                      | 是否有持久 session(SessionPresent 字段) |
| `keepAliveTimeSeconds` | int     | ✅  | `60`                         | 心跳保活间隔(秒)                        |
| `lastWillTopic`        | string  | ⬛  | `"$thing/down/alarm/dev001"` | 遗嘱消息 topic(无遗嘱可省)                |
| `lastWillQos`          | int     | ⬛  | `1`                          | 遗嘱消息 QoS(0/1/2)                  |
| `lastWillRetain`       | boolean | ⬛  | `false`                      | 遗嘱消息是否 retained                  |
| `lastWillPayload`      | string  | ⬛  | `"b2ZmbGluZQ=="`             | 遗嘱消息 payload(Base64,见 第 10 节)       |
| `acl`                  | string  | ⬛  | `"rule-001"`                 | ACL 规则标识                         |

**示例 JSON**:

```json
{
  "eventType": "CONNECT",
  "eventTime": 1746521094000,
  "eventTimeStr": "2026-05-06 22:44:54",
  "success": "success",
  "tenantId": "thinglinks",
  "clientId": "5282358452289536_dev001",
  "userId": "5282358452289536",
  "serverId": "bifromq-node-1",
  "userSessionId": "sess-7f8e2a90-...",
  "cleanSession": true,
  "sessionPresent": false,
  "keepAliveTimeSeconds": 60,
  "lastWillTopic": "",
  "lastWillQos": 0,
  "lastWillRetain": false,
  "lastWillPayload": "",
  "acl": "",
  "ver": "3.1.1",
  "version": "3.1.1",
  "channelId": "0x12ab34cd",
  "address": "10.0.0.5:51632",
  "broker": "bifromq-node-1",
  "sessionType": "clean"
}
```

源码:`processor/ClientConnectedEventProcessor.java`

---

## 5. DISCONNECT ── 客户端主动断开

**Topic**: `mqtt.client.disconnect.topic` 等

**事件特异字段**:

| 字段                  | 类型      | 必填 | 示例      | 说明                              |
|---------------------|---------|----|---------|---------------------------------|
| `withoutDisconnect` | boolean | ✅  | `false` | 是否未发送 DISCONNECT 报文直接断开(异常断开标志) |

**示例 JSON**:

```json
{
  "eventType": "DISCONNECT",
  "eventTime": 1746521234567,
  "eventTimeStr": "2026-05-06 22:47:14",
  "success": "success",
  "tenantId": "thinglinks",
  "clientId": "5282358452289536_dev001",
  "userId": "5282358452289536",
  "withoutDisconnect": false,
  "aclRule": "rule-001",
  "ver": "3.1.1",
  "version": "3.1.1",
  "channelId": "0x12ab34cd",
  "address": "10.0.0.5:51632",
  "broker": "bifromq-node-1",
  "sessionType": "clean"
}
```

源码:`processor/ByClientEventProcessor.java`

---

## 6. CLOSE ── 服务端断开

**Topic**:

- `mqtt.server.disconnect.topic` ── 服务端主动断开(协议违规 / 配额超限等)
- `mqtt.device.kicked.topic` ── 被同 clientId 新连接踢下线

两个 topic 共用相同事件契约,通过 topic 区分原因。**事件无特异字段**,仅公共字段。

**示例 JSON**:

```json
{
  "eventType": "CLOSE",
  "eventTime": 1746521314000,
  "eventTimeStr": "2026-05-06 22:48:34",
  "success": "success",
  "tenantId": "thinglinks",
  "clientId": "5282358452289536_dev001",
  "userId": "5282358452289536",
  "aclRule": "rule-001",
  "ver": "3.1.1",
  "version": "3.1.1",
  "channelId": "0x12ab34cd",
  "address": "10.0.0.5:51632",
  "broker": "bifromq-node-1",
  "sessionType": "clean"
}
```

源码:`processor/ByServerEventProcessor.java` / `processor/KickedEventProcessor.java`

---

## 7. PUBLISH ── 设备发布消息(数据上行)

**Topic**: `mqtt.distribution.completed.topic` 等

> 这是**最重要**的事件 ── 设备遥测数据 / 属性上报 / 命令应答 全走这条通路。
> ⚠ payload 必须按 第 10 节 二进制安全规约用 Base64 编码,**禁止**直接放原始字符串。

**事件特异字段**:

| 字段                 | 类型             | 必填 | 示例                                 | 说明                             |
|--------------------|----------------|----|------------------------------------|--------------------------------|
| `reqId`            | long           | ⬛  | `1746521094123`                    | 请求 ID(BifroMQ 内部对账用)           |
| `topic`            | string         | ✅  | `"$thing/up/property/p001/dev001"` | MQTT 发布主题                      |
| `messageId`        | int            | ✅  | `12345`                            | 消息 ID(MQTT packet identifier)  |
| `qos`              | int            | ✅  | `1`                                | QoS 等级(0/1/2)                  |
| `isRetain`         | boolean        | ✅  | `false`                            | 是否 retained 消息                 |
| `isRetained`       | boolean        | ✅  | `false`                            | 当前是从 retained 消息分发出来(订阅触发)     |
| `isUTF8String`     | boolean        | ✅  | `true`                             | payload 是否合法 UTF-8             |
| `contentType`      | string         | ⬛  | `"application/json"`               | MQTT v5 ContentType            |
| `responseTopic`    | string         | ⬛  | `"$thing/down/ack/dev001"`         | MQTT v5 ResponseTopic          |
| `correlationData`  | string         | ⬛  | `"correlation-001"`                | MQTT v5 CorrelationData(UTF-8) |
| `userProperties`   | string         | ⬛  | `"[]"`                             | MQTT v5 用户属性(json 序列化)         |
| `expiryInterval`   | int            | ⬛  | `3600`                             | MQTT v5 消息过期(秒)                |
| `timestamp`        | long           | ✅  | `1746521094000`                    | 消息生成时间(毫秒)                     |
| `time`             | long           | ✅  | `1746521094000`                    | 同 `timestamp`(冗余字段保留兼容)        |
| **`payload`**      | string(Base64) | ✅  | `"eyJ0ZW1wIjoyNS41fQ=="`           | **载荷的 Base64 编码**(详见 第 10 节)      |
| **`payloadHex`**   | string         | ✅  | `"7b2274656d70223a32352e357d"`     | **载荷的十六进制字符串**(便于调试 / 二进制协议解析) |
| **`originalSize`** | int            | ✅  | `19`                               | 原始字节数(校验完整性用)                  |
| **`encoding`**     | string         | ✅  | `"base64"`                         | 编码方式标识,固定字符串 `"base64"`        |

**示例 JSON**:

```json
{
  "eventType": "PUBLISH",
  "eventTime": 1746521094123,
  "eventTimeStr": "2026-05-06 22:44:54",
  "success": "success",
  "tenantId": "thinglinks",
  "clientId": "5282358452289536_dev001",
  "userId": "5282358452289536",
  "reqId": 1746521094123,
  "topic": "$thing/up/property/p001/dev001",
  "messageId": 12345,
  "qos": 1,
  "isRetain": false,
  "isRetained": false,
  "isUTF8String": true,
  "contentType": "application/json",
  "responseTopic": "",
  "correlationData": "",
  "userProperties": "[]",
  "expiryInterval": 0,
  "timestamp": 1746521094000,
  "time": 1746521094000,
  "payload": "eyJ0ZW1wIjoyNS41LCJodW1pZGl0eSI6NjB9",
  "payloadHex": "7b2274656d70223a32352e352c2268756d6964697479223a36307d",
  "originalSize": 32,
  "encoding": "base64",
  "aclRule": "rule-001",
  "ver": "3.1.1",
  "version": "3.1.1",
  "channelId": "0x12ab34cd",
  "address": "10.0.0.5:51632",
  "broker": "bifromq-node-1",
  "sessionType": "clean"
}
```

源码:`processor/DistedEventProcessor.java`

---

## 8. SUBSCRIBE / UNSUBSCRIBE ── 订阅生命周期

**Topic**:

- SUBSCRIBE ── `mqtt.subscription.acked.topic`
- UNSUBSCRIBE ── `mqtt.unsubscription.acked.topic`

**SUBSCRIBE 事件特异字段**:

| 字段          | 类型     | 必填 | 示例                             | 说明                                   |
|-------------|--------|----|--------------------------------|--------------------------------------|
| `messageId` | int    | ✅  | `100`                          | SUBSCRIBE 报文 ID                      |
| `topic`     | string | ✅  | `"$thing/down/command/dev001"` | 订阅 topic(多 topic 时取首个;批量订阅请拆多条事件)    |
| `granted`   | int    | ✅  | `1`                            | broker 授予的最大 QoS(0/1/2),`128` 表示订阅失败 |

**示例 JSON(SUBSCRIBE)**:

```json
{
  "eventType": "SUBSCRIBE",
  "eventTime": 1746521124000,
  "eventTimeStr": "2026-05-06 22:45:24",
  "success": "success",
  "tenantId": "thinglinks",
  "clientId": "5282358452289536_dev001",
  "userId": "5282358452289536",
  "messageId": 100,
  "topic": "$thing/down/command/dev001",
  "granted": 1,
  "aclRule": "rule-001",
  "ver": "3.1.1",
  "version": "3.1.1",
  "channelId": "0x12ab34cd",
  "address": "10.0.0.5:51632",
  "broker": "bifromq-node-1",
  "sessionType": "clean"
}
```

**UNSUBSCRIBE 事件特异字段**:

| 字段          | 类型     | 必填 | 示例                             | 说明                |
|-------------|--------|----|--------------------------------|-------------------|
| `messageId` | int    | ✅  | `101`                          | UNSUBSCRIBE 报文 ID |
| `topic`     | string | ✅  | `"$thing/down/command/dev001"` | 取消订阅的 topic       |

源码:`processor/SubAckedEventProcessor.java` / `processor/UnsubAckedEventProcessor.java`

---

## 9. PING ── 心跳

**Topic**: `mqtt.ping.req.topic`

**事件特异字段**:

| 字段              | 类型      | 必填 | 示例              | 说明                    |
|-----------------|---------|----|-----------------|-----------------------|
| `pong`          | boolean | ✅  | `true`          | broker 是否已回复 PINGRESP |
| `heartbeatTime` | long    | ⬛  | `1746521154000` | broker 收到心跳的时刻(毫秒);**缺省则 mqs 取 `eventUtc` 兜底** |

**mqs 侧处理**(`DevicePingProcessor` → link `reportDeviceHeartbeat`):

- `last_heartbeat_time` ← `heartbeatTime`(无则 `eventUtc`),**每条 PING 无条件续**;
- `connect_status` ← ONLINE,走 `eventHlc` **HLC CAS 单调写**(60s 节流;`eventHlc` 缺失则只续心跳不动状态)。

> ⚠ PING 同样**必须带 `eventHlc` / `eventUtc`**(见 第 3 节),否则设备连上不显示在线、心跳时间不更新。

**示例 JSON**:

```json
{
  "eventType": "PING",
  "eventTime": 1746521154123,
  "eventTimeStr": "2026-05-06 22:45:54",
  "eventUtc": 1746521154000,
  "eventHlc": 114413000000000,
  "success": "success",
  "tenantId": "thinglinks",
  "clientId": "5282358452289536_dev001",
  "userId": "5282358452289536",
  "pong": true,
  "heartbeatTime": 1746521154000,
  "aclRule": "rule-001",
  "ver": "3.1.1",
  "version": "3.1.1",
  "channelId": "0x12ab34cd",
  "address": "10.0.0.5:51632",
  "broker": "bifromq-node-1",
  "sessionType": "clean"
}
```

源码:`processor/PingReqEventProcessor.java`

---

## 10. ERROR ── 分发错误

**Topic**: `mqtt.distribution.error.topic`

**事件特异字段**:

| 字段        | 类型     | 必填 | 示例                   | 说明                  |
|-----------|--------|----|----------------------|---------------------|
| `reqId`   | string | ✅  | `"req-123"`          | 出错的请求 ID            |
| `code`    | int    | ✅  | `1001`               | 错误码(见 broker 错误码字典) |
| `message` | string | ⬛  | `"qos2 dist failed"` | 出错消息体(可选)           |

源码:`processor/DistErrorEventProcessor.java`

---

## 11. 二进制 payload 编码强制规约

> ⚠ **接入方必读**。Payload 不按本规约编码会导致下游解析错误 / 数据损坏。

**问题**:MQTT payload 是任意字节序列。直接以 String 塞 JSON 时,UTF-8 解码器会把不合法字节(如 `0xE6`)替换为替代字符
`0xEF 0xBF 0xBD`,**永久性破坏二进制数据**。

**规约**:每条 PUBLISH 事件**必须**同时携带 4 个字段:

| 字段             | 用途                                                                |
|----------------|-------------------------------------------------------------------|
| `payload`      | **Base64** 编码字符串。下游需要二进制时 → `Base64.getDecoder().decode(payload)` |
| `payloadHex`   | 十六进制字符串(无 `0x` 前缀,小写)。便于人工调试 / 设备协议解析                             |
| `originalSize` | 原始字节数。下游可用 `decode(payload).length === originalSize` 校验完整性        |
| `encoding`     | 固定字符串 `"base64"`。预留多编码兼容,当前仅此一种                                   |

**第三方接入示例**(Java 伪码):

```java
byte[] raw = mqttPacket.getPayloadBytes();
event.put("payload",      Base64.getEncoder().encodeToString(raw));
event.put("payloadHex",   HexUtil.encodeHexStr(raw));
event.put("originalSize", raw.length);
event.put("encoding",     "base64");
```

---

## 12. 第三方协议接入指南(WS / TCP / 自定义)

接入步骤:

1. **决定 topic 前缀**:把所有 `mqtt.*.*.topic` 中的 `mqtt` 换成自己的协议标识(`websocket` / `tcp` / `coap` /
   `myprotocol`),其余命名 1:1 对齐。
2. **构造事件 JSON**:对每个生命周期事件按本 README 第 3 节 ~ 第 10 节 字段结构构造 JSON 对象。
3. **eventType 必须用业务标准值**(CONNECT / DISCONNECT / CLOSE / PUBLISH / SUBSCRIBE / UNSUBSCRIBE / PING / ERROR),**不要
   **用协议原生事件名。
4. **payload 必须 Base64**(见 第 11 节)。
5. **投到对应 Kafka topic**,key 用 `tenantId` 或 `clientId` 保证同设备消息有序。
6. mqs 侧若已存在对应协议 Consumer Handler(如 `WebSocketMessageKafkaConsumerHandler`)→ 自动消费,无需改 mqs
   代码;若是新协议 → 在 `KafkaConsumerTopicConstant.Mqs` 新增子接口 + 写一个新的 `*MessageKafkaConsumerHandler` 复用
   `ProtocolHandlerFactory` 派发。

**验收清单**:

- [ ] 设备上线 → `*.client.connected.topic` 可见 CONNECT 事件
- [ ] 设备发数据 → `*.distribution.completed.topic` 可见 PUBLISH 事件,`payload` 为 Base64,
  `originalSize === Base64.decode(payload).length`
- [ ] 平台 DeviceAction 表新增 type=PUBLISH,时序库收到解析后属性
- [ ] 设备订阅下行 topic → `*.subscription.acked.topic` 可见 SUBSCRIBE 事件
- [ ] 心跳 → `*.ping.req.topic` 可见 PING 事件,设备状态保持 ONLINE
- [ ] 设备断开 → `*.client.disconnect.topic` 可见 DISCONNECT 事件,DeviceAction 表新增 DISCONNECT,`device.connect_status`
  改 OFFLINE
- [ ] 北向桥接联调:配桥接规则(产品 = 该协议设备的产品,事件类型 = PUBLISH) → 设备发数据 → 第三方下游 Kafka 收到 envelope,
  **与 MQTT 完全一致的字段结构**

---

## 13. 字段值约定补充

### 13.1 `tenantId` 解析顺序

1. 接入端认证后取得的租户 ID(BifroMQ `ClientInfo.tenantId`,WS / TCP 接入端从 token / metadata 取)
2. 不存在 → 用平台默认 tenantId(系统级事件)
3. 仍空 → 视为非法事件,丢弃

### 13.2 `clientId` 推荐格式

`{tenantId}_{deviceIdentification}` ── 这样即便跨租户重名设备 ID,clientId 仍唯一。MQTT v3.1.1 限制 23 字节,新场景建议直接走
v5 解除限制。

### 13.3 时间字段对齐

- `eventTime` / `timestamp` / `heartbeatTime` ── 全部 **Unix 毫秒时间戳**
- `eventTimeStr` ── `yyyy-MM-dd HH:mm:ss` 本地时区(运维可读)
- 跨时区场景下,业务侧应优先用 `eventTime` 做精确对齐,`eventTimeStr` 仅作展示

---

## 14. 排障建议

| 现象                           | 排查方向                                                                                                                                              |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| Kafka 收到事件但 mqs 没消费          | 1. 确认 topic 名拼写与 `KafkaConsumerTopicConstant` 一致 <br> 2. 确认 `spring.kafka.consumer.group-id` 没冲突 <br> 3. 看 `MqttMessageKafkaConsumerHandler` 启动日志 |
| 设备上线 / 离线但 mqs 不更新 device 状态 | 1. 看事件 JSON 是否含 `tenantId` / `clientId` <br> 2. 看 `eventType` 是否为标准值 <br> 3. 看 mqs 日志 `ProtocolHandler.processMessage` 异常栈                        |
| 上行数据 payload 解析乱码            | 99% 是接入端没走 Base64,直接塞了原始字符串。对照 第 11 节 修接入端                                                                                                           |
| 同一设备短时间多条 CONNECT / CLOSE    | 检查接入端是否未做幂等;mqs 侧依赖事件先后投递顺序,生产建议按 `clientId` partition                                                                                            |

---

## 15. 版本与变更

- v1.0 (2026-05-06):MQTT 9 类事件契约首版,基于 `bifromq-event-collector-plugin`
- 后续协议(WS / TCP / CoAP / 自定义)接入时参照本文,**不变更基线契约**;若必须扩展字段,加在事件特异字段中,公共字段保持不变

---

**契约维护**:本文是唯一权威。修改契约必须同步:

1. `bifromq-event-collector-plugin/.../processor/*EventProcessor.java`
2. `thinglinks-mqs-biz/.../consumer/kafka/*MessageKafkaConsumerHandler.java`
3. `thinglinks-public/thinglinks-common/.../mq/KafkaConsumerTopicConstant.java`
4. 本 README

任一文件修改未同步本 README 的契约部分,视为破坏接入约束。
