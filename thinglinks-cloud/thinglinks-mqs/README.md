# thinglinks-mqs —— 设备消息接入与处理服务

> 物联网平台**设备消息接入与统一处理服务**。所有协议(MQTT / WebSocket / TCP)的设备事件 → 协议归一 → 落库 / 物模型 / 时序入库 / 状态机 / 桥接 / 告警,全部从本服务流转。
>
> **架构主线:`bus`(传输层,管"消息怎么流")→ 唯一桥 `DeviceBizDispatchStage` → `biz`(领域层,管"消息意味着什么")。** 改代码请同步本 README。

---

## 1. 服务定位 / 模块拆分

| 维度 | 说明 |
|---|---|
| 服务职责 | 多协议设备消息接入 + 协议归一 + 传输流水线 + 领域处理(落库 / 状态 / 物模型 / 时序) + 桥接出入站 |
| **上游** | ① BifroMQ broker plugin → Kafka;② mqs 自带 WS broker → Kafka;③ mqs 自带 TCP broker → Kafka;④ RocketMQ 桥接入站(rule 端回流) |
| **下游** | DB `device_action` / `device.connect_status`;时序库;RocketMQ 桥接出站(→ rule)/ 告警(→ rule alarm);Feign link(设备元数据)/ product(物模型) |
| 启动入口 | [thinglinks-mqs-server](thinglinks-mqs-server/) |
| 装配入口 | [DeviceBusAutoConfiguration](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/config/DeviceBusAutoConfiguration.java) |

### 1.1 6 个子模块

| 子模块 | 职责 | 顶层包 |
|---|---|---|
| **thinglinks-mqs-entity** | DTO / 枚举 / SPI 接口 / 协议归一载体 | `bus.{adapter,stage,hook,route}`(SPI)、`enumeration.bus`、`entity.protocol`、`entity.device` |
| **thinglinks-mqs-facade** | Feign 接口(供其他服务调 mqs) | `facade.mqs` |
| **thinglinks-mqs-biz-bus** ⭐ | **传输层**:Kafka/RocketMQ 入站、协议归一、PRE/CORE/POST 流水线、传输 stage、bus↔biz 唯一桥 | `mqs.bus.*` |
| **thinglinks-mqs-biz** ⭐ | **领域层**:设备事件编排、动作处理、状态同步、解码、共享服务、桥接 producer | `mqs.{event,mqtt,session,service,bridge}` |
| **thinglinks-mqs-controller** | REST 控制器 + WS/TCP broker 接入端点 + 运维 endpoint | `mqs.{ws,bus}.controller` |
| **thinglinks-mqs-server** | Spring Boot 启动 + Nacos 配置 + dynamictp 线程池 | 启动 main |

**依赖**:`server → controller → biz-bus → biz → entity`;`biz-bus` 依赖 `biz`(经唯一桥调领域逻辑)。

### 1.2 消息中间件

| MQ | 用途 |
|---|---|
| **Kafka** | 设备消息接入(MQTT/WS/TCP 三协议高吞吐数据流) |
| **RocketMQ** | 跨服务总线:桥接出站(→rule)/ 桥接入站(rule→mqs)/ 告警 / 死信 |

---

## 2. 端到端数据流

