# BifroMQ Event Provider Plugin

这是一个基于 BifroMQ 框架的事件提供器插件，用于收集 BifroMQ 执行过程中发生的各种事件。该插件允许开发者灵活处理事件并实现特定的事件过滤逻辑。

## 目录结构

```plaintext
bifromq-event-provider-plugin/
├── event-provider/ <-- event provider module as a reference for other bifromq plugin, you can remove it if not needed
│   └── src/
│       └── main/
│           └── java/
│               └── com.yourcompany.newproject/
│                   └── BifromqEventCollectorPluginEventProvider.java
├── plugin-build/  <-- plugin-build module to build the plugin zip file
│   ├── assembly/
│   │   └── assembly-zip.xml
│   ├── conf/      <-- folder to contain plugin configuration files
│   │   ├── config.yaml <-- plugin configuration file
│   │   └── logback.xml <-- logback configuration file for the plugin
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── com.yourcompany.newproject/
│   │               └── YourPluginClassName.java <-- Your plugin main class
│   └── target/
│       └── pom.xml
├── plugin-context/  <-- plugin-context module to define the plugin context
│   └── src/
│       └── main/
│           └── java/
│               └── com.yourcompany.newproject/
│                   └── YourPluginContextClassName.java
└── pom.xml
```

## 功能特性

- **事件收集**：收集 BifroMQ 执行过程中发生的各种事件。
- **事件过滤**：支持通过自定义过滤逻辑对事件进行筛选。
- **事件处理**：提供灵活的事件处理机制，允许开发者自定义事件处理逻辑。
- **配置驱动**：支持通过 `config.yaml` 进行动态配置。
- **日志管理**：集成 Logback 日志框架，通过 `logback.xml` 进行日志级别和格式的配置。

## ⚠️ 心跳(PING_REQ)前置条件(必读)

心跳是高频事件,BifroMQ 内核**仅在 `DebugModeEnabled=true` 时才采集并上报 `PING_REQ`**。`DebugModeEnabled` 由 setting-provider 插件提供,因此要收到 `PING_REQ`,**必须同时满足**:

1. setting-provider 配置 `debugModeEnabled: true`(默认已是)
2. BifroMQ 启动加 `-Dsetting_provide_init_value=true`(否则启动期内核用初值 `false`,插件配置不生效)

缺任一条,本插件都收不到 `PING_REQ`,下游 mqs 也写不了设备 `last_heartbeat_time`。配置方法见 [setting-provider 插件 README「让插件配置生效」](../bifromq-setting-provider-plugin/README.md)。

> 其余事件(`CLIENT_CONNECTED` / `MQTT_SESSION_START` / `MQTT_SESSION_STOP` / `IDLE` / 各 disconnect 子类型等)**不受 DebugMode 限制,始终上报**。所以"只有 ping 收不到、其它都正常"基本就是这个前置条件没满足。

```bash
# 验证:设备连上并经过一个 keepalive 周期后应出现 PING_REQ
grep "Received event type=PING_REQ" logs/bifromq-event-collector-plugin/info.log
```

## 快速开始

### 1. 配置文件设置

在 plugin-context 模块`conf/config.yaml` 中定义事件提供器配置，具体配置项如下：

```yaml
eventProviderConfig:
  # The kafka topic to which the events are published
  kafkaBootstrapServer: "127.0.0.1:9200"
```

### 2. 构建和打包

通过 Maven 构建并打包插件：

```bash
mvn clean package
```

构建成功后，插件包将生成在 `target/` 目录中，例如 `target/bifromq-event-provider-plugin-1.0.4.zip`。

### 3. 部署和加载插件

将插件包上传到 BifroMQ 插件目录（假设目录为 `/home/bifromq/plugins`）：

然后启动 BifroMQ 服务，插件将自动加载。

在 BifroMQ 中运行此插件时，并不需要通过配置文件指定 Event Provider 的完全限定类名（FQN）。请注意，BifroMQ 允许运行多个 Event
Provider 实例。

## 使用说明

### 代码示例

#### 初始化事件上下文

在插件初始化时，通过 `BifromqEventProviderContext` 加载配置：

```java
public class YourPluginClassNameEventProvider {
    private final int collectionInterval;

    public YourPluginClassNameEventProvider(BifromqEventProviderContext context) {
        this.collectionInterval = context.getPluginConfig().getEventProviderConfig().getCollectionInterval();
        // 初始化其他配置项
    }
```

#### 收集事件

在插件中实现事件收集逻辑：

```java
public void collectEvents(){
        // 实现事件收集逻辑
        }
```

#### 处理事件

在插件中实现事件处理逻辑：

