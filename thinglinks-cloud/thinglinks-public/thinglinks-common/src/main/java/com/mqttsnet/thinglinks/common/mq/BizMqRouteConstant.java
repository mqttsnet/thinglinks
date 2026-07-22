package com.mqttsnet.thinglinks.common.mq;

/**
 * 业务级 RocketMQ 路由常量(Topic / Tag / ConsumerGroup 三套字面量集中管理)。
 *
 * <p><b>原则</b>:所有跨服务异步通信的 topic / tag / consumerGroup 字面量必须在本文件声明,
 * <b>业务代码禁止硬编码</b>。生产者 / 消费者通过本文件协同,确保命名变更只改一处、信息差为零。
 *
 * <h2>命名规约</h2>
 * <ul>
 *   <li><b>Topic</b>:项目前缀 {@code ${THINGLINKS_MQ_NAMESPACE}-} + 业务域 + 用途,全小写连字符。例:
 *       {@code ${THINGLINKS_MQ_NAMESPACE}-bridge-device-event}</li>
 *   <li><b>Tag</b>:业务事件类型(eventType / actionType / sourceType 等)。例:{@code PUBLISH} / {@code instance-up}</li>
 *   <li><b>ConsumerGroup</b>:大写 {@code CID_${THINGLINKS_MQ_NAMESPACE_UPPER}_<DOMAIN>_<PURPOSE>};
 *       BROADCASTING 模式额外附加节点 IP / 主机名后缀让每节点独立 group(避免 offset 抢占)</li>
 *   <li><b>Destination 拼接</b>:业务侧 send 时用 {@code topic + ":" + tag} 拼装传给
 *       {@code RocketMQTemplate}(spring rocketmq 的 destination 语法)</li>
 * </ul>
 *
 * <h2>全局链路一览</h2>
 *
 * <pre>
 * ┌─────────────────────────────── 桥接出站(设备 → 第三方) ───────────────────────────────┐
 * │                                                                                         │
 * │ 设备 ─MQTT/WS/TCP─→ thinglinks-mqs                                                        │
 * │                       │                                                                  │
 * │                       │ 主链路:解析 + 持久化 DeviceAction(不变)                          │
 * │                       │ 旁路:MqsBridgeEventProducer.publishBridgeEvent (best-effort)   │
 * │                       ↓                                                                  │
 * │           [ Topic: ${THINGLINKS_MQ_NAMESPACE}-bridge-device-event ]   ← Bridge#DEVICE_EVENT             │
 * │           [ Tag:   actionType (PUBLISH/CONNECT/...) ]                                   │
 * │           [ Mode:  CLUSTERING / Group: BRIDGE_DEVICE_EVENT ]                            │
 * │                       │                                                                  │
 * │                       ↓                                                                  │
 * │ thinglinks-rule.BridgeDeviceEventConsumer                                                │
 * │     → BridgeRuleMatcher.matchOutbound (Caffeine 缓存命中)                                 │
 * │     → SinkDispatcher.dispatch (限流 + 转换 + 投递 + 重试 + 死信 + Trace 日志)              │
 * │     → 第三方系统(Kafka / Redis / RocketMQ / RabbitMQ / MySQL / HTTP / WebHook / MQTT)       │
 * └─────────────────────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────── 桥接入站(第三方 → 平台) ───────────────────────────────┐
 * │                                                                                         │
 * │ 第三方系统(Kafka topic / MQTT topic / HTTP POST)                                        │
 * │           │                                                                              │
 * │           │ rule.SubscriptionSourceLifecycleManager (订阅 / HTTP endpoint)                │
 * │           │   → 字段映射 → BridgeMessageEnvelope                                          │
 * │           ↓                                                                              │
 * │           [ Topic: ${THINGLINKS_MQ_NAMESPACE}-bridge-ingress ]   ← Bridge#INGRESS                       │
 * │           [ Tag:   targetHandler (MQTT_FORWARD/RAW_INSERT/RULE_TRIGGER) ]               │
 * │           [ Mode:  CLUSTERING / Group: BRIDGE_INGRESS ]                                  │
 * │                       │                                                                  │
 * │                       ↓                                                                  │
 * │ thinglinks-mqs.BridgeIngressRocketmqConsumerHandler                                      │
 * │   ├─ MQTT_FORWARD  → 伪装成设备 publish,走 MqttPublishEventListener 主链路                 │
 * │   ├─ RAW_INSERT    → 调 link-api Feign 直写 device_action 表                              │
 * │   └─ RULE_TRIGGER  → 调 rule-api Feign 触发场景联动                                        │
 * └─────────────────────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────── WebSocket 集群心跳同步 ───────────────────────────────┐
 * │                                                                                       │
 * │ 设备 PING (ws 心跳)                                                                    │
 * │   → broker.WsHeartbeatTracker.update                                                   │
 * │   → 本地 lastActiveTime + Redis TTL + Owner 续命                                         │
 * │   → [ Topic: ${THINGLINKS_MQ_NAMESPACE}-ws-heartbeat-sync ]   ← WebSocket#HEARTBEAT_SYNC                │
 * │   → [ Mode:  BROADCASTING / Group: WS_HEARTBEAT_SYNC_PREFIX + ${HOSTNAME或IP} ]         │
 * │   → 各 broker 节点 WsHeartbeatSyncListener 收到 → 持有该 session 的节点更新本地存活(重连漂移兜底) │
 * └───────────────────────────────────────────────────────────────────────────────────────┘
 *
 * ┌─────────────────────────────── WebSocket 下行命令广播 ───────────────────────────────┐
 * │                                                                                       │
 * │ 业务侧下发命令 → 任一 broker 节点                                                       │
 * │   → WebSocketBrokerServiceImpl.publishMessage(vo)                                      │
 * │   → 编码 ws 子协议报文 + 在线校验(查 Redis session 信息)                                │
 * │   → [ Topic: ${THINGLINKS_MQ_NAMESPACE}-ws-command-downlink ]   ← WebSocket#COMMAND_DOWNLINK           │
 * │   → [ Mode:  BROADCASTING / Group: WS_COMMAND_DOWNLINK_PREFIX + ${spring.application.name} ] │
 * │   → 每个 broker 节点 WsCommandDownlinkListener 收到 → 查本地 Holder                       │
 * │       ├─ 命中(持有该设备 TCP 的节点)→ 推 socket                                         │
 * │       └─ 未命中 → 静默忽略                                                              │
 * └───────────────────────────────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>链路速查表</h2>
 * <table border="1">
 *   <caption>RocketMQ 业务路由速查</caption>
 *   <tr>
 *     <th>常量</th><th>Topic</th><th>Mode</th><th>Producer</th><th>Consumer</th><th>Tag 规约</th>
 *   </tr>
 *   <tr>
 *     <td>{@link Bridge#DEVICE_EVENT}</td>
 *     <td>${THINGLINKS_MQ_NAMESPACE}-bridge-device-event</td>
 *     <td>CLUSTERING</td>
 *     <td>mqs · {@code MqsBridgeEventProducer}</td>
 *     <td>rule · {@code BridgeDeviceEventConsumer}</td>
 *     <td>actionType(PUBLISH / CONNECT / CLOSE / SUBSCRIBE / ...)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link Bridge#INGRESS}</td>
 *     <td>${THINGLINKS_MQ_NAMESPACE}-bridge-ingress</td>
 *     <td>CLUSTERING</td>
 *     <td>rule · {@code SubscriptionSourceLifecycleManager} / {@code BridgeIngressOpenAnyUserController}</td>
 *     <td>mqs · {@code BridgeIngressRocketmqConsumerHandler}</td>
 *     <td>targetHandler(MQTT_FORWARD / RAW_INSERT / RULE_TRIGGER)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link Bridge#DEAD_LETTER}</td>
 *     <td>${THINGLINKS_MQ_NAMESPACE}-bridge-dlq</td>
 *     <td>CLUSTERING</td>
 *     <td>rule · {@code SinkDispatcher.sendToDeadLetter} <i>(预留)</i></td>
 *     <td>监控告警 <i>(待接入)</i></td>
 *     <td>ruleCode</td>
 *   </tr>
 *   <tr>
 *     <td>{@link WebSocket#HEARTBEAT_SYNC}</td>
 *     <td>${THINGLINKS_MQ_NAMESPACE}-ws-heartbeat-sync</td>
 *     <td>BROADCASTING</td>
 *     <td>broker · {@code WsHeartbeatTracker}</td>
 *     <td>broker 全节点 · {@code WsHeartbeatSyncListener}</td>
 *     <td>* (无 tag)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link WebSocket#COMMAND_DOWNLINK}</td>
 *     <td>${THINGLINKS_MQ_NAMESPACE}-ws-command-downlink</td>
 *     <td>BROADCASTING</td>
 *     <td>broker · {@code WebSocketBrokerServiceImpl}</td>
 *     <td>broker 全节点 · {@code WsCommandDownlinkListener}</td>
 *     <td>* (无 tag)</td>
 *   </tr>
 * </table>
 *
 * <h2>使用样例</h2>
 *
 * <pre>{@code
 * // Producer 端 ──「topic:tag」拼装 destination
 * String dest = BizMqRouteConstant.Bridge.DEVICE_EVENT + ":" + envelope.getActionType();
 * rocketmqTemplate.asyncSend(dest, envelope, callback);
 *
 * // Consumer 端 ── 引用常量,避免硬编码
 * @RocketMQMessageListener(
 *     topic = BizMqRouteConstant.Bridge.DEVICE_EVENT,
 *     consumerGroup = BizMqRouteConstant.Groups.BRIDGE_DEVICE_EVENT,
 *     selectorExpression = "*",
 *     messageModel = MessageModel.CLUSTERING,
 *     consumeMode = ConsumeMode.CONCURRENTLY)
 * public class XxxConsumer extends AbstractTenantAwareRocketmqListener<...> { ... }
 *
 * // BROADCASTING 模式 ── ConsumerGroup 必须按节点拼后缀,各节点独立 group
 * consumerGroup = BizMqRouteConstant.Groups.WS_HEARTBEAT_SYNC_PREFIX
 *     + "${HOSTNAME:${spring.cloud.client.ip-address}}",
 * messageModel = MessageModel.BROADCASTING,
 * }</pre>
 *
 * <h2>新增 Topic 时的检查清单</h2>
 * <ol>
 *   <li>在本文件 {@code Bridge} / {@code WebSocket} / 新业务域接口下声明常量</li>
 *   <li>注释必须含 6 项:用途 / Producer / Consumer / Mode / Tag 规约 / ConsumerGroup</li>
 *   <li>{@code Tag} 字符串集中到 {@link Tags} 子接口(避免业务代码硬编码)</li>
 *   <li>{@code ConsumerGroup} 名进 {@link Groups} 子接口</li>
 *   <li>更新本文件顶部"链路一览"图 + "速查表"对应行</li>
 *   <li>RocketMQ broker 侧:阿里云需控制台预创建 topic / group,自建可开 autoCreateTopicEnable</li>
 * </ol>
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
public interface BizMqRouteConstant {

    /** Topic 前缀，由根目录产品清单中的消息队列命名空间组成。 */
    String TOPIC_PREFIX = ConsumerGroupConstant.THINGLINKS_MQ_NAMESPACE + "-";

    /** Consumer Group 前缀，引用 ConsumerGroupConstant 中的消费组前缀。 */
    String CONSUMER_GROUP_PREFIX = ConsumerGroupConstant.THINGLINKS_CONSUMER_GROUP_PREFIX;

    // =========================================================================================
    // 业务域:桥接(rule 服务承载)
    // =========================================================================================

    /**
     * 桥接业务域 Topic 集合。
     *
     * <p>承载"设备数据 → 第三方系统"出站桥接 + "第三方系统 → 平台"入站桥接两条链路;
     * 失败兜底由独立的死信 topic 承接(预留)。
     */
    interface Bridge {

        /**
         * <h3>桥接出站事件 Topic — 设备 → rule 桥接引擎</h3>
         *
         * <p>mqs 在每个协议事件链路尾部以"旁路 + best-effort"方式投递设备事件包络
         * ({@code BridgeMessageEnvelope}),由 rule 桥接消费器消费做规则匹配 + 第三方系统分发。
         *
         * <ul>
         *   <li><b>Producer</b>:thinglinks-mqs · {@code MqsBridgeEventProducer.publishBridgeEvent}
         *       (异步、失败仅 warn 不阻塞主链路)</li>
         *   <li><b>Consumer</b>:thinglinks-rule · {@code BridgeDeviceEventConsumer}
         *       (基于 {@code AbstractTenantAwareRocketmqListener} 自动恢复 ContextUtil)</li>
         *   <li><b>Mode</b>:{@code CLUSTERING}(集群消费 ── 多副本只投一次)</li>
         *   <li><b>Tag</b>:{@code actionType.name()} ── PUBLISH / CONNECT / DISCONNECT / CLOSE /
         *       KICKED / HEART_TIMEOUT / ERROR / PING / SUBSCRIBE / UNSUBSCRIBE / DISPATCH_ERROR /
         *       INBOUND(枚举 {@code DeviceActionTypeEnum} 当前 13 项);actionType blank 时 producer
         *       fallback 为 {@code NO_ACTION} 兜底 tag</li>
         *   <li><b>ConsumerGroup</b>:{@link Groups#BRIDGE_DEVICE_EVENT}</li>
         *   <li><b>QPS 预期</b>:跟设备 publish 量级一致,生产环境建议 ≥ 5k(队列数 ≥ 16)</li>
         *   <li><b>失败处理</b>:Consumer 抛异常 → broker 重投 → 超 max-reconsume-times 进
         *       {@code %DLQ%CID_${THINGLINKS_MQ_NAMESPACE_UPPER}_BRIDGE_DEVICE_EVENT}(自动死信 topic)</li>
         * </ul>
         */
        String DEVICE_EVENT = TOPIC_PREFIX + "bridge-device-event";

        /**
         * <h3>桥接入站消息 Topic — 第三方 → mqs 还原</h3>
         *
         * <p>rule 服务从订阅源(Kafka / MQTT / HTTP endpoint)拉取或接收第三方消息,
         * 完成字段映射后标准化为 {@code BridgeMessageEnvelope};mqs 消费后按 tag 路由到
         * 不同还原策略(MQTT 主链路伪装 / 直写 device_action / 触发规则联动)。
         *
         * <ul>
         *   <li><b>Producer</b>:thinglinks-rule · {@code SubscriptionSourceLifecycleManager}
         *       (订阅源后台拉取) + {@code BridgeIngressOpenAnyUserController}(HTTP POST 入站)</li>
         *   <li><b>Consumer</b>:thinglinks-mqs · {@code BridgeIngressRocketmqConsumerHandler}</li>
         *   <li><b>Mode</b>:{@code CLUSTERING}</li>
         *   <li><b>Tag</b>:targetHandler(由 subscription_source.target_handler 字段决定)
         *       ── {@link Tags#INGRESS_MQTT_FORWARD} / {@link Tags#INGRESS_RAW_INSERT}
         *       / {@link Tags#INGRESS_RULE_TRIGGER}</li>
         *   <li><b>ConsumerGroup</b>:{@link Groups#BRIDGE_INGRESS}</li>
         *   <li><b>失败处理</b>:MQTT_FORWARD / RAW_INSERT 抛异常会触发 broker 重投;
         *       RULE_TRIGGER 失败仅记 log 不重投(规则副作用不能重复执行)</li>
         * </ul>
         */
        String INGRESS = TOPIC_PREFIX + "bridge-ingress";

        /**
         * <h3>桥接死信 Topic — 兜底回放与告警</h3>
         *
         * <p>SinkDispatcher 在重试达到 retry_max_times 仍失败的消息投递到此 topic,
         * 给监控告警系统消费 + DB 死信日志追溯 + 后续手工回放。
         *
         * <ul>
         *   <li><b>Producer</b>:thinglinks-rule · {@code SinkDispatcher.sendToDeadLetter}<i>(预留实现)</i></li>
         *   <li><b>Consumer</b>:监控告警系统 / 运维大盘<i>(待接入)</i></li>
         *   <li><b>Mode</b>:{@code CLUSTERING}</li>
         *   <li><b>Tag</b>:{@code ruleCode}(便于按规则维度告警 / 聚合)</li>
         *   <li><b>ConsumerGroup</b>:{@link Groups#BRIDGE_DLQ}</li>
         * </ul>
         *
         * <p><b>注意</b>:此 topic 与 RocketMQ 内置自动死信 topic({@code %DLQ%<group>})不同 ──
         * 内置 DLQ 是 broker 重投耗尽后的兜底,本 topic 是 rule 业务层主动投递的"业务死信"
         * (重试 + 退避策略由 rule 自己控制,而非依赖 broker 重投)。
         */
        String DEAD_LETTER = TOPIC_PREFIX + "bridge-dlq";
    }

    // =========================================================================================
    // 业务域:WebSocket 集群通信
    // =========================================================================================

    /**
     * WebSocket 集群通信 Topic 集合。
     *
     * <p>承载多副本 broker 节点间的 WS 集群协同:设备心跳跨节点同步 + 下行命令广播投递
     * (各节点查本地 session,持有设备 TCP 的节点投递,无需 owner 路由 / ip:port)。
     */
    interface WebSocket {

        /**
         * <h3>WS 设备心跳跨节点同步 Topic</h3>
         *
         * <p>设备发 PING(应用层心跳)时,持有 session 的 broker 节点广播本事件,其它 broker
         * 节点同步本地 session cache 的 lastActiveTime。
         *
         * <p><b>典型场景</b>:多 broker 副本下,设备从 broker-A 重连到 broker-B,但 owner key
         * 还在 broker-A(短暂窗口);broker-A 看到广播事件后立即更新本地 session lastActiveTime,
         * 避免 timeout checker 误关闭仍活跃的连接。
         *
         * <ul>
         *   <li><b>Producer</b>:thinglinks-broker · {@code WsHeartbeatTracker.update}
         *       (异步、失败仅 warn ── 心跳是软实时,Redis TTL 90s 自然兜底)</li>
         *   <li><b>Consumer</b>:thinglinks-broker 全节点 · {@code WsHeartbeatSyncListener}
         *       (仅当本节点持有该 clientId 的 session 才更新本地存活)</li>
         *   <li><b>Mode</b>:{@code BROADCASTING}(每节点都收)</li>
         *   <li><b>Tag</b>:{@code *}(无 tag,业务侧不区分)</li>
         *   <li><b>ConsumerGroup</b>:{@link Groups#WS_HEARTBEAT_SYNC_PREFIX} + {@code ${spring.application.name}}
         *       (用 application name 作后缀,字符合法;BROADCASTING 模式下同 group 多 instance 各收一份,instance 由 rocketmq-spring 自动生成 PID 区分)</li>
         *   <li><b>QPS 预期</b>:跟设备 PING 频率成正比(默认 30s 一次),100k 设备 ≈ 3.3k QPS</li>
         *   <li><b>失败处理</b>:成功路径不打日志(避免高频刷屏);失败 warn 不抛(广播模式吞掉)</li>
         * </ul>
         */
        String HEARTBEAT_SYNC = TOPIC_PREFIX + "ws-heartbeat-sync";

        /**
         * <h3>WS 设备下行命令广播 Topic</h3>
         *
         * <p>下行命令(平台 → 设备)统一走广播:业务侧调任一 broker 节点 publishMessage,该节点
         * 编码为 ws 子协议报文后广播本事件;<b>每个</b> broker 节点收到后查本地 session 表
         * ({@code WebSocketSubject.Holder}),命中(即持有该设备 TCP 的那一个节点)才推 socket,
         * 其余节点静默忽略。如此恰好一个节点投递,无需知道设备 owner、不存 ip:port、不做点对点转发。
         *
         * <p><b>取代</b>原 owner 路由(Redis 存 ip:port + RestTemplate 反向调用):后者在容器换 IP /
         * 端口不可达 / 多 RestTemplate Bean 等场景脆弱。命令为低频必达场景,广播扇出开销可忽略。
         *
         * <ul>
         *   <li><b>Producer</b>:thinglinks-broker · {@code WebSocketBrokerServiceImpl.publishMessage}
         *       (异步、失败仅 warn ── 不阻塞业务侧下发调用)</li>
         *   <li><b>Consumer</b>:thinglinks-broker 全节点 · {@code WsCommandDownlinkListener}
         *       (仅当本节点持有该 clientId 的 session 才投递)</li>
         *   <li><b>Mode</b>:{@code BROADCASTING}(每节点都收)</li>
         *   <li><b>Tag</b>:{@code *}(无 tag)</li>
         *   <li><b>ConsumerGroup</b>:{@link Groups#WS_COMMAND_DOWNLINK_PREFIX} + {@code ${spring.application.name}}</li>
         *   <li><b>QPS 预期</b>:跟下行命令量级一致(人工 / 规则触发,远低于上行数据流)</li>
         *   <li><b>失败处理</b>:消费异常吞掉不抛(广播模式),由设备重试 / 业务重发兜底</li>
         * </ul>
         */
        String COMMAND_DOWNLINK = TOPIC_PREFIX + "ws-command-downlink";
    }

    // =========================================================================================
    // 业务域:实时告警
    // =========================================================================================

    /**
     * 实时告警 Topic 集合。
     *
     * <p>mqs bus 在 POST 阶段把 PUBLISH 数据上行 / DISPATCH_ERROR(broker 分发失败)错误事件投递到此 topic,
     * 由 rule 服务的 AlarmRealtimeConsumer 消费做规则匹配 + 告警下发(钉钉 / 邮件 / 短信)。
     *
     * <p><b>区别于</b> 桥接通路({@link Bridge#DEVICE_EVENT}):桥接是数据外推到第三方系统;
     * 告警是内部规则触发告警通道,语义不同 ── 故使用独立 topic。
     */
    interface Alarm {

        /**
         * <h3>实时告警事件 Topic</h3>
         *
         * <ul>
         *   <li><b>Producer</b>:thinglinks-mqs · {@code AlarmRealtimeRelayStage}
         *       (POST 阶段,失败仅 warn ── 告警不能影响主链路)</li>
         *   <li><b>Consumer</b>:thinglinks-rule · {@code AlarmRealtimeConsumer}
         *       (规则匹配 + 告警下发,后续接入)</li>
         *   <li><b>Mode</b>:{@code CLUSTERING}(多副本只投一次)</li>
         *   <li><b>Tag</b>:productIdentification ── 便于按产品维度过滤订阅</li>
         *   <li><b>ConsumerGroup</b>:{@link Groups#ALARM_REALTIME}</li>
         * </ul>
         */
        String REALTIME = TOPIC_PREFIX + "alarm-realtime";
    }

    /**
     * Tag 常量集合。
     *
     * <p><b>原则</b>:业务代码 send / consume 时禁止硬编码 tag 字面量,统一引用本接口的常量。
     * 当 selectorExpression 是组合(如 {@code "MQTT_FORWARD || RAW_INSERT"})时,
     * Consumer 注解里也用本常量字符串拼接,确保任何重命名只改一处。
     *
     * <p><b>例外</b>:{@link Bridge#DEVICE_EVENT} 的 tag 直接用 {@code DeviceActionTypeEnum.name()}
     * 枚举值,不在此处枚举(避免重复定义);其它有限离散值的 tag 才进本接口。
     */
    interface Tags {

        // ========== Bridge.DEVICE_EVENT 兜底 Tag ==========

        /**
         * Bridge#DEVICE_EVENT Tag — actionType 为空时的 fallback。
         * <p>正常路径下 tag 用 {@code DeviceActionTypeEnum.name()},此值仅在业务漏传 actionType
         * 时由 {@code MqsBridgeEventProducer.nullSafeTag} 兜底使用并 warn 日志,便于线上排查。
         */
        String BRIDGE_DEVICE_EVENT_NO_ACTION = "NO_ACTION";

        // ========== Bridge.INGRESS targetHandler ==========

        /**
         * Bridge#INGRESS Tag — MQTT 转发(伪装成设备 publish 走原协议链路)。
         * <p>使用方:Producer = rule 订阅源拉取后投递;Consumer = mqs 走 MqttPublishEventListener。
         */
        String INGRESS_MQTT_FORWARD = "MQTT_FORWARD";

        /**
         * Bridge#INGRESS Tag — 直接写 device_action 表(旁路 protocol handler)。
         * <p>使用方:Producer = rule 订阅源(target_handler=RAW_INSERT);Consumer = mqs 调 link-api Feign。
         */
        String INGRESS_RAW_INSERT = "RAW_INSERT";

        /**
         * Bridge#INGRESS Tag — 触发场景联动规则。
         * <p>使用方:Producer = rule 订阅源(target_handler=RULE_TRIGGER);Consumer = mqs 调 rule-api Feign。
         */
        String INGRESS_RULE_TRIGGER = "RULE_TRIGGER";

    }

    /**
     * ConsumerGroup 常量集合。
     *
     * <p><b>命名规约</b>:
     * <ul>
     *   <li>{@code CLUSTERING} 模式:固定 group 名 {@code CID_${THINGLINKS_MQ_NAMESPACE_UPPER}_<DOMAIN>_<PURPOSE>},
     *       多副本同 group 共享 offset,broker 负载均衡</li>
     *   <li>{@code BROADCASTING} 模式:必须每节点独立 group(否则各节点抢占同一 offset 互相覆盖),
     *       本接口提供 {@code _PREFIX} 常量,业务侧 Consumer 注解里拼接节点标识(优先 HOSTNAME,兜底 IP):
     *       <pre>{@code consumerGroup = Groups.WS_HEARTBEAT_SYNC_PREFIX + "${HOSTNAME:${spring.cloud.client.ip-address}}"}</pre></li>
     * </ul>
     */
    interface Groups {

        /**
         * 桥接出站事件消费组({@link Bridge#DEVICE_EVENT} CLUSTERING 模式)。
         * <p>使用方:rule · {@code BridgeDeviceEventConsumer}。
         */
        String BRIDGE_DEVICE_EVENT = CONSUMER_GROUP_PREFIX + "BRIDGE_DEVICE_EVENT";

        /**
         * 桥接入站消息消费组({@link Bridge#INGRESS} CLUSTERING 模式)。
         * <p>使用方:mqs · {@code BridgeIngressRocketmqConsumerHandler}。
         */
        String BRIDGE_INGRESS = CONSUMER_GROUP_PREFIX + "BRIDGE_INGRESS";

        /**
         * 桥接死信消费组({@link Bridge#DEAD_LETTER} CLUSTERING 模式)。
         * <p>使用方:监控告警系统<i>(待接入)</i>。
         */
        String BRIDGE_DLQ = CONSUMER_GROUP_PREFIX + "BRIDGE_DLQ";

        /**
         * 规则引擎事件触发消费组({@link Bridge#DEVICE_EVENT} CLUSTERING 模式)。
         * <p>与 {@link #BRIDGE_DEVICE_EVENT}(桥接 Sink 分发)是同 topic 的两个独立消费组,
         * 各自收到全量设备事件互不影响:本组供 rule 服务把设备上报/生命周期事件实时转化为规则评估。
         * <p>使用方:rule · {@code RuleTriggerEventConsumer}。
         */
        String RULE_TRIGGER_EVENT = CONSUMER_GROUP_PREFIX + "RULE_TRIGGER_EVENT";

        /**
         * WS 心跳跨节点同步广播消费组前缀({@link WebSocket#HEARTBEAT_SYNC} BROADCASTING 模式)。
         * <p>运行时拼装节点 HOSTNAME / IP:
         * <pre>{@code Groups.WS_HEARTBEAT_SYNC_PREFIX + "${HOSTNAME:${spring.cloud.client.ip-address}}"}</pre>
         * <p>使用方:broker · {@code WsHeartbeatSyncListener}。
         */
        String WS_HEARTBEAT_SYNC_PREFIX = CONSUMER_GROUP_PREFIX + "WS_HEARTBEAT_SYNC_";

        /**
         * WS 下行命令广播消费组前缀({@link WebSocket#COMMAND_DOWNLINK} BROADCASTING 模式)。
         * <p>运行时拼装节点标识(用 application name 后缀):
         * <pre>{@code Groups.WS_COMMAND_DOWNLINK_PREFIX + "${spring.application.name}"}</pre>
         * <p>使用方:broker · {@code WsCommandDownlinkListener}。
         */
        String WS_COMMAND_DOWNLINK_PREFIX = CONSUMER_GROUP_PREFIX + "WS_COMMAND_DOWNLINK_";

        /**
         * 实时告警事件消费组({@link Alarm#REALTIME} CLUSTERING 模式)。
         * <p>使用方:rule · {@code AlarmRealtimeConsumer}<i>(待接入)</i>。
         */
        String ALARM_REALTIME = CONSUMER_GROUP_PREFIX + "ALARM_REALTIME";
    }
}