```
上游(三协议 broker / plugin)──Kafka(header: traceId/tenantId)──┐
                                                                  ▼
┌─────────────────────────── bus 传输层 (mqs.bus) ───────────────────────────┐
│ inbound/kafka/{Mqtt|Tcp|Ws}KafkaInboundConsumer                            │
│   恢复 traceId/tenantId → BusPipelineDispatcher.dispatch(topic, raw)        │
│                                                                            │
│ BusPipelineDispatcher:                                                     │
│   route → adapter.canonicalize → DeviceProtocolEvent                       │
│   PRE  : DeviceCacheEnricher        查 DeviceCacheVO,富化 appId/deviceId/  │
│                                     productId/tenantId 进 event + ctx,切库 │
│   CORE : DeviceBizDispatchStage ◀── 唯一桥:assemble → 委派 biz             │
│   POST : BridgeRelay / AlarmRelay / Metric / Distribution(旁路,best-effort)│
└────────────────────────────────────┬───────────────────────────────────────┘
                                     ▼ (同线程直调,ctx 由 bus 管)
┌─────────────────────────── biz 领域层 (mqs.event) ──────────────────────────┐
│ DeviceEventDispatcher.dispatch(CommonDeviceEvent):                          │
│   ① hooks(before): DeviceConnectStatusSyncHook ── 连接状态 HLC CAS(见第 5 节) │
│   ② 统一落库 device_action(排除高频 PING)── DeviceEventActionService.save  │
│   ③ route processor:                                                       │
│        PUBLISH → DevicePublishProcessor  解码 + 物模型 + 时序入库            │
│        PING    → DevicePingProcessor     续命 + 60s 节流 reconcile ONLINE   │
│        其余(CONNECT/断开/SUB…)无 processor:状态由①、落库由②已完成          │
│   ④ hooks(after): 预留                                                      │
└──────────────────────────────────────────────────────────────────────────┘
        ▼                    ▼                  ▼              ▼
   device_action      device.connect_status   时序库      RocketMQ(POST 桥接/告警)

桥接入站(独立链路):RocketMQ thinglinks-bridge-ingress
  → bus inbound/rocketmq/BridgeIngressRocketmqConsumer 按 tag 分流
    MQTT_FORWARD → DevicePublishProcessor.process(只解码持久化,不计上行、不出站)
    RAW_INSERT   → Feign 直写 device_action
    RULE_TRIGGER → rule Feign triggerRulePolicy
```

**失败语义**:
- **PRE/CORE 抛异常** → 流水线 FAILED,**POST 仍 best-effort 触发**(桥接/告警/指标不丢)。
- **消费层**:dispatch 抛异常 → `BatchListenerFailedException` → 单 record 重试 + DLT(kafka-starter 统一兜底);CORE 业务 FAILED 但未抛 → ERROR 告警 + ack 不重试。
- **biz 内部分级**:钩子(状态)与落库**失败隔离吞**(状态靠后续事件自愈、落库为旁路审计);**processor(解码/续命)异常上抛** → CORE FAILED → DLT(数据不丢)。

---

## 3. 设备接入入口(协议接入在 broker / BifroMQ,mqs 只消费 Kafka)

mqs **不直接持有设备连接**,只消费 Kafka 事件。三协议接入端各自按统一契约([事件字段契约](thinglinks-mqs-entity/README.md))把上行事件投 Kafka:

| 协议 | 接入端(谁持有 session) | 事件 → Kafka |
|---|---|---|
| MQTT | 外部 **BifroMQ** + `bifromq-event-collector-plugin` | `mqtt.*` topic |
| WebSocket | **broker** [WebSocketDeviceOpenAccessProtocolEndpoint](../thinglinks-broker/thinglinks-broker-controller/src/main/java/com/mqttsnet/thinglinks/broker/ws/endpoint/WebSocketDeviceOpenAccessProtocolEndpoint.java)(`@OnOpen/@OnMessage/@OnClose`)| `websocket.*` topic |
| TCP | broker tcp 端点(同模式) | `tcp.*` topic |

> **三协议归一**:WS/TCP 接入端与 BifroMQ 插件设计对齐 —— 落同名 `*.client.connected.topic`(CONNECT)/ `*.ping.req.topic`(PING)/ `*.distribution.completed.topic`(PUBLISH)/ `*.distribution.error.topic`(DISPATCH_ERROR)等;且统一带 `eventHlc/eventUtc` 时钟。下游 bus **协议中立**,不感知差异。WS 接入全链路(注册 / 鉴权 / 会话上下文 / 心跳)见 [broker README 第 4 节](../thinglinks-broker/README.md)。

---

## 4. 包结构 / 职责划分

### 4.1 bus(传输层)`com.mqttsnet.thinglinks.mqs.bus`