```java
public void processEvents(){
        // 实现事件处理逻辑
        }
```

## 常见问题

### 如何在不重启 BifroMQ 的情况下更新配置？

可以直接修改 BifroMQ 配置文件 `standalone.yml` 中的配置项，并重启插件实现动态更新。
程序会自动覆盖模块 `conf/config.yaml` 中定义的配置（standalone.yml 优先级高于 config.yaml）

### 如何启用详细日志？

修改 `logback.xml` 中的 `<level>` 标签，将日志级别设置为 `DEBUG`，然后重启插件。

## Processor 包结构(v1.1)

按 BifroMQ 源码包镜像组织,扩展新事件时直接对照 BifroMQ 对应包路径建文件即可,无需修改样板代码:

```
processor/
├── EventProcessor.java                 接口
├── AbstractEventProcessor.java         通用模板(enrich + send)
├── AbstractClientEventProcessor.java   ClientEvent 家族公共字段抽取(Optional 防御 + 类型安全 cast)
├── EventProcessorFactory.java          单点注册 + Optional getter
│
├── mqttbroker/
│   ├── PingReqEventProcessor.java
│   ├── clientconnected/  CLIENT_CONNECTED
│   ├── clientdisconnect/ 20 个 disconnect 子类型(IDLE 含 KeepAlive 超时等)
│   ├── channelclosed/    仅 NOT_AUTHORIZED_CLIENT(其余 P2)
│   ├── subhandling/      SUB_ACKED / UNSUB_ACKED
│   └── disthandling/     DISTED / DIST_ERROR
└── session/              MQTT_SESSION_START / STOP
```

新增一个 EventType 的步骤:

1. 在对应 BifroMQ 包路径下的 processor 子目录新建 `XxxEventProcessor extends AbstractClientEventProcessor<Xxx>`
2. 重写 `eventClass()` 返 Class 字面量;有特异字段则 override `enrichSubtype()`
3. `EventProcessorFactory` 构造函数注册一行
4. `BifromqEventCollectorPluginEventProvider.TOPIC_MAP` 加一行 topic 映射

## 与 thinglinks-mqs-server 联动核对(v1.1 收尾)

### 已对齐(全自动生效,mqs 零改动)
- 18 个新增 disconnect 子类型(IDLE / BAD_PACKET / PROTOCOL_VIOLATION / ...)全部映射到 `mqtt.server.disconnect.topic`
- mqs 端 `MqttLifecycleEdgeAdapter` + `MqttKafkaInboundConsumer` 已监听该 topic
- mqs `DeviceActionTypeEnum.CLOSE.affectsConnectionStatus()=true` → 触发 `device.connect_status` 切 OFFLINE
- plugin 端 `EventTypeEnum.businessSystemEventType` 已显式声明 "CLOSE" / "DISCONNECT" / "KICKED"(不再依赖 mqs fallback)
- HLC + UTC 透传(`event.hlc()` / `event.utc()` 在 `report()` 入口同步抓取)

### 已启用 audit 类 topic(mqs 端 log 消费)

以下 3 个 topic 由 mqs `thinglinks-mqs-biz-bus/.../inbound/kafka/audit/` 下 3 个独立 consumer 接管:

| topic | plugin processor | mqs consumer | log 前缀 |
|---|---|---|---|
| `mqtt.client.unauthorized` | `channelclosed/NotAuthorizedClientEventProcessor` | `NotAuthorizedClientAuditConsumer` | `[audit.unauthorized]` |
| `mqtt.session.start` | `session/MqttSessionStartEventProcessor` | `MqttSessionStartAuditConsumer` | `[audit.session.start]` |
| `mqtt.session.stop` | `session/MqttSessionStopEventProcessor` | `MqttSessionStopAuditConsumer` | `[audit.session.stop]` |

audit consumer 仅 log INFO 消费(打 `partition / offset / key / rawJson` 一行),**不入 DB / 不触发业务流程**。运维排查 `grep '[audit.unauthorized]'` / `grep '[audit.session.start]'` 即可。各 consumer 独立 group(`CID_THINGLINKS_AUDIT_*`),失败仅 warn 不抛,finally ack(audit best-effort,不阻塞 offset)。

## 3.3.5 → 4.0 字段差异完整核对表

本表针对 plugin 当前注册的 29 个 EventType 对应的事件类做字段级核对(基于 jar 反编译,javap 验证)。

### 字段完全一致(25 个,升级时只需改 import 包名)

