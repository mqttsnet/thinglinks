# thinglinks-broker · 协议接入适配层

> ThingLinks IoT 云平台 5 个核心微服务之一(broker / [link](../thinglinks-link/README.md) / [mqs](../thinglinks-mqs/README.md) / [rule](../thinglinks-rule/README.md) / tds)。
> **本服务不是 MQTT broker 本体**:真正的 MQTT broker 是外部独立部署的 [BifroMQ](https://bifromq.io);本服务是 **broker 适配层 + 平台自带的 WebSocket broker 集群实现**。

---

## 🚀 快速导航

| 我想... | 直接看 |
| --- | --- |
| 看 MQTT 适配怎么发消息 / 踢线 / 查 session | [MqttBrokerService](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/service/MqttBrokerService.java) · [Impl](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/service/impl/MqttBrokerServiceImpl.java) |
| 看 WS broker 集群下行命令广播 | [WebSocketBrokerServiceImpl](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/service/impl/WebSocketBrokerServiceImpl.java) · [WsCommandDownlinkListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/command/WsCommandDownlinkListener.java) · [WsDeviceSessionRegistry](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/common/session/WsDeviceSessionRegistry.java) |
| 看 WS 设备接入(@OnOpen / @OnMessage / @OnClose) | [WebSocketDeviceOpenAccessProtocolEndpoint](thinglinks-broker-controller/src/main/java/com/mqttsnet/thinglinks/broker/ws/endpoint/WebSocketDeviceOpenAccessProtocolEndpoint.java) |
| 看 WS 心跳超时 / 跨节点同步 | [WsHeartbeatTracker](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatTracker.java) · [WsHeartbeatTimeoutChecker](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatTimeoutChecker.java) · [WsHeartbeatSyncListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatSyncListener.java) |
| 调本服务的 Feign 接口(其它服务用) | [thinglinks-broker-api](thinglinks-broker-facade/thinglinks-broker-api/) |
| 看启动类 / 端口 / Nacos 配置 | [BrokerServerApplication](thinglinks-broker-server/src/main/java/com/mqttsnet/thinglinks/BrokerServerApplication.java) · [application.yml](thinglinks-broker-server/src/main/resources/application.yml) · [thinglinks-broker-server.yml](../docs/config/nacos/DEFAULT_GROUP/thinglinks-broker-server.yml) |
| 看容器化部署 | [Dockerfile](thinglinks-broker-server/Dockerfile) |

---

## 1. 服务定位 / 5 子模块分工

`thinglinks-broker` 是 **协议接入适配层**,只做两件事:

1. **MQTT broker 适配** —— 把外部 BifroMQ 的 HTTP 管理 API 封装成平台统一的 `MqttBrokerService` + Feign Facade,业务侧(link / mqs / rule / 上层 OpenAPI)看不见 BifroMQ。
2. **WebSocket broker(平台自带,集群分布式)** —— 平台自实现的 WS broker,持有 ws 设备 session、靠 RocketMQ 广播下发命令,让多副本部署下任一节点都能把命令送到设备所在节点(持有该设备 TCP 的节点投递)。

| 子模块 | 角色 | 关键依赖 |
| --- | --- | --- |
| [thinglinks-broker-entity](thinglinks-broker-entity/) | DTO / VO / 枚举,被 facade / biz / controller 共用 | 无 |
| [thinglinks-broker-facade](thinglinks-broker-facade/) | 三态部署的 Facade(api 接口 + boot/cloud 实现) | 见 第 5 节 |
| [thinglinks-broker-biz](thinglinks-broker-biz/) | 核心业务实现 ── MQTT 适配 + WS broker | BifroMQ HTTP API · Redis · Kafka · RocketMQ |
| [thinglinks-broker-controller](thinglinks-broker-controller/) | REST + WebSocket Endpoint 暴露 | jakarta.websocket · Spring Web |
| [thinglinks-broker-server](thinglinks-broker-server/) | Spring Boot 启动器 | 见 [BrokerServerApplication](thinglinks-broker-server/src/main/java/com/mqttsnet/thinglinks/BrokerServerApplication.java) |

---

## 2. 两大业务维度俯瞰

| 维度 | MQTT broker 适配 | WebSocket broker(平台自带) |
| --- | --- | --- |
| **broker 在哪** | 外部 ── BifroMQ 进程 | 本服务进程内(jakarta.websocket) |
| **本服务承担** | HTTP API 客户端 + 业务封装 | session 持有 + 集群路由 + 心跳 |
| **session 持有方** | BifroMQ 自己 | 本服务([WebSocketSubject.Holder](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/session/WebSocketSubject.java) 内存表) |
| **设备上行入口** | BifroMQ → mqs(BifroMQ rule) | `@OnMessage` → Kafka → mqs |
| **下行命令入口** | `MqttBrokerService.publishMessage` → BifroMQ `/pub` | `WebSocketBrokerService.publishMessage` → RocketMQ 广播 → 持有 session 的节点 → socket |
| **集群协作** | BifroMQ 自身集群 | RocketMQ 广播下发 + Redis session 信息(多节点共享在线 / 渠道) |

⭐ 两个维度共用同一 spring boot 进程,但代码完全按包路径隔离:`broker.mqtt.*` vs `broker.ws.*`,公共能力放 `broker.common.*`。

---

## 3. MQTT broker 适配(对接 BifroMQ 外部 broker)

### 3.1 调用链

```
业务侧 (link / mqs / openapi)
  → MqttBrokerOpenAnyUserFacade           (Feign / boot)
    → /anyUser/mqttBrokerOpen/...         (REST,controller)
      → MqttBrokerService                 (业务编排,本服务 biz)
        → BifroMqFacade                   (BifroMQ HTTP API Feign 包装)
          → BifroMQApi @FeignClient        ── url 由 thinglinks.feign.bifromq.bifromq-api-server 配置
            → BifroMQ HTTP API (:8091)
```

### 3.2 核心能力(8 个方法,见 [MqttBrokerService](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/service/MqttBrokerService.java))

| 方法 | BifroMQ HTTP | 用途 |
| --- | --- | --- |
| `publishMessage` | `POST /pub` | 下行发消息到设备 topic |
| `getSessionInfo` | `GET /session` | 查 MQTT session 详情 |
| ⭐ `isOnline` | `GET /session` | **三态在线判定**(在线 / 离线 / 不确定),是设备 status 同步核心 |
| `expireSession` | `DELETE /session` | 按租户批量过期持久 session |
| `killClientConnection` | `DELETE /kill` | 踢线 |
| `addTopicSubscription` | `PUT /sub` | 给 session 加订阅 |
| `removeTopicSubscription` | `DELETE /unsub` | 取消订阅 |
| `retainMessage`(仅 [BifroMqFacade](thinglinks-broker-facade/thinglinks-broker-api/src/main/java/com/mqttsnet/thinglinks/broker/BifroMqFacade.java)) | `POST /retain` | 保留消息(retain) |

### 3.3 三态在线判定(`isOnline`)

⭐ **关键设计**:不要把 broker 异常误判成"离线",否则设备状态机会被刷脏。实现见 [MqttBrokerServiceImpl#isOnline](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/service/impl/MqttBrokerServiceImpl.java)。

| 来源 | 语义 | 返回 |
| --- | --- | --- |
| `getSessionInfo` 2xx + 非空 | 在线 | `R.success(true)` |
| Feign 404 → [SessionNotFoundException](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/exception/SessionNotFoundException.java) | 离线 | `R.success(false)` |
| 任意其它异常(网络 / 反序列化 / 5xx) | **不确定** | `R.fail(...)` ── 调用方必须保留现状,不许写库 |

依赖方:见 [SessionStatusResolver](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/session/SessionStatusResolver.java)(mqs)。

### 3.4 下行 payload 处理

`publishMessage` 已经在 [PublishMessageRequestVO](thinglinks-broker-entity/src/main/java/com/mqttsnet/thinglinks/vo/query/PublishMessageRequestVO.java) 里支持 `forceBase64Decode`,实现细节见 [MqttBrokerServiceImpl#callPublishBifromqApi](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/service/impl/MqttBrokerServiceImpl.java);BifroMQ 强制要二进制 payload,不能直接发 String。

---

## 4. WebSocket broker(平台自带,集群分布式)

### 4.1 角色分工

| 角色 | 在哪 |
| --- | --- |
| WS 设备接入端点(`@ServerEndpoint`) | [WebSocketDeviceOpenAccessProtocolEndpoint](thinglinks-broker-controller/src/main/java/com/mqttsnet/thinglinks/broker/ws/endpoint/WebSocketDeviceOpenAccessProtocolEndpoint.java) |
| 节点内 session 表 | [WebSocketSubject.Holder](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/session/WebSocketSubject.java)(ConcurrentHashMap) |
| WS 设备 session 信息(Redis,多节点共享) | [WsDeviceSessionRegistry](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/common/session/WsDeviceSessionRegistry.java)(存 [WsDeviceSessionInfo](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/cache/broker/ws/WsDeviceSessionInfo.java),TTL 90s) |
| 下行命令广播消费者(各节点) | [WsCommandDownlinkListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/command/WsCommandDownlinkListener.java)(RocketMQ BROADCASTING,查本地 session 投递) |
| jakarta.websocket 注册器(`@EnableWebSocket` + `ServerEndpointExporter`) | [WebSocketConfig](thinglinks-broker-server/src/main/java/com/mqttsnet/thinglinks/broker/config/WebSocketConfig.java) ── ⚠ **必须放 broker-server 主应用模块**(挨着启动类);放 biz 等库模块时 Undertow 下端点注册不上、握手 404 |

### 4.2 上行链路(设备 → mqs)

**连接地址**(经网关 `wsBroker` 路由):
`ws://<gw>/wsBroker/anyUser/deviceOpenSocket/accessProtocol/socket/{tenantId}/{clientId}?username=&password=`
— 租户由路径段 `{tenantId}` 直接带入(不再从 clientId 解析);username/password 走 query 做账号认证。

**报文统一协议**:业务帧 `{"topic":"<ThingLinks Topic>","payload":"<统一消息体JSON字符串>"}`(topic、payload 都在报文里);心跳帧 `{"type":"PING"}`。

```
设备 ─── ws 握手 ──▶ broker-A.@OnOpen(EndpointConfig + @PathParam tenantId/clientId)
                  ├─ Configurator 握手阶段从 query 取 username/password 存 userProperties
                  ├─ 异步 Feign 调 link 设备认证 (DeviceOpenAnyTenantFacade.clientConnectionAuthentication, ACCOUNT_MODE)
                  ├─ 认证通过 → 构建 WsDeviceSessionInfo{clientId,tenantId,username,deviceIdentification,productIdentification,protocol,channelId,connectTime}
                  │            存入 session.userProperties ── ⭐「信息存在 == 已认证」,替代独立 wsAuthenticated 标记
                  ├─ WebSocketSubject.Holder.registerSession() + WsDeviceSessionRegistry.save()(写 Redis 供多节点共享)
                  └─ Kafka → THINGLINKS_WEBSOCKET_CLIENT_CONNECTED_TOPIC → mqs

设备 ─── 报文 ──▶ broker-A.@OnMessage(只收 text;clientId/topic/租户 全从会话上下文取,无需重复解析)
                  ├─ 鉴权门:取不到上下文(未认证 / 认证空窗期)→ 丢弃,杜绝冒充灌数据
                  ├─ WsHeartbeatTracker.update()  ── 任何报文都刷本地 lastActiveTime(节点内存级存活)
                  ├─ PING 帧 → Kafka THINGLINKS_WEBSOCKET_PING_REQ_TOPIC ── 经 DevicePingProcessor 平台续命 + CAS 在线校准(与 BifroMQ PING 对齐)
                  └─ 业务帧 {topic,payload} → Kafka THINGLINKS_WEBSOCKET_DISTRIBUTION_COMPLETED_TOPIC(topic 决定下游路由,payload 为数据载荷)

设备 ─── close ──▶ broker-A.@OnClose
                  ├─ WsDeviceSessionRegistry.remove()               ── Redis 删 session 信息
                  ├─ Kafka → THINGLINKS_WEBSOCKET_CLIENT_DISCONNECTED_TOPIC(仅认证通过的会话发,未认证不发配对断开)
                  └─ WebSocketSubject.unregisterSession()           ── 清本地
```

> ⭐ 事件统一带时钟:broker 产出的 `ClientConnectedEvent` / `PingReqEvent` / `DistedEvent` 等均继承 `BaseEvent`,
> 经 `@Builder.Default` 自动盖 `eventHlc`(`HybridLogicalClockUtil`,编码同 BifroMQ 内核 HLC)/ `eventUtc`,
> 下游 mqs 据此做连接状态 / 心跳的单调写,详见 [事件字段契约](../thinglinks-mqs/thinglinks-mqs-entity/README.md)。

### 4.3 下行命令 RocketMQ 广播(⭐ 集群分布式核心)

业务侧调任一 broker 节点,该节点编码后**广播**给所有 broker 节点;每个节点查本地 session 表,只有持有该设备 TCP 的那一个节点投递。**不存 owner ip:port、不点对点转发。** 实现见 [WebSocketBrokerServiceImpl#publishMessage](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/service/impl/WebSocketBrokerServiceImpl.java) + [WsCommandDownlinkListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/command/WsCommandDownlinkListener.java)。

```
业务侧 → broker-B(入口任一节点).WebSocketBrokerService.publishMessage(vo)
  ① 在线校验:WsDeviceSessionRegistry.isOnline(tenant, clientId) → 查 Redis session 信息;不在线 → throw BizException("设备不在线")
  ② 编码:WsCommandProtocolEncoder.encodeDown(topic, payload, messageId)(producer 侧一次性编码)
  ③ RocketMQ 广播:asyncSend(WebSocket.COMMAND_DOWNLINK, WsCommandBroadcastEvent{clientId, encodedMessage, ...})
        │  BROADCASTING ── 每个 broker 节点都收到同一份
        ▼
  ④ broker-A / broker-B / broker-C ... 各自 WsCommandDownlinkListener.onMessage：
        ├─ 查本地 WebSocketSubject.Holder.get(clientId)
        ├─ 命中(持有该设备 TCP 的节点)──▶ publishLocal() ──▶ WebSocketSubject.notify(json) ──▶ socket
        └─ 未命中 ──▶ 静默忽略
```

⭐ 这取代了早期"Redis 存 owner ip:port + RestTemplate 反向调用"的单播路由 —— 后者在容器换 IP / 端口不可达 / 多 RestTemplate Bean 等场景脆弱。命令为低频必达场景,广播扇出开销可忽略,且不依赖任何节点地址;心跳同步本就走广播([WsHeartbeatSyncListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatSyncListener.java)),两者机制一致。设备整条会话渠道信息存 Redis([WsDeviceSessionInfo](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/cache/broker/ws/WsDeviceSessionInfo.java)),供多节点查在线 / 渠道,而非用于路由。

### 4.4 心跳与超时

| 组件 | 角色 |
| --- | --- |
| [WsHeartbeatTracker](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatTracker.java) | 设备任意上行报文都调一次:刷本地 `lastActiveTime` + RocketMQ 广播(跨节点同步)。Redis session 续期在端点 `touchHeartbeat` 里 `save`(同节奏、单次 SET、自愈) |
| [WsHeartbeatTimeoutChecker](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatTimeoutChecker.java) | 节点本地 ScheduledExecutor,每 30s 扫一次,`now - lastActiveTime > 90s` 则 `session.close()` → 触发 `@OnClose` 链路 |
| [WsHeartbeatSyncListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatSyncListener.java) | RocketMQ **BROADCASTING** 模式订阅,跨节点同步 `lastActiveTime`,防止"重连漂移"窗口下旧 session 被误关 |

参数(默认值)可在 nacos 覆盖:`thinglinks.ws.heartbeat.timeout-ms=90000` · `thinglinks.ws.heartbeat.check-interval-ms=30000`。

⭐ **两层心跳,各司其职**(别混):

| 层 | 触发 | 作用 | 写哪 |
| --- | --- | --- | --- |
| broker 内存级存活 | 任何报文(`WsHeartbeatTracker`) | 防 90s 超时关连接 | 本地 `lastActiveTime`(+ RocketMQ 广播跨节点同步) |
| Redis session 信息续期 | 任何报文(端点 `touchHeartbeat` → `save`) | 续 90s TTL + 自愈 + 刷 `lastActiveTime`,与本地存活同节奏,保证多节点在线判断准 | Redis `def_ws_session:{clientId}` |
| 平台 DB `last_heartbeat_time` | **仅 PING 帧** → Kafka PING topic → mqs `DevicePingProcessor` | 业务展示心跳时间 + CAS 校准在线 | link `device.last_heartbeat_time`(无条件续)+ `connect_status`(走 eventHlc CAS 单调写) |

即:数据帧只刷内存存活(不更新 DB 心跳时间),与 MQTT 一致;DB 心跳时间只由 PING 推进。下游处理见 [thinglinks-mqs/README.md](../thinglinks-mqs/README.md)。

---

## 5. Facade 三态部署(api / boot-impl / cloud-impl)

ThingLinks 通用 facade 拆分:**接口在一个 jar**,**进程内实现在另一个 jar**,**远程 Feign 实现在第三个 jar**。其它服务按部署形态(单体 / 微服务)二选一引入实现 jar。

| 子模块 | 内容 | 何时引入 |
| --- | --- | --- |
| [thinglinks-broker-api](thinglinks-broker-facade/thinglinks-broker-api/) | 接口 + Hystrix Fallback 声明 | 总是引入(只依赖接口) |
| [thinglinks-broker-boot-impl](thinglinks-broker-facade/thinglinks-broker-boot-impl/) | 直接调本进程 `MqttBrokerService` / `WebSocketBrokerService` | 单体部署(broker 与调用方同 JVM) |
| [thinglinks-broker-cloud-impl](thinglinks-broker-facade/thinglinks-broker-cloud-impl/) | 通过 `@FeignClient` 调远端 broker REST | 微服务部署(默认) |

三个对外 Facade:

| Facade | 接口 | boot 实现 | cloud Feign | Fallback |
| --- | --- | --- | --- | --- |
| MqttBroker | [MqttBrokerOpenAnyUserFacade](thinglinks-broker-facade/thinglinks-broker-api/src/main/java/com/mqttsnet/thinglinks/broker/MqttBrokerOpenAnyUserFacade.java) | [boot](thinglinks-broker-facade/thinglinks-broker-boot-impl/src/main/java/com/mqttsnet/thinglinks/broker/MqttBrokerOpenAnyUserFacadeImpl.java) | [Api](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/MqttBrokerOpenAnyUserApi.java) + [cloud Impl](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/facade/impl/MqttBrokerOpenAnyUserFacadeImpl.java) | [Fallback](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/hystrix/MqttBrokerOpenAnyUserApiFallback.java) |
| WebSocketBroker | [WebSocketBrokerOpenAnyUserFacade](thinglinks-broker-facade/thinglinks-broker-api/src/main/java/com/mqttsnet/thinglinks/broker/WebSocketBrokerOpenAnyUserFacade.java) | [boot](thinglinks-broker-facade/thinglinks-broker-boot-impl/src/main/java/com/mqttsnet/thinglinks/broker/WebSocketBrokerOpenAnyUserFacadeImpl.java) | [Api](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/WebSocketBrokerOpenAnyUserApi.java) + [cloud Impl](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/facade/impl/WebSocketBrokerOpenAnyUserFacadeImpl.java) | [Fallback](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/hystrix/WebSocketBrokerOpenAnyUserApiFallback.java) |
| BifroMQ 原生 API | [BifroMqFacade](thinglinks-broker-facade/thinglinks-broker-api/src/main/java/com/mqttsnet/thinglinks/broker/BifroMqFacade.java) | [boot](thinglinks-broker-facade/thinglinks-broker-boot-impl/src/main/java/com/mqttsnet/thinglinks/broker/BifroMqFacadeImpl.java) | [Api](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/BifroMQApi.java) + [cloud Impl](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/facade/impl/BifroMqFacadeImpl.java) | [Fallback](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/hystrix/BifroMQApiFallback.java) |

⭐ `BifroMqFacade` 是给 **job / 任务调度** 直接用的"裸 BifroMQ API";一般业务请用 `MqttBrokerOpenAnyUserFacade`(封装好了参数 VO + 错误码 + 三态 isOnline)。

---

## 6. 跨服务联动

### 6.1 mqs 主动调 broker(下行)

mqs 收到设备上行消息后,经常需要回写响应到设备(如 OTA 升级 / 命令回执 / 时间同步)。典型场景:

| mqs 处理器 | 调用 | 用途 |
| --- | --- | --- |
| [TimeSyncRequestHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/TimeSyncRequestHandler.java) | `MqttBrokerOpenAnyUserFacade.sendMessage` | 时间同步响应 |
| [OtaCommandResponseHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/OtaCommandResponseHandler.java) | 同上 | OTA 命令响应 |
| [CommandResponseHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/CommandResponseHandler.java) | 同上 | 通用命令响应 |
| [DeviceDatasHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/DeviceDatasHandler.java) / [QueryDeviceHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/QueryDeviceHandler.java) / [SecretKeyHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/SecretKeyHandler.java) 等 | 同上 | 各类 ack / 推送 |
| [SessionStatusResolver](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/session/SessionStatusResolver.java) | `MqttBrokerOpenAnyUserFacade.isOnline` | 设备状态机判定(三态) |

mqs 处理器统一通过 [AbstractMessageHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/mqtt/handler/factory/AbstractMessageHandler.java) 持有 `MqttBrokerOpenAnyUserFacade` 引用。

### 6.2 link 调 broker(踢人 / 命令下发)

| link 服务 | 调用 | 用途 |
| --- | --- | --- |
| [DeviceActionServiceImpl](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceActionServiceImpl.java) | `mqttBrokerOpenAnyUserFacade.closeConnection` | 设备禁用 / 解绑时主动踢线 |
| [DeviceCommandServiceImpl](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/device/service/impl/DeviceCommandServiceImpl.java) | `MqttBrokerOpenAnyUserFacade` + `WebSocketBrokerOpenAnyUserFacade` | 统一命令下发(按设备协议路由 MQTT / WS) |
| [OtaTaskExecutionHandler](../thinglinks-link/thinglinks-link-biz/src/main/java/com/mqttsnet/thinglinks/ota/service/statemachine/event/handler/OtaTaskExecutionHandler.java) | 同上 | OTA 状态机执行任务下发 |

### 6.3 broker 调 link(认证)

WS 设备 `@OnOpen` 时,broker 反向调 link 做认证。

| 调用方 | Feign | 用途 |
| --- | --- | --- |
| [WebSocketDeviceOpenAccessProtocolEndpoint](thinglinks-broker-controller/src/main/java/com/mqttsnet/thinglinks/broker/ws/endpoint/WebSocketDeviceOpenAccessProtocolEndpoint.java) | `DeviceOpenAnyTenantFacade.clientConnectionAuthentication`(ACCOUNT_MODE,认证结果带回 deviceInfoResult 用于建会话上下文) | 认证失败 → `session.close(CANNOT_ACCEPT)` |

> ⚠ **依赖**:`DeviceOpenAnyTenantFacade` 的实现(`@Service` + Feign)在 `thinglinks-link-cloud-impl`,broker-server 必须引它(broker-biz 只引了 link-api 接口);缺它运行期 facade 无 bean,WS `@OnOpen` 鉴权直接 NPE。

### 6.4 broker → Kafka → mqs(WS 上行事件结构)

broker WS 端点把上行事件封装成 `BaseEvent` 子类投 Kafka。每条 = **公共信封** + 特异字段;信封里 `eventHlc`(因果时钟,CAS 单调写键,数值远超 ms)/ `eventUtc`(真实发生 epoch ms)/ `timestamp` 由 `BaseEvent @Builder.Default` **自动盖戳**(`eventHlc` 走 `HybridLogicalClockUtil`,编码同 BifroMQ 内核 HLC)。

| 事件 | Kafka topic(`MqsWebSocket.*`,实际值)| 触发 | 实体 | 特异字段(信封外) |
| --- | --- | --- | --- | --- |
| CONNECT | `websocket.client.connected.topic` | `@OnOpen` 认证通过 | `ClientConnectedEvent` | `version` `userId` `address` `channelId` `keepAliveTimeSeconds` |
| PING | `websocket.ping.req.topic` | `@OnMessage` `{"type":"PING"}` | `PingReqEvent` | `version` `userId` `address` `channelId` |
| PUBLISH | `websocket.distribution.completed.topic` | `@OnMessage` `{topic,payload}` | `DistedEvent` | `topic` `messageId` `qos` `payload`=Base64(原始报文) `payloadHex` `encoding` `originalSize` |
| DISCONNECT | `websocket.client.disconnect.topic` | `@OnClose`(仅认证通过)| `ClientDisconnectEvent` | `version` `address` `channelId` |
| DISPATCH_ERROR | `websocket.distribution.error.topic` | `@OnError` | `DistErrorEvent` | `reqId` `code` `errorMessage` |

示例(CONNECT / PUBLISH):

```json
// CONNECT → websocket.client.connected.topic
{ "tenantId":"1", "clientId":"3752151419551744@1", "event":"CONNECT", "success":"success",
  "eventHlc":114413000000000, "eventUtc":1746521094000, "timestamp":1746521094123,
  "version":"v1", "userId":"dev001", "address":"10.0.0.5:51632", "channelId":"id-xxx", "keepAliveTimeSeconds":60 }

// PUBLISH → websocket.distribution.completed.topic(设备发 {"topic","payload"} → topic/payload)
{ "tenantId":"1", "clientId":"3752151419551744@1", "event":"PUBLISH", "success":"success",
  "eventHlc":114413000000400, "eventUtc":1746521130000,
  "topic":"/v1/devices/3752151419551744/datas", "messageId":123, "qos":0,
  "encoding":"UTF-8", "originalSize":98, "payload":"<Base64(原始报文)>", "payloadHex":"7b22…" }
```

> 常量类 `KafkaConsumerTopicConstant.Mqs.MqsWebSocket`;WS 主题与 MQTT `mqtt.*` 一一对称(`websocket.*`)。**全部 5 类事件的完整 JSON + 字段语义 + 三协议对称入口**见 [事件字段契约 第 5.1 节 / 第 1.1 节](../thinglinks-mqs/thinglinks-mqs-entity/README.md)。下游消费见 [thinglinks-mqs/README.md](../thinglinks-mqs/README.md)。

---

## 7. 部署与配置

### 7.1 启动

| 项 | 值 |
| --- | --- |
| 主类 | [BrokerServerApplication](thinglinks-broker-server/src/main/java/com/mqttsnet/thinglinks/BrokerServerApplication.java) |
| 服务名 | `thinglinks-broker-server`(注册到 Nacos) |
| Context path | `/broker`(网关路由 predicates 必须对齐) |
| HTTP 端口 | `18790`(见 [Nacos 配置](../docs/config/nacos/DEFAULT_GROUP/thinglinks-broker-server.yml)) |
| JDK | 17(容器基镜 `openjdk:17-jdk`,GC = ZGC) |

### 7.2 Nacos 配置加载(顺序见 [application.yml](thinglinks-broker-server/src/main/resources/application.yml))

`common.yml` · `redis.yml` · `database.yml` · `rocketmq.yml` · `kafka.yml` · `dynamictp.yml` · `thinglinks-broker-server.yml`

### 7.3 关键外部依赖

| 依赖 | 配置 key | 用途 |
| --- | --- | --- |
| BifroMQ HTTP API | `thinglinks.feign.bifromq.bifromq-api-server`(默认 `http://127.0.0.1:8091`) | MQTT 适配 第 3 节 |
| Redis | `redis.yml`(Nacos) | ws session 信息(多节点共享在线 / 渠道,TTL 90s) |
| Kafka | `kafka.yml`(Nacos) | ws 上行事件投 mqs |
| RocketMQ | `rocketmq.yml`(Nacos) | ws 下行命令广播 + 心跳跨节点广播 |
| Nacos | `bootstrap.*` / `application.yml` | 配置中心 + 服务发现 |

### 7.4 容器化

镜像构建与运行参数见 [Dockerfile](thinglinks-broker-server/Dockerfile)(JVM Xms=4g / Xmx=8g / ZGC / HeapDumpOnOOM)。健康检查 `GET /actuator/health`。

### 7.5 集群部署要点

⭐ 多副本部署 broker 时必须保证:

- **RocketMQ 必须可用且各 broker 节点接同一套** ── 下行命令 + 心跳都走广播([WebSocket.COMMAND_DOWNLINK](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/BizMqRouteConstant.java) / `HEARTBEAT_SYNC`,BROADCASTING);自建集群开 `autoCreateTopicEnable`,阿里云需控制台预创建 topic / group。无 RocketMQ 时下行命令不可用(消费者 `@ConditionalOnProperty(rocketmq.name-server)` 不注册,producer 抛 `RocketmqTemplate not available`)。
- Redis 必须可用 ── 设备 session 信息存 Redis(在线判断 / 多节点共享渠道信息);不再需要节点间 IP 反向可达(广播下发不依赖任何节点地址)。
- broker 节点不再需要互相 IP 可达,也不存任何节点地址 —— 下行/心跳全走 RocketMQ 广播,与节点 IP 无关。

---

## 8. 排查路径

| 症状 | 先看 |
| --- | --- |
| MQTT 发消息失败 | broker 日志 `Preparing to publish message` / `Response from BifroMQApi` → 再查 BifroMQ HTTP 日志 |
| 设备 `isOnline` 一直返回 fail | [MqttBrokerServiceImpl#isOnline](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/mqtt/service/impl/MqttBrokerServiceImpl.java) 异常分支 → 检查 BifroMQ HTTP 可达性 + 鉴权配置 |
| WS 握手 404(落到 `/error`) | 端点没注册 → 确认 [WebSocketConfig](thinglinks-broker-server/src/main/java/com/mqttsnet/thinglinks/broker/config/WebSocketConfig.java) 在 **broker-server** 且有 `@EnableWebSocket`、Configurator 是**顶层类**;URL 用新格式 `socket/{tenantId}/{clientId}`(topic 在报文里,不在路径) |
| WS `@OnOpen` 鉴权 NPE(`SpringUtils...getBean`) | broker-server 缺 `thinglinks-link-cloud-impl` 依赖 → facade 无 bean,见 第 6.3 节 |
| WS 设备连不上 / 不显示在线 | broker 日志 `连接成功` 后是否 `认证成功`(否→查 link 认证:认证方式=账号、username/password=设备字段);在线状态由 mqs 据 `eventHlc` CAS 写,见 mqs README |
| WS 设备 30~90s 内自动断开 | [WsHeartbeatTimeoutChecker](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/heartbeat/WsHeartbeatTimeoutChecker.java) 日志 `timeout clientId=... idleMs=...` → 确认设备 PING 帧格式 `{"type":"PING"...}` |
| WS 下行命令报"设备不在线: xxx" | Redis 查 `broker:{tenant}:def_ws_session:id:{clientId}` 是否存在 → 不存在则设备真的不在线(或 session 信息已过期 TTL) |
| WS 下行命令发了但设备没收到 | ① 各 broker 节点是否接同一套 RocketMQ;② 该 topic/group 是否已创建([WsCommandDownlinkListener](thinglinks-broker-biz/src/main/java/com/mqttsnet/thinglinks/broker/ws/command/WsCommandDownlinkListener.java) 日志);③ producer 是否报 `RocketmqTemplate not available`(RocketMQ 没接上) |
| `@OnClose` 后 mqs 收不到 disconnect 事件 | 看 Kafka topic `THINGLINKS_WEBSOCKET_CLIENT_DISCONNECTED_TOPIC` 是否有积压 / broker 日志 `发送...事件消息时出错` |

---

## 9. 文档维护索引

- 兄弟服务 README:
  - [thinglinks-mqs/README.md](../thinglinks-mqs/README.md) ── 消息接入(Kafka 消费 + 协议解析 + 桥接旁路)
  - [thinglinks-link/README.md](../thinglinks-link/README.md) ── 设备元数据 / 产品 / 物模型 / 命令编排
  - [thinglinks-rule/README.md](../thinglinks-rule/README.md) ── 规则引擎
- BifroMQ HTTP API 契约:见 [BifroMQApi](thinglinks-broker-facade/thinglinks-broker-cloud-impl/src/main/java/com/mqttsnet/thinglinks/broker/api/BifroMQApi.java) 注解(`/pub` `/retain` `/session` `/kill` `/sub` `/unsub`)
- Nacos broker 段配置:[`docs/config/nacos/DEFAULT_GROUP/thinglinks-broker-server.yml`](../docs/config/nacos/DEFAULT_GROUP/thinglinks-broker-server.yml)
- 改 entity / VO 后需要同步:见 [thinglinks-broker-entity](thinglinks-broker-entity/src/main/java/com/mqttsnet/thinglinks/) ── facade api / boot / cloud 三处实现都依赖它