```
mqs/bus/
├── inbound/
│   ├── kafka/   AbstractProtocolKafkaInboundConsumer · Mqtt/Tcp/Ws · audit/*
│   └── rocketmq/ BridgeIngressRocketmqConsumer ── 桥接入站(tag 分流,第 6 节)
├── protocol/    ProtocolKafkaPayloadParser · *EdgeAdapter(mqtt/tcp/ws × 4 类)── 原始报文→DeviceProtocolEvent
├── core/        adapter/Registry · route/TopicRouteResolver · stage/DeviceEventStageRegistry
├── dispatcher/  BusPipelineDispatcher · Sync/AsyncStageRunner · SourceTopicHolder
├── stats/ · support/ · config/
└── stage/
    ├── enrich/DeviceCacheEnricher           PRE  富化 deviceCache→event/ctx + 切库
    ├── bizbridge/DeviceBizDispatchStage     CORE 唯一桥:assemble→委派 biz;PUBLISH 计上行
    ├── relay/BridgeRelayStage               POST 桥接出站
    ├── relay/AlarmRealtimeRelayStage        POST 实时告警旁路
    ├── distribution/DistributionResultStage POST 分发失败计数
    └── metric/MetricStage                   POST 指标收口
```

**三相语义**:PRE/CORE 由 [SyncStageRunner](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/dispatcher/SyncStageRunner.java) 跑、失败终止;POST 由 [AsyncStageRunner](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/dispatcher/AsyncStageRunner.java) fire-and-forget、始终触发。CORE 仅业务分组(`DEVICE_DATA` / `DEVICE_LIFECYCLE` 等)进入。

> **重构要点**:旧的 4 个 CORE 业务 stage(decode/lifecycle/heartbeat/persist)已删除,业务全部下沉到 biz 领域层;bus CORE 只剩 `DeviceBizDispatchStage` 一个桥,是 bus↔biz 唯一接缝,biz 演进对 bus 无感。

### 4.2 biz(领域层)`com.mqttsnet.thinglinks.mqs`

```
mqs/
├── event/                              ① 设备事件编排(领域入口)
│   ├── DeviceEventDispatcher           钩子→统一落库→processor→钩子(bus 直调)
│   ├── assembler/CommonDeviceEventAssembler  DeviceProtocolEvent→CommonDeviceEvent(直映射,零回查)
│   ├── processor/
│   │   ├── DeviceEventProcessor (SPI)  supports(actionType) / process(event)
│   │   ├── DevicePublishProcessor      PUBLISH:topic 路由 handler 解码+物模型+时序入库
│   │   └── DevicePingProcessor         PING:续命 + 60s 节流 CAS reconcile ONLINE
│   ├── hook/
│   │   ├── DeviceEventHook (SPI) · DeviceEventContext · DeviceEventHookOrder
│   │   └── impl/DeviceConnectStatusSyncHook   连接状态 HLC CAS 唯一出口(第 5 节)
│   └── counter/LinkDataReportCounter   上行计数(由 bus DeviceBizDispatchStage 调)
├── mqtt/
│   ├── handler/   MqttTopicHandlerFactory + AbstractMessageHandler + 13 TopicHandler(topic 解码)
│   └── service/   OTA / 命令业务(被 handler 调)
├── session/  DeviceConnectStatusSyncer   设备连接状态写入器(HLC 单调写,被 hook/ping 调)
├── service/  DeviceEventActionService(落库) · DeviceDataProcessingService · ProtocolGroovyScriptService
└── bridge/   MqsBridgeEventProducer       桥接出站 producer(被 bus BridgeRelayStage 调)
```

---

## 5. 设备状态 HLC 单调写(connect_status 不回退)

连接状态在 biz `DeviceConnectStatusSyncHook`(钩子)统一写,经 [DeviceConnectStatusSyncer](thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqs/session/DeviceConnectStatusSyncer.java) 走 **event-time LWW CAS**:

```
事件 A: ONLINE  hlc=100   事件 B: OFFLINE hlc=200   事件 C: ONLINE hlc=300
A→C→B 乱序到达:无 HLC 最终错成 OFFLINE;有 HLC 时 B 的 CAS 检查 300<200 失败 → 拒绝,保持 ONLINE(对)
```

