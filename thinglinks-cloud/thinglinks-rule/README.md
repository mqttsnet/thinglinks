# thinglinks-rule —— 规则引擎与告警服务

> 物联网平台**规则引擎 + 告警 + 桥接 + 插件**多合一服务。消费 mqs 投递的设备事件,执行业务规则、触发告警、外推第三方系统、运行 Groovy 脚本、管理插件实例。
>
> **本 README 是 rule 服务开发者唯一参考手册** ── 改代码同步改 README。

---

## 🚀 快速导航

| 我想了解 / 改 | 跳到 | 关键入口文件 |
|---|---|---|
| **服务整体定位 / 子模块拆分** | [§1](#1-服务定位--子模块拆分) | [pom.xml](pom.xml) |
| **rule 的 4 大业务维度** | [§2](#2-四大业务维度俯瞰) | — |
| **桥接(出站 / 入站 / 死信)完整链路** | [§3](#3-桥接维度--device-event--ingress--dlq) | [BridgeDeviceEventConsumer](thinglinks-rule-biz-bridge/src/main/java/com/mqttsnet/thinglinks/bridge/consumer/BridgeDeviceEventConsumer.java) |
| **桥接规则匹配 / SinkDispatcher 出站** | [§3.4](#34-规则匹配--出站投递) | [BridgeMatchStrategyChain](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/BridgeMatchStrategyChain.java) / [SinkDispatcher](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/dispatcher/SinkDispatcher.java) |
| **告警(channel / record / 实时告警)** | [§4](#4-告警维度--realtime-alarm--告警通道--告警记录) | [RuleAlarmService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/alarm/RuleAlarmService.java) |
| **规则引擎(Groovy 脚本)** | [§5](#5-规则引擎维度--groovy-脚本) | [RuleGroovyScriptService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/script/RuleGroovyScriptService.java) |
| **联动规则(linkage)** | [§6](#6-联动规则维度--rule-linkage) | [RuleConditionService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/linkage/RuleConditionService.java) |
| **插件实例管理(pf4j)** | [§7](#7-插件实例管理pf4j-动态加载) | [PluginInstanceService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/plugin/PluginInstanceService.java) |
| **执行日志(执行轨迹追溯)** | [§8](#8-执行日志维度--rule-execution-trace) | [BridgeTraceBuilder](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/trace/BridgeTraceBuilder.java) |
| **跨服务联动 / mqs 改 enum 怎么同步** | [§9](#9-跨服务联动--mqs-action-type-对齐) | [§9.2 联动检查清单](#92-mqs-action-type-改动后-rule-端检查清单) |
| **端口 / 部署 / 启动** | [§10](#10-端口--部署--启动) | [thinglinks-rule-server](thinglinks-rule-server/) |
| **常见排查路径** | [§11](#11-常见排查路径) | — |
| **PR Review 红线 / 维护索引** | [§12](#12-文档维护索引--pr-review-checklist) | — |

---

## 1. 服务定位 / 子模块拆分

| 维度 | 说明 |
|---|---|
| 服务职责 | ① 消费 mqs 桥接的设备事件 → 规则匹配 → 出站第三方系统 ② 消费第三方入站消息 → 规则触发 ③ 实时告警 ④ Groovy 脚本规则引擎 ⑤ 联动规则 ⑥ 插件实例 pf4j 动态加载 |
| **上游** | ① RocketMQ `thinglinks-bridge-device-event`(mqs 投递)<br>② RocketMQ `thinglinks-alarm-realtime`(mqs 实时告警)<br>② RocketMQ `thinglinks-bridge-ingress`(第三方系统入站)<br>③ 外部订阅源(Kafka / MQTT / HTTP push,由 SubscriptionSource 配置) |
| **下游** | ① 第三方系统(HTTP / Kafka / MQTT / RocketMQ sink)<br>② DB:`rule_alarm_record` / `rule_execution_log` / `data_bridge_rule` 等<br>③ 告警渠道:钉钉 / 邮件 / 短信(通过 channel)<br>④ Feign 回调:link / mqs / 其他服务 |
| 启动入口 | [thinglinks-rule-server](thinglinks-rule-server/) |

### 1.1 6 个子模块分工

| 子模块 | 职责 | 关键包 |
|---|---|---|
| **thinglinks-rule-entity** | DTO / 枚举 / VO | `entity.bridge.*` / `dto.bridge.*` / `enumeration.linkage.*` |
| **thinglinks-rule-facade** | Feign 接口(供其他服务调 rule,内部 RPC 走 `/inner/ruleOpen`) | `facade.rule.*` / [RuleOpenInnerApi](thinglinks-rule-facade/thinglinks-rule-cloud-impl/src/main/java/com/mqttsnet/thinglinks/rule/api/RuleOpenInnerApi.java) |
| **thinglinks-rule-biz** ⭐ | 核心业务实现 ── 桥接 / 告警 / 规则引擎 / 联动 / 脚本 / 插件 / 执行日志 | `bridge/` / `service/{alarm,script,linkage,plugin,execution}` / `manager/` |
| **thinglinks-rule-biz-bridge** | 桥接 RocketMQ 消费 + 订阅源生命周期管理 | `bridge/consumer/BridgeDeviceEventConsumer` / `bridge/source/SubscriptionSourceLifecycleManager` |
| **thinglinks-rule-controller** | REST 入口 + 桥接规则管理 endpoint + inner 服务间接口 | `bridge/controller/*` / [RuleOpenInnerController](thinglinks-rule-controller/src/main/java/com/mqttsnet/thinglinks/inner/controller/RuleOpenInnerController.java) / [RuleGroovyScriptController](thinglinks-rule-controller/src/main/java/com/mqttsnet/thinglinks/groovy/controller/script/RuleGroovyScriptController.java) |
| **thinglinks-rule-server** | Spring Boot 启动 + 端口约定 + 配置 | 启动 main + 端口 `18786`(规则引擎) + `50000-51000`(插件) |

---

## 2. 四大业务维度俯瞰

```
┌─────────────────────────────────────────────────────────────────┐
│                      thinglinks-rule 服务                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ① 桥接维度(§3)                                               │
│      ├─ 出站:mqs设备事件 → 规则匹配 → 第三方系统                │
│      ├─ 入站:第三方 → 内部转 MQTT/RAW/规则触发                  │
│      └─ 死信:重试超限隔离                                       │
│                                                                  │
│   ② 告警维度(§4)                                               │
│      ├─ 实时告警:mqs alarm-realtime → AlarmRealtimeConsumer    │
│      ├─ 告警通道(channel):钉钉 / 邮件 / 短信下发             │
│      └─ 告警记录(record):写 rule_alarm_record 审计           │
│                                                                  │
│   ③ 规则引擎维度(§5 / §6)                                     │
│      ├─ Groovy 脚本:自定义规则逻辑(产品级 / 规则级)         │
│      └─ 联动规则(linkage):条件 + 动作触发                    │
│                                                                  │
│   ④ 插件实例维度(§7)                                          │
│      └─ pf4j 动态加载第三方算法插件,端口范围 50000-51000        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**它们怎么关联**:桥接是数据通路,告警/规则引擎/联动是处理通路 ── 一条 mqs 投来的设备事件可以**同时**触发桥接出站 + 告警 + 联动规则,互不影响(消费组各自独立)。

---

## 3. 桥接维度 ── device-event / ingress / DLQ

### 3.1 三个核心 RocketMQ topic

| Topic | 方向 | Producer | Consumer | 用途 |
|---|---|---|---|---|
| `thinglinks-bridge-device-event` | 出站(平台 → 第三方)| mqs [MqsBridgeEventProducer](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/bridge/MqsBridgeEventProducer.java) | rule [BridgeDeviceEventConsumer](thinglinks-rule-biz-bridge/src/main/java/com/mqttsnet/thinglinks/bridge/consumer/BridgeDeviceEventConsumer.java) | 设备事件桥接 |
| `thinglinks-bridge-ingress` | 入站(第三方 → 平台)| 外部系统 | rule [BridgeIngressRocketmqConsumerHandler](../thinglinks-mqs/thinglinks-mqs-biz/src/main/java/com/mqttsnet/thinglinks/consumer/rocketmq/bridge/BridgeIngressRocketmqConsumerHandler.java) | 第三方推数据进平台 |
| `thinglinks-bridge-dlq` | 死信 | 重试超限自动进 | 运维监控消费 | 失败隔离 |

常量定义:[BizMqRouteConstant.Bridge](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/mq/BizMqRouteConstant.java)

### 3.2 出站完整链路

```
mqs DeviceProtocolEvent
  │  BridgeRelayStage.toEnvelope
  ▼
BridgeMessageEnvelope (含 actionType / clientId / topic / payload / hlc / utc)
  │  MqsBridgeEventProducer.publishBridgeEvent
  │  dest = thinglinks-bridge-device-event:<actionType>  (tag = actionType)
  ▼
RocketMQ thinglinks-bridge-device-event
  ▼
rule BridgeDeviceEventConsumer
  │  @RocketMQMessageListener(selectorExpression="*")    ⭐ 全收 tag
  │  消费组 = CID_THINGLINKS_BRIDGE_DEVICE_EVENT(CLUSTERING)
  │  ContextUtil 自动恢复 traceId / tenantId
  ▼
BridgeRuleMatcher.match(envelope)
  │  从 data_bridge_rule 加载所有启用规则
  │  按 BridgeMatchConfig 链式匹配(product / actionType / topic / device / payload / time)
  ▼
SinkDispatcher.dispatch(rule, envelope)
  │  规则命中 → 应用 transform 脚本(可选 Groovy)
  │  按 sink 类型路由(HTTP / Kafka / MQTT / RocketMQ)
  │  调外部 SDK 投递
  ▼
第三方系统
```

### 3.3 入站完整链路

```
外部系统(第三方 Kafka / MQTT / HTTP push)
  │  通过 SubscriptionSource 配置接入
  │  SubscriptionSourceLifecycleManager 拉起对应 pub-sub
  ▼
RocketMQ thinglinks-bridge-ingress(或外部源直接进)
  ▼
BridgeIngressRocketmqConsumerHandler
  │  按 tag 分流:MQTT_FORWARD / RAW_INSERT / RULE_TRIGGER
  ├─ MQTT_FORWARD:转 MQTT topic 下发设备
  ├─ RAW_INSERT:直插时序库
  └─ RULE_TRIGGER:触发规则引擎(联动 / Groovy)
```

### 3.4 规则匹配 + 出站投递

#### 规则匹配链(BridgeMatchStrategyChain)

按 `@Order` 排序的策略链,任一 strategy MISS 则规则不命中。所有 strategy 都是**字符串匹配**,**enum 重命名不影响**。

| Strategy | order | 维度 | 配置字段(`BridgeMatchConfig`)|
|---|---|---|---|
| [ProductMatchStrategy](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/strategy/ProductMatchStrategy.java) | — | 产品标识 | `productIdentifications: ["productA"]`(支持 `["*"]` 通配)|
| **[ActionTypeMatchStrategy](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/strategy/ActionTypeMatchStrategy.java)** | — | **action 类型** | `actionTypes: ["PUBLISH","CONNECT","DISPATCH_ERROR"]`(支持 `["*"]` 通配)|
| [TopicMatchStrategy](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/strategy/TopicMatchStrategy.java) | — | MQTT topic 模式 | `topicPatterns: ["device/+/data","cmd/#"]`(MQTT + 平台占位符)|
| [DeviceFilterStrategy](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/strategy/DeviceFilterStrategy.java) | — | 设备维度 | `deviceFilter: {...}` |
| [PayloadFilterStrategy](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/strategy/PayloadFilterStrategy.java) | — | 内容(JSONPath / SpEL / Groovy) | `payloadFilter: {...}` |
| [TimeWindowStrategy](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/strategy/TimeWindowStrategy.java) | — | 时间窗口(cron) | `timeWindow: {cronExpr}` |

**核心实现**:[BridgeMatchConfig](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/matcher/BridgeMatchConfig.java)(序列化自 `data_bridge_rule.match_config_json`)。

#### 出站投递(SinkDispatcher)

| Sink 类型 | 实现 | 配置 |
|---|---|---|
| HTTP | `HttpSinkStrategy` | URL / 方法 / Header |
| Kafka | `KafkaSinkStrategy` | bootstrap-server / topic |
| MQTT | `MqttSinkStrategy` | broker URL / clientId / topic 模板 |
| RocketMQ | `RocketmqSinkStrategy` | nameserver / topic / tag |

`SinkDispatcher` 支持 transform 脚本(Groovy)做数据格式转换。

### 3.5 关键设计要点

| 设计 | 说明 |
|---|---|
| Consumer `selectorExpression="*"` | tag 改名零影响 ── rule 端 fully 全收所有 actionType,内部分流靠规则配置 |
| Consumer 失败处理 | 抛异常 → broker 重投 → 超 `max-reconsume-times` 进 DLQ |
| 跨租户隔离 | `BridgeDeviceEventConsumer extends AbstractTenantAwareRocketmqListener`,自动恢复 ContextUtil 切库 |
| 字符串匹配 | `ActionTypeMatchStrategy` 用 `envelope.getActionType()`(String) 比较 `cfg.getActionTypes()` 列表 ── enum 重命名不影响代码 |

---

## 4. 告警维度 ── realtime alarm / 告警通道 / 告警记录

### 4.1 三层模型

```
mqs AlarmRealtimeRelayStage
  │  POST 阶段,把 PUBLISH / ERROR / DISPATCH_ERROR 投到 thinglinks-alarm-realtime
  ▼
RocketMQ thinglinks-alarm-realtime
  │  tag = productIdentification
  ▼
rule AlarmRealtimeConsumer
  │  按规则匹配触发告警
  ▼
RuleAlarmService.fireAlarm(...)
  ├─ 写 rule_alarm_record 表(record 审计)
  └─ 走 RuleAlarmChannel 下发
     ├─ 钉钉(DingDingChannel)
     ├─ 邮件(EmailChannel)
     ├─ 短信(SmsChannel)
     └─ Webhook(WebhookChannel)
```

### 4.2 三大类核心

| 类 | 职责 | 入口 |
|---|---|---|
| [RuleAlarmService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/alarm/RuleAlarmService.java) | 告警触发主入口 | `fireAlarm(ruleId, payload)` |
| [RuleAlarmChannelService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/alarm/RuleAlarmChannelService.java) | 告警渠道管理(CRUD + 加密配置) | controller REST |
| [RuleAlarmRecordService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/alarm/RuleAlarmRecordService.java) | 告警记录(审计 / 查询) | controller REST |

### 4.3 告警与桥接的区别

| | 告警维度(§4) | 桥接维度(§3) |
|---|---|---|
| 用途 | 内部规则触发后下发告警通道(钉钉/邮件等) | 数据外推到第三方系统(Kafka/HTTP 等)|
| Topic | `thinglinks-alarm-realtime` | `thinglinks-bridge-device-event` |
| Producer | mqs [AlarmRealtimeRelayStage](../thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/stage/relay/AlarmRealtimeRelayStage.java)(只发 3 种 action)| mqs [BridgeRelayStage](../thinglinks-mqs/thinglinks-mqs-biz-bus/src/main/java/com/mqttsnet/thinglinks/mqs/bus/stage/relay/BridgeRelayStage.java)(全部 action)|
| Consumer | `AlarmRealtimeConsumer` | `BridgeDeviceEventConsumer` |
| 下游 | 告警通道(channel)| 第三方 sink |

**为什么独立 topic**:语义不同,告警偏即时性 + 渠道下发,桥接偏数据外推 + 规则匹配。独立 topic 便于运维隔离 + 性能优化。

---

## 5. 规则引擎维度 ── Groovy 脚本

### 5.1 两个粒度

| 粒度 | 实体 | 用途 |
|---|---|---|
| **产品级脚本**([GroovyScriptProductService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/script/GroovyScriptProductService.java)) | `rule_groovy_script_product` 关联表 | 产品级数据预处理(物模型转换前后)|
| **规则级脚本**([RuleGroovyScriptService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/script/RuleGroovyScriptService.java)) | `rule_groovy_script` 主表 | 桥接规则的 transform / payload filter / 自定义动作 |

### 5.2 脚本生命周期

```
[RuleGroovyScriptController](thinglinks-rule-controller/src/main/java/com/mqttsnet/thinglinks/groovy/controller/script/RuleGroovyScriptController.java) / [RuleGroovyScriptService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/script/RuleGroovyScriptService.java)
  │  存 rule_groovy_script 表
  ▼
RuleGroovyScriptChangedEvent / RuleGroovyScriptDeletedEvent(Spring event)
  ▼
RedisScriptLoader 加载到 Redis 缓存
  │  ManualRegisterScriptHelper 注册 Groovy 引擎
  ▼
SinkDispatcher / BridgeMatcher 执行脚本时从缓存拿
```

### 5.3 调用入口

| 场景 | 入口 |
|---|---|
| 桥接 transform 脚本 | `SinkDispatcher.TransformScriptConfig` |
| 桥接 payload filter | `PayloadFilterStrategy` |
| 联动规则动作 | [RuleConditionActionService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/linkage/RuleConditionActionService.java) |
| 物模型预处理 | `GroovyScriptProductService` |

---

## 6. 联动规则维度 ── rule linkage

### 6.1 数据模型

| 表 | 实体 | 职责 |
|---|---|---|
| `rule_instance` | [RuleInstanceService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/linkage/RuleInstanceService.java) | 规则实例(启用 / 禁用 / 描述)|
| `rule_condition` | [RuleConditionService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/linkage/RuleConditionService.java) | 条件(设备属性 / topic / 触发 action)|
| `rule_condition_action` | `RuleConditionActionService` | 满足条件后的动作(下发命令 / 调 Feign / 跑脚本) |
| `rule_execution_log` | [RuleExecutionLogService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/linkage/RuleExecutionLogService.java) | 规则执行日志 |
| `rule_action_execution_log` | [RuleActionExecutionLogService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/linkage/RuleActionExecutionLogService.java) | 动作执行日志 |

### 6.2 触发时机

- 设备事件桥接进 rule → 匹配联动规则 → 触发条件 → 执行动作
- 数据字典 `RULE_CONDITION_DEVICE_ACTION_TIRGGER_TYPE` 决定 UI 可选触发动作(12 项:11 个 MQS 动作 + OFFLINE 业务分组,详见 mqs README §5.4)

### 6.3 ExecutionLog 异步落库

```
RuleConditionActionService.execute()
  │  ExecutionLogEventPublisher.publishActionExecutionLog(...)
  ▼
ActionExecutionLogEvent(Spring event,异步)
  ▼
ExecutionLogEventListener → DB 落库
```

---

## 7. 插件实例管理(pf4j 动态加载)

### 7.1 设计

| 概念 | 实体 | 端口 |
|---|---|---|
| [PluginInfo](thinglinks-rule-entity/src/main/java/com/mqttsnet/thinglinks/entity/plugin/PluginInfo.java) | 插件 jar 元信息(id / version / class) | — |
| [PluginInstance](thinglinks-rule-entity/src/main/java/com/mqttsnet/thinglinks/entity/plugin/PluginInstance.java) | 运行中的插件实例 | `50000-51000`(TCP/UDP)|
| [PluginInstanceMapping](thinglinks-rule-entity/src/main/java/com/mqttsnet/thinglinks/entity/plugin/PluginInstanceMapping.java) | 实例 ↔ 设备 / 产品映射 | — |
| `PluginClient` | 插件 SDK 客户端调用 | — |

### 7.2 加载流程

```
启动 / 手动触发
  │  PluginScanService 扫描 /home/www/mqttsnet/plugins/
  ▼
PluginInfoService 注册 jar 元信息到 DB
  ▼
PluginInstanceService 启动实例
  │  pf4j PluginManager 加载 jar
  │  分配端口(50000-51000)
  │  实例进 PluginInstance 表
  ▼
PluginClientService 提供 SDK 调用入口
```

### 7.3 文件挂载

宿主机 `/data01/mqttsnet/thinglinks` → 容器 `/home/www/mqttsnet`(详见 §10 部署)。

---

## 8. 执行日志维度 ── rule execution trace

### 8.1 BridgeTraceBuilder

| 类 | 职责 |
|---|---|
| [BridgeTraceBuilder](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/trace/BridgeTraceBuilder.java) | 构造单条桥接执行轨迹(envelope → 规则匹配 → sink 投递的全过程) |
| [BridgeStepType](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/trace/BridgeStepType.java) | 步骤类型枚举(MATCH / TRANSFORM / DISPATCH / FAIL 等)|
| [BridgeTraceCompletedEvent](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/trace/BridgeTraceCompletedEvent.java) | 轨迹完成事件,异步落库 |
| [BridgeTraceEventListener](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/listener/BridgeTraceEventListener.java) | 监听事件并写 `rule_bridge_execution_trace` 表 |

### 8.2 trace 用途

- 排查"某条设备事件为什么没出站到第三方" ── 查 trace 看哪一步 MISS
- 规则配置回归测试 ── 改规则后观察 trace 路径变化
- 性能分析 ── 步骤耗时

### 8.3 trace 表关键字段

| 字段 | 说明 |
|---|---|
| `traceId` | 跨链路追踪(贯穿 mqs → rule)|
| `tenantId` | 多租户 |
| `rule_id` | 命中 / 匹配尝试的规则 ID |
| `step_type` | MATCH / TRANSFORM / DISPATCH / FAIL |
| `action_type` | 设备事件 action(PUBLISH / CONNECT 等)|
| `topic` | 设备事件 topic |
| `match_result` | HIT / MISS + reason |
| `cost_ms` | 该步骤耗时 |

---

## 9. 跨服务联动 / mqs Action Type 对齐

> 本节是**最重要的**:mqs 改了 [DeviceActionTypeEnum](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/enums/DeviceActionTypeEnum.java) / topic / envelope 字段后,rule 端要同步做的事。

### 9.1 rule 端依赖 mqs 的关键约定

| mqs 侧产出 | rule 侧消费方 | 是否硬编码依赖 |
|---|---|---|
| [BridgeMessageEnvelope](../thinglinks-public/thinglinks-common/src/main/java/com/mqttsnet/thinglinks/common/event/bridge/BridgeMessageEnvelope.java)`.actionType`(String)| `ActionTypeMatchStrategy`(字符串匹配)| ❌ 不硬编码 enum,改名零影响 |
| `BridgeMessageEnvelope.topic` | `TopicMatchStrategy` | ❌ |
| `BridgeMessageEnvelope.payload` / `rawMessage` | `PayloadFilterStrategy` + Groovy 脚本 | ❌ |
| `BridgeMessageEnvelope.eventHlc` / `eventUtc` | `BridgeTraceBuilder` 时序记录 | ❌ |
| RocketMQ topic 名 `thinglinks-bridge-device-event` | `BridgeDeviceEventConsumer` `@RocketMQMessageListener.topic` | ✅ 改 topic 名 rule 必须同步改 |
| RocketMQ tag 名(actionType) | `selectorExpression="*"` | ❌ 全收 tag,改名零影响 |

### 9.2 mqs Action Type 改动后 rule 端检查清单

mqs 改了 `DeviceActionTypeEnum`(如本次 `PUBLISH_ERROR` → `DISPATCH_ERROR`):

| 检查项 | 操作 |
|---|---|
| ✅ rule java 代码 | grep `DISTRIBUTION_ERROR` / `PUBLISH_ERROR` / 旧 enum 值,确认无硬编码 |
| ✅ [BridgeMatchStrategyChain](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/bridge/match/BridgeMatchStrategyChain.java) | 字符串匹配,无需改 |
| ⚠️ **历史规则配置 JSON** | DBA 跑迁移 SQL(下面)|
| ✅ 字典 SQL | mqs 端 `def_dict_action_type_update.sql` 已同步加 `RULE_CONDITION_DEVICE_ACTION_TIRGGER_TYPE` 子集 |
| ✅ Echo 字典 | 用 `@Echo` 注解的字段会自动从 dict 取最新值 |

#### 历史规则 JSON 迁移

```sql
-- 如果运营配过含旧 action 名的规则,DBA 执行:
UPDATE data_bridge_rule
SET match_config_json = REPLACE(REPLACE(match_config_json, 'DISTRIBUTION_ERROR', 'DISPATCH_ERROR'), 'PUBLISH_ERROR', 'DISPATCH_ERROR')
WHERE match_config_json LIKE '%DISTRIBUTION_ERROR%' OR match_config_json LIKE '%PUBLISH_ERROR%';

-- 清理已删 enum 值(WRITE/CLUSTER/PUBLISH_ACK 等)── 这些值不再有效:
-- 不强制删,但配过这些值的规则永远不会命中(默默失效,不报错)
```

### 9.3 mqs 端的反向依赖

| rule 侧产出 | mqs 侧消费 | 备注 |
|---|---|---|
| `INBOUND` action type(rule [SubscriptionSourceLifecycleManager](thinglinks-rule-biz-bridge/src/main/java/com/mqttsnet/thinglinks/bridge/source/SubscriptionSourceLifecycleManager.java) 发桥接)| mqs `DeviceActionTypeEnum.INBOUND` | enum 兜底 fromValue 找不到时返 UNKNOWN |
| 第三方入站消息转发 MQTT | mqs broker 主动下发 | 走 RocketMQ ingress 通路 |

---

## 10. 端口 / 部署 / 启动

| 项 | 值 |
|---|---|
| 规则引擎端口 | `18786`(TCP)|
| 插件实例端口范围 | `50000-51000`(TCP/UDP)|
| 网络模式 | `network_mode: "host"` |
| 健康检查 | `GET /actuator/health` |
| 启动命令 | `docker-compose up -d` |

### 环境变量

| 变量 | 用途 |
|---|---|
| `TZ` | 时区(默认 `Asia/Shanghai`) |
| `SPRING_PROFILES_ACTIVE` | Spring profile(默认 `test`) |
| `NACOS_IP` / `NACOS_PORT` | Nacos 服务发现 + 配置 |
| `NACOS_NAMESPACE` | Nacos 命名空间 |
| `NACOS_USERNAME` / `NACOS_PASSWORD` | Nacos 鉴权 |
| `NACOS_DISCOVERY_IP` | 服务注册 IP |

### 卷映射

| 宿主机 | 容器内 | 用途 |
|---|---|---|
| `/data01/mqttsnet/thinglinks` | `/home/www/mqttsnet` | 插件 jar / 日志 / 临时文件 |
| `/etc/localtime` | `/etc/localtime` | 时区一致 |

---

## 11. 常见排查路径

| 现象 | 第一手段 |
|---|---|
| mqs 发了桥接消息但 rule 没收到 | 看 `BridgeDeviceEventConsumer` 启动日志确认订阅了 `thinglinks-bridge-device-event` topic + `CID_THINGLINKS_BRIDGE_DEVICE_EVENT` 消费组;再看 RocketMQ broker 管理台消费 offset |
| rule 收到消息但规则没匹配 | 看 `rule_bridge_execution_trace` 表,按 traceId 查每个 step_type=MATCH 的 match_result(MISS 会带原因如 `'DISPATCH_ERROR' not in [PUBLISH, CONNECT]`)|
| 规则命中但 sink 投递失败 | `SinkDispatcher` 异常 log + `rule_bridge_execution_trace` step_type=DISPATCH 的 fail 记录 |
| 历史规则配置 `actionTypes:["DISTRIBUTION_ERROR"]` 不命中 | mqs 已改名 DISPATCH_ERROR,DBA 跑 §9.2 迁移 SQL |
| 告警没下发 | `RuleAlarmRecordService` 看 record 表,确认 fireAlarm 调用了;再看 channel 配置是否正确(钉钉 token / 邮件 SMTP 等)|
| Groovy 脚本不生效 | 检查 `rule_groovy_script` 表 state 字段,看 Redis 缓存是否加载([RedisScriptLoader](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/config/groovy/loader/RedisScriptLoader.java))|
| 插件实例起不来 | 看 [PluginScanService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/plugin/PluginScanService.java) 扫描日志 / [PluginInstanceService](thinglinks-rule-biz/src/main/java/com/mqttsnet/thinglinks/service/plugin/PluginInstanceService.java) 启动日志;确认 50000-51000 端口未占 |
| 多租户切库到错租户 | `BridgeDeviceEventConsumer extends AbstractTenantAwareRocketmqListener` 自动从消息 header 恢复 tenantId,看 ContextUtil 是否被外层覆盖 |

---

## 12. 文档维护索引 + PR Review checklist

> 改下表中任一文件/枚举,**对应小节必须同步更新本 README**。

| 改了什么 | 同步小节 |
|---|---|
| `BridgeMatchConfig` 字段增减 | [§3.4 规则匹配链](#34-规则匹配--出站投递) |
| 新增 sink 类型 | [§3.4 出站投递表](#34-规则匹配--出站投递) |
| `RuleAlarmChannel` 实现新增 | [§4.1 告警链路](#41-三层模型) |
| `BridgeTraceBuilder` 步骤 | [§8 执行日志](#8-执行日志维度--rule-execution-trace) |
| `SubscriptionSource` 协议接入 | [§3.3 入站链路](#33-入站完整链路) |
| RocketMQ topic 名变更 | [§3.1 三个核心 topic](#31-三个核心-rocketmq-topic) + 跨仓库 mqs README §6.3 |
| Groovy 脚本调用点新增 | [§5.3 调用入口](#53-调用入口) |
| 联动规则数据模型变更 | [§6.1 数据模型](#61-数据模型) |
| 插件 SPI 变更 | [§7 插件实例管理](#7-插件实例管理pf4j-动态加载) |

### PR Review 红线

- [ ] 改了 `BridgeMessageEnvelope` 字段使用?同步 §3.4 + §9.1
- [ ] 改了规则匹配 strategy?同步 §3.4
- [ ] 改了 RocketMQ topic / consumer group?同步 §3.1 + 部署文档
- [ ] mqs `DeviceActionTypeEnum` 改了?对照 §9.2 检查清单
- [ ] 改了告警通道?同步 §4.1
- [ ] 改了 Groovy 脚本调用点?同步 §5.3

### 与 mqs README 的关系

mqs README 是设备事件 producer 视角,本 README 是 consumer 视角。**两个 README 必须同步**:
- mqs `BridgeMessageEnvelope` 字段 ↔ rule §3.4 字段解析
- mqs `DeviceActionTypeEnum` ↔ rule §9.2 配置迁移
- mqs `BizMqRouteConstant.Bridge.*` topic 名 ↔ rule §3.1

---

## 13. 外部相关文档

- [thinglinks-mqs README](../thinglinks-mqs/README.md) ── 设备事件 producer / 协议总线 / 桥接出站投递
- [bifromq-plugin](../../bifromq-plugin/) ── BifroMQ broker 事件源头
- [thinglinks-link README](../thinglinks-link/README.md)(如有) ── 设备元数据 / 物模型