| Class | 字段(3.3.5 / 4.0 一致) |
|---|---|
| `ClientConnected` | serverId, userSessionId, keepAliveTimeSeconds, cleanSession, sessionPresent, lastWill |
| `ByClient` | withoutDisconnect |
| `ByServer` | (无特异字段) |
| `Idle` | keepAliveTimeSeconds |
| `BadPacket` | cause (Throwable) |
| `ProtocolViolation` | statement |
| `ServerBusy` | reason |
| `ResourceThrottled` | reason |
| `ClientChannelError` | cause (Throwable) |
| `ReAuthFailed` | (无特异字段) |
| `NoPubPermission` | topic, qos, retain |
| `ExceedReceivingLimit` | limit |
| `ExceedPubRate` | limit |
| `InvalidTopic` | topic |
| `InvalidTopicFilter` | topicFilter |
| `MalformedTopic` | topic |
| `MalformedTopicFilter` | topicFilter |
| `TooLargeSubscription` | max, actual |
| `TooLargeUnsubscription` | max, actual |
| `PingReq` | pong |
| `SubAcked` | messageId, topicFilter, granted |
| `UnsubAcked` | messageId, topicFilter |
| `Disted` | reqId, messages, fanout |
| `DistError` | reqId, messages, code |
| `NotAuthorizedClient` | tenantId, userId, clientId |
| `MQTTSessionStart` / `MQTTSessionStop` | sessionId |

### 字段 4.0 新增(2 个,plugin 已 TODO 标记 enrichSubtype 待补)

| Class | 4.0 新增字段 | plugin 标记位置 |
|---|---|---|
| `Kicked` | `ClientInfo kicker`(踢线发起方信息) | `KickedEventProcessor` 类顶部 TODO |
| `InboxTransientError` | `String reason`(暂时错误根因) | `InboxTransientErrorEventProcessor` 类顶部 TODO |

### 字段 4.0 删除

无。所有 plugin 当前用到的字段在 4.0 全部保留。

### EventType 枚举差异

| 变化 | 项 | 影响 |
|---|---|---|
| 4.0 新增 | `SERVER_REDIRECTED`(对应 `Redirect` class)+ `RETAIN_MSG_MATCHED` + `QOS1_PUSH_ERROR` / `QOS2_PUSH_ERROR` / `SUB_STALLED` / `PERSISTENT_FANOUT_THROTTLED` / `PERSISTENT_FANOUT_BYTES_THROTTLED` / `GROUP_FANOUT_THROTTLED` | EventTypeEnum + Factory + TOPIC_MAP 按需补 |
| 4.0 删除 | `DELIVER_NO_INBOX` / `SUBSCRIBED` / `SUBSCRIBE_ERROR` / `UNSUBSCRIBED` / `UNSUBSCRIBED_ERROR` | EventTypeEnum 删除对应行(plugin 未注册这些,不影响) |

### 包路径

| 3.3.5 | 4.0 |
|---|---|
| `com.baidu.bifromq.plugin.eventcollector` | `org.apache.bifromq.plugin.eventcollector` |
| `com.baidu.bifromq.type.ClientInfo` | `org.apache.bifromq.type.ClientInfo` |
| `com.baidu.bifromq.type.QoS` | `org.apache.bifromq.type.QoS` |

升级时全局 `sed -i 's|com\.baidu\.bifromq|org.apache.bifromq|g'` + 验证编译。

## BifroMQ 4.0 升级说明

本插件的 BifroMQ 兼容版本见根目录 [`.thinglinks-product.env`](../.thinglinks-product.env) 中的 `THINGLINKS_BIFROMQ_VERSION`。
迁移到 `4.0.0-incubating` 时需要:

1. **包名替换**:全局 `com.baidu.bifromq` → `org.apache.bifromq`
2. **新增 EventType 补 processor**:
   - `SERVER_REDIRECTED`(对应 `Redirect` class,4.0 新增)── 新建 `ServerRedirectedEventProcessor`,topic 进 `mqtt.server.disconnect.topic`,Factory + TOPIC_MAP 各加一行(代码内已加 `TODO 4.0 升级时补` 标记)
   - 可选:`RETAIN_MSG_MATCHED` / `QOS1_PUSH_ERROR` / `QOS2_PUSH_ERROR` / `SUB_STALLED` / `PERSISTENT_FANOUT_THROTTLED` / `PERSISTENT_FANOUT_BYTES_THROTTLED` / `GROUP_FANOUT_THROTTLED` ── 按业务需要选择性接入
3. **删除已废弃**:4.0 移除了 `DELIVER_NO_INBOX / SUBSCRIBED / SUBSCRIBE_ERROR / UNSUBSCRIBED / UNSUBSCRIBED_ERROR`,本插件未注册这些,无需处理

## 贡献

欢迎提出 issue 和 pull request 以改进插件功能。如有疑问，请联系项目维护社区 MQTTSNET。

--- 