| 字段 | 语义 | 用途 |
|---|---|---|
| `eventHlc` | HLC 因果时钟(**非 ms**) | CAS 单调写键;禁止当 epoch ms |
| `eventUtc` | 设备事件真实发生瞬间(epoch ms) | 业务展示 / 时序入库 / 桥接下游 |

- **作用域**:`affectsConnectionStatus()` 的 6 个 action(CONNECT/DISCONNECT/CLOSE/KICKED/HEART_TIMEOUT/ERROR)→ hook 置 ONLINE/OFFLINE;CONNECT→ONLINE,其余→OFFLINE。
- **PING 心跳 + 在线自愈**:`DevicePingProcessor` → `reportDeviceHeartbeat` 一次上报 ── `last_heartbeat_time` 每条 PING 无条件续;`connect_status=ONLINE` 走**同一 CAS**(60s 节流、`eventHlc` 缺失则跳过状态只续心跳)。迟到 PING 的 hlc < 已写 DISCONNECT 的 hlc → CAS reject,不会错翻离线为在线。⭐ 原「直写 ONLINE」已废除,心跳态写入与生命周期事件**统一收口到 CAS**。
- **失败**:syncer facade 失败仅 warn(状态靠下个事件自愈,不 DLT 整事件)。关键日志 `[StatusSyncer] CAS rejected (stale event)` / `[Device.updateByEvent] CAS rejected` = 机制正在工作。
- **底层 CAS**:link [DeviceServiceImpl](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceServiceImpl.java)`#updateDeviceConnectionStatusByEvent` → `WHERE last_status_event_hlc < ?`;`DeviceConnectStatusSyncHook`(生命周期)与 `reportDeviceHeartbeat`(PING)**共用此底层 CAS**。

---

## 6. 桥接(出站 + 入站)

| 维度 | 出站 | 入站 |
|---|---|---|
| topic | `thinglinks-bridge-device-event` | `thinglinks-bridge-ingress` |
| producer | bus [BridgeRelayStage](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/stage/relay/BridgeRelayStage.java)(POST) | rule 端 |
| consumer | rule | **bus** [BridgeIngressRocketmqConsumer](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/rocketmq/BridgeIngressRocketmqConsumer.java) |

**出站**:`DeviceProtocolEvent` → `BridgeMessageEnvelope`(traceId/tenantId/protocolType/actionType/clientId/topic/rawMessage/ts/eventHlc/eventUtc)→ RocketMQ,`actionType` 作 tag,rule 端 `selectorExpression="*"` 全收。

**入站按 tag 分流**:`MQTT_FORWARD`→`DevicePublishProcessor.process`(复用解码持久化)/ `RAW_INSERT`→Feign 直写 / `RULE_TRIGGER`→rule。

> ⚠️ **解耦边界**:桥接入站只复用"解码持久化",**不触发出站桥接、不计设备上行**(直调 `DevicePublishProcessor` 而非走 dispatcher,无连接生命周期)。出站桥接只由设备真实上报(主流程 POST `BridgeRelayStage`)触发。两条流互不绑定。

---

## 7. ⭐ Action Type 完整规范

> 改 [DeviceActionTypeEnum](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/enums/DeviceActionTypeEnum.java) 时同步本表。

| Action | 含义 | Producer | Kafka topic | bus 分组 | biz 处理 | 终点 |
|---|---|---|---|---|---|---|
| **PUBLISH** ⭐ | 设备数据上行 | plugin DISTED / WS/TCP | `*.distribution.completed.topic` | DEVICE_DATA | 落库 + DevicePublishProcessor(解码/物模型/时序) | device_action + 时序 + 桥接 |
| **CONNECT** | 连接成功 | plugin CLIENT_CONNECTED | `*.client.connected.topic` | DEVICE_LIFECYCLE | hook→ONLINE + 落库 | connect_status=ONLINE(HLC) |
| **DISCONNECT** | 主动断开 | plugin BY_CLIENT | `*.client.disconnect.topic` | DEVICE_LIFECYCLE | hook→OFFLINE + 落库 | connect_status=OFFLINE |
| **CLOSE** | 服务端关闭 | plugin BY_SERVER | `*.server.disconnect.topic` | DEVICE_LIFECYCLE | 同上 | OFFLINE |
| **KICKED** | 被踢线 | plugin KICKED | `*.device.kicked.topic` | DEVICE_LIFECYCLE | 同上 | OFFLINE |
| **HEART_TIMEOUT** | 心跳超时 | 应用层 | — | DEVICE_LIFECYCLE | 同上 | OFFLINE |
| **ERROR** | 协议异常 | mqs WS/TCP | (内部) | DEVICE_LIFECYCLE | hook→OFFLINE + 落库 | OFFLINE + 告警 |
| **PING** | 心跳上行 | plugin PING_REQ | `*.ping.req.topic` | DEVICE_LIFECYCLE | DevicePingProcessor(续命+reconcile);**不落库** | 续命 + 节流 reconcile ONLINE |
| **SUBSCRIBE** | 订阅成功 | plugin SUB_ACKED | `*.subscription.acked.topic` | CONTROL_ACK | 仅落库 + 桥接 | audit + 桥接 |
| **UNSUBSCRIBE** | 取消订阅 | plugin UNSUB_ACKED | `*.unsubscription.acked.topic` | CONTROL_ACK | 同上 | audit + 桥接 |
| **DISPATCH_ERROR** ⭐ | broker 分发失败 | plugin DIST_ERROR | `*.distribution.error.topic` | DISTRIBUTION_ACK | 落库 + POST DistributionResultStage 计数 | stats + 桥接 + 告警 |
| **INBOUND** | 第三方订阅源入站 | rule 端 | — | (不进 bus dispatcher) | — | — |
| **UNKNOWN** | 兜底 | `fromValue` 未匹配 | — | — | — | — |

**命名**:`PUBLISH`(业务命名,IoT 共识)+ `DISPATCH_ERROR`(技术命名,broker 真相,95% 是下行投设备失败)。不强求对称是 by design。

**落库口径**:dispatcher 对有 actionType 的事件统一落 `device_action`,**排除高频且与续命冗余的 PING**;PUBLISH 数据另入时序库。

---

## 8. 上下文 / 多租户切库

| 环节 | 机制 |
|---|---|
| 上游 → Kafka | 写 `X-Trace-Id` / `X-Tenant-Id` header |
| Kafka → consumer | [AbstractProtocolKafkaInboundConsumer](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/inbound/kafka/AbstractProtocolKafkaInboundConsumer.java) 读 header → `ContextUtil` |
| PRE enrich | `DeviceCacheEnricher` 拿缓存的业务 tenantId 覆盖,`@DS` 切库 |
| bus → biz | **同线程直调**,ctx 由 bus 线程管理;biz 不再 @Async、不增删 ctx |
| POST 异步 | dynamictp 线程池 + `taskWrapperNames:[ttl,mdc]` 透传 |

---

## 9. 监控 / 排查

**指标(Micrometer)**:`bus_dispatch_total{protocol,action,group,status}` · `bus_stage_executions{stage,phase,status}` · `bus_no_route{topic}` · Redis `bus:stats:*`([BusStatsService](thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/stats/BusStatsService.java))。

**日志关键词**:`[bus.kafka.receive]` · `[bus.dispatch]` · `[bus.stage.fail]` · `[bus.route.resolver]` · `[bus.enricher.device-cache]`(cache miss=设备未注册或后端下行)· `[StatusSyncer]`(HLC CAS)· `[DevicePublish]`(解码持久化)· `[BridgeIngressConsumer]`(桥接入站)。

**常见排查**:

| 现象 | 第一手段 |
|---|---|
| 某 topic 没处理 | `[bus.route.resolver] indexed N routes` 确认注册;运行时 `NO_ROUTE` |
| PUBLISH 没落库/解码 | bus group 是否 `DEVICE_DATA`;plugin body `eventType=PUBLISH`;`[bus.enricher.device-cache] cache miss`(后端下行正常 / 设备未预热) |
| 状态没切换 | hook `supports` 是否命中(`affectsConnectionStatus`);cache 缺失会影响 |
| 快插拔状态错乱 | `[StatusSyncer] CAS rejected` 有=HLC 拒绝迟到(正常);无但仍错=plugin 没发 eventHlc |
| 桥接消息丢 | POST swallow,`[bus.stage.fail] stage=BridgeRelayStage`;outcome 仍 SUCCESS,看指标 |
| 桥接入站没持久化 | `[DevicePublish] dispatch handler=...` 是否出现;无则 topic 未匹配 handler |

**运维接口**:`POST /mqs/bus/test/dispatch`([DeviceBusTestController](thinglinks-mqs-controller/src/main/java/com/mqttsnet/thinglinks/mqs/bus/controller/DeviceBusTestController.java))· `GET /mqs/bus/stats/today/dispatch`。

---

## 10. 如何扩展(SPI)

bus SPI 接口位于 mqs-entity 的 `com.mqttsnet.thinglinks.bus.{adapter,stage,hook,route}`;biz SPI 是 `DeviceEventProcessor`(动作级)/ `TopicHandler`(PUBLISH 内 topic 级)/ `DeviceEventHook`(横切)。

| 需求 | 选择 | 关键约束 |
|---|---|---|
| 接新协议(CoAP…) | bus `ProtocolEdgeAdapter` + `@TopicRoute` + `*KafkaInboundConsumer` | 协议归一,topic 路由 |
| 新传输旁路(MQ/指标) | bus `DeviceEventStage`(POST,放 `stage/`) | best-effort,registry 自动收集 |
| 全局准入 / 黑名单 | bus `DeviceEventInterceptor` | `DeviceEventDropException` 终止 |
| **新动作处理** | biz `DeviceEventProcessor`(放 `event/processor/`) | 一个 actionType 一个,**不改 dispatcher** |
| **厂商自定义上行 topic 处理** | biz `TopicHandler`(放 `uplink/handler/`,可继承 `AbstractMessageHandler`) | 实现 `topicPattern()` 正则 + `@Service`,`TopicHandlerFactory` 启动扫描自动注册,**不改工厂**;PUBLISH 内按 topic 正则**首个命中即路由** |
| **新横切(限流/审计)** | biz `DeviceEventHook`(放 `event/hook/impl/`) | 按 order,自动触发 |
| 接新 Kafka topic(已有协议) | 扩 `@TopicRoute.value` + `@KafkaListener(topics)` | parser map 加映射 |

> **上行处理三层扩展(由粗到细)**:① 动作级 `DeviceEventProcessor`(按 actionType,如 PUBLISH/CONNECT);② PUBLISH 内 topic 级 `TopicHandler`(按 topic 正则路由,厂商自定义上报链路,**与老代码一致**);③ 无代码级「规则脚本」前置转换(把私有报文翻译成标准 datas)。
>
> 运行链路:`DevicePublishProcessor → InboundScriptTransformer.resolveEventSource(规则脚本/未命中透传) → TopicHandlerFactory.findMatchingHandler(topic) → handler.handle()`。规则脚本是 **opt-in**:未命中转换则**原样透传**,`TopicHandler` 照常按原 topic 命中 → 老用户自定义 handler **零改动继续可用**。
>
> ⚠️ **同一 topic 勿同时配「规则脚本 + 自定义 handler」**:转换会把 topic 改写成 `/v1/devices/.../datas`,匹配原 topic 的 handler 就不命中了 —— 二选一。

### 10.1 自定义上行 `TopicHandler` 示例(厂商私有 topic → 复用 datas 落库)

接入 3 步,**不改工厂 / 路由代码**:① `implements TopicHandler`;② `topicPattern()` 返回 topic 正则(`^...$` 全匹配);③ `@Service` 声明 Bean → `TopicHandlerFactory` 启动自动注册。import 同 `DeviceDatasHandler`(`fastjson2.JSON`、`LinkCacheDataHelper`、`DeviceCacheVO`、`UplinkMessageEventSource`、`DeviceDataProcessingService`、`TopoDeviceDataReportParam`)。

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class VendorReportHandlerExample implements TopicHandler {

    private final LinkCacheDataHelper linkCacheDataHelper;                 // 取设备缓存
    private final DeviceDataProcessingService deviceDataProcessingService; // 复用 datas 落库:TDengine + 影子

    /** 只匹配本厂商私有上行 topic;例:/vendorx/devices/{deviceId}/report */
    @Override
    public String topicPattern() {
        return "^/vendorx/devices/[^/]+/report$";
    }

    @Override
    public void handle(UplinkMessageEventSource source) {
        String topic = source.getTopic();
        String raw = new String(source.getPayloadBytes(), StandardCharsets.UTF_8); // 二进制改用 source.getPayloadHex()

        // 1) 从 topic 取设备标识(按你的 topic 结构调整)
        String[] seg = topic.split("/");
        String deviceId = seg.length > 3 ? seg[3] : null;

        // 2) 设备缓存:优先用上行已透传的,缺失再按 id 取
        DeviceCacheVO device = source.getDeviceCacheVO() != null
            ? source.getDeviceCacheVO()
            : linkCacheDataHelper.getDeviceCacheVO(deviceId).orElse(null);
        if (device == null) { log.warn("设备未命中缓存,跳过 {}", deviceId); return; }

        try {
            // 3) 解析私有报文 → 映射成物模型属性(键 = propertyCode)
            JSONObject body = JSON.parseObject(raw);
            Map<String, Object> data = new HashMap<>();
            data.put("battery", body.getInteger("b"));
            data.put("brightness", body.getInteger("bri"));
            data.values().removeIf(v -> v == null);

            // 4) 组装平台标准结构(devices → services → data):Map→JSON→param
            String std = JSON.toJSONString(Map.of("devices", List.of(Map.of(
                "deviceId", device.getDeviceIdentification(),
                "services", List.of(Map.of(
                    "serviceCode", "default_attributes_controls",
                    "data", data, "eventTime", System.currentTimeMillis()))))));
            TopoDeviceDataReportParam param = JSON.parseObject(std, TopoDeviceDataReportParam.class);

            // 5) 落库:TDengine 时序 + 设备影子(前提:该版本已发布、服务已启用,超表才在)
            deviceDataProcessingService.processDeviceDataReport(param);
        } catch (Exception e) {
            log.error("处理失败 deviceId={} err={}", deviceId, e.getMessage(), e); // 不外抛,避免阻断上行
        }
    }
}
```

要点:`topicPattern()` 用 `^...$` 且互不重叠(**首个命中即路由**);`handle()` 内 `try/catch` **不外抛**;落库可复用 `processDeviceDataReport`(白嫖 TDengine + 影子),或在 `handle()` 里走完全自定义存储;加密信封参考 `DeviceDatasHandler` 调 `protocolMessageAdapter.decryptMessage` 解密后再解析。

---

## 11. 跨仓库联动 / 字典维护

| 改 mqs 哪里 | 同步改 |
|---|---|
| `DeviceActionTypeEnum` 增删改 | ① bifromq-plugin-pro `EventTypeEnum`(如来自 plugin)② DBA 字典 SQL ③ rule 规则 JSON ④ 本 README 第 7 节 |
| Kafka topic 新增 | EdgeAdapter `@TopicRoute` + `KafkaInboundConsumer` + 本 README 第 4 节 |
| `BridgeMessageEnvelope` 字段 | `BridgeRelayStage.toEnvelope` + rule 端解析 + 本 README 第 6 节 |
| HLC 流程 | 本 README 第 5 节 + link `DeviceServiceImpl#updateDeviceConnectionStatusByEvent` |

字典:`LINK_DEVICE_ACTION_TYPE`(13 项 ↔ enum)/ `RULE_CONDITION_DEVICE_ACTION_TIRGGER_TYPE`(12 项:11 个 MQS 动作 + OFFLINE 业务分组,规则 UI)── 在运营后台维护,改 enum 同步通知 DBA。

**PR Review 红线**:代码改动是否影响本 README 某节?改 enum 是否同步字典 SQL / plugin / rule?改 envelope 是否同步 rule 端?

---

外部相关:[bifromq-plugin](../bifromq-plugin/)(上游 Kafka producer)· [thinglinks-rule](../thinglinks-rule/)(桥接消费 / 告警 / 规则)。
