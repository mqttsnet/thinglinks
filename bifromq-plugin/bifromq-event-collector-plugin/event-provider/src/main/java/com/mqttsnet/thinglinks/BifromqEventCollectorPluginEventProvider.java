/*
 * Copyright (c) 2024. The BifroMQ Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.mqttsnet.thinglinks;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.baidu.bifromq.plugin.eventcollector.Event;
import com.baidu.bifromq.plugin.eventcollector.EventType;
import com.baidu.bifromq.plugin.eventcollector.IEventCollector;
import com.mqttsnet.thinglinks.config.EventCollectorConfig;
import com.mqttsnet.thinglinks.config.PluginConfig;
import com.mqttsnet.thinglinks.processor.EventProcessor;
import com.mqttsnet.thinglinks.processor.EventProcessorFactory;
import com.mqttsnet.thinglinks.sender.KafkaMessageSender;
import com.mqttsnet.thinglinks.util.TaskQueue;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;


/**
 * BifroMQ 事件收集插件入口 ── 把 broker 内核事件按 type 路由到 Kafka topic 透传给下游(mqs / 审计).
 *
 * <h3>实现要点</h3>
 * <ul>
 *   <li>{@link IEventCollector#report} 入口同步抓 {@code hlc / utc} ── HLC 严格单调,异步 worker 取值会破坏因果顺序</li>
 *   <li>{@link TaskQueue} 64 worker 池 ── 解析 + send 异步化,主链路 0 阻塞</li>
 *   <li>{@link #TOPIC_MAP} + {@link EventProcessorFactory} 双层注册 ── 任一缺失即 warn 丢弃,启动期可通过日志审计覆盖率</li>
 * </ul>
 *
 * <h3>4.0 升级备注</h3>
 * <ul>
 *   <li>升级时 import 包名 {@code com.baidu.bifromq} → {@code org.apache.bifromq} 全局替换</li>
 *   <li>{@link #TOPIC_MAP} 中 {@code EventType.SERVER_REDIRECTED} 对应 {@code <前缀>.server.disconnect.topic}</li>
 *   <li>{@link EventProcessorFactory} 注册 {@code ServerRedirectedEventProcessor}(新建)</li>
 * </ul>
 *
 * @author xiaonannet
 * @version 1.1
 * @since 2024/2/23
 */
@Slf4j
@Extension
public final class BifromqEventCollectorPluginEventProvider implements IEventCollector {

    /**
     * Kafka Topic 公共前缀，与 ThingLinks Cloud/MQS 使用相同的跨服务协议值。
     * 该值属于运行时通信协议，不属于产品发行元数据。
     */
    private static final String KAFKA_TOPIC_PREFIX = "mqtt";

    /**
     * Kafka topic 路由表 ── EventType → topic.
     *
     * <h4>分组策略</h4>
     * <ul>
     *   <li>{@code BY_CLIENT}(客户端主动断)→ {@code <前缀>.client.disconnect.topic}</li>
     *   <li>{@code KICKED}(同 clientId 抢占)→ {@code <前缀>.device.kicked.topic}</li>
     *   <li>其余 18 个被动 disconnect(含 {@code IDLE} KeepAlive 超时)→ 统一进 {@code <前缀>.server.disconnect.topic},
     *       下游 mqs 按 actionType=CLOSE 统一写 OFFLINE,reasonCode/特异字段从 message body 取</li>
     * </ul>
     */
    private static final Map<EventType, String> TOPIC_MAP = new EnumMap<>(EventType.class);

    static {
        // ── client connected ──
        TOPIC_MAP.put(EventType.CLIENT_CONNECTED, topic("client.connected.topic"));

        // ── disconnect:语义分流到 3 个 topic ──
        TOPIC_MAP.put(EventType.BY_CLIENT, topic("client.disconnect.topic"));
        TOPIC_MAP.put(EventType.KICKED, topic("device.kicked.topic"));
        // 18 个被动断都归一到 server.disconnect topic ── 下游 mqs 写 OFFLINE 不需要区分子类型
        TOPIC_MAP.put(EventType.BY_SERVER, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.IDLE, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.BAD_PACKET, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.CLIENT_CHANNEL_ERROR, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.EXCEED_PUB_RATE, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.EXCEED_RECEIVING_LIMIT, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.INBOX_TRANSIENT_ERROR, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.INVALID_TOPIC, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.INVALID_TOPIC_FILTER, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.MALFORMED_TOPIC, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.MALFORMED_TOPIC_FILTER, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.NO_PUB_PERMISSION, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.PROTOCOL_VIOLATION, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.RE_AUTH_FAILED, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.RESOURCE_THROTTLED, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.SERVER_BUSY, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.TOO_LARGE_SUBSCRIPTION, topic("server.disconnect.topic"));
        TOPIC_MAP.put(EventType.TOO_LARGE_UNSUBSCRIPTION, topic("server.disconnect.topic"));
        // BifroMQ 4.0 的 SERVER_REDIRECTED 事件对应 server.disconnect topic。

        // ── pub/sub handling ──
        TOPIC_MAP.put(EventType.SUB_ACKED, topic("subscription.acked.topic"));
        TOPIC_MAP.put(EventType.UNSUB_ACKED, topic("unsubscription.acked.topic"));
        // DISTED 承载设备 PUBLISH 上行的完整报文(topic/qos/payload/publisher) ──
        // BifroMQ Standalone 无 mqtt-to-kafka connector,这是设备上行唯一可用来源;
        // mqs MqttDeviceDataEdgeAdapter 把本 topic 路由到 DEVICE_DATA group → DevicePayloadDecodeStage → DeviceDatasHandler 走主流程(物模型 + TDS 入库).
        // 上下行都触发:下行命令(backend publisher)在 mqs DeviceCacheEnricher cache miss 后自然 skip,无需额外过滤.
        TOPIC_MAP.put(EventType.DISTED, topic("distribution.completed.topic"));
        // DIST_ERROR 是 broker dispatch 失败,businessSystemEventType="DISPATCH_ERROR" 协议层真相命名;
        // mqs MqttDistributionEdgeAdapter → DISTRIBUTION_ACK group → DistributionResultStage 记失败 stats.
        TOPIC_MAP.put(EventType.DIST_ERROR, topic("distribution.error.topic"));
        TOPIC_MAP.put(EventType.PING_REQ, topic("ping.req.topic"));

        // ── audit 类(mqs `bus/inbound/kafka/audit/` 下 3 个独立 consumer 已就绪,仅 log 消费,不入业务流程)──
        TOPIC_MAP.put(EventType.NOT_AUTHORIZED_CLIENT, topic("client.unauthorized"));
        TOPIC_MAP.put(EventType.MQTT_SESSION_START, topic("session.start"));
        TOPIC_MAP.put(EventType.MQTT_SESSION_STOP, topic("session.stop"));
    }

    private static String topic(String suffix) {
        return KAFKA_TOPIC_PREFIX + "." + suffix;
    }

    private final KafkaMessageSender sender;
    private final EventProcessorFactory processorFactory;

    /**
     * TaskQueue 64 worker ── 上游高频事件(CONNECT/SUB/PUB-delivered/PING)单线程处理不够,
     * 提到 64 worker 应对 50000+ events/s.每 worker 任务极短(JSON 序列化 + producer.send 异步入队).
     */
    private final TaskQueue taskQueue = new TaskQueue(64, 64, 60L, TimeUnit.SECONDS);

    public BifromqEventCollectorPluginEventProvider(BifromqEventCollectorContext context) {
        PluginConfig pluginConfig = context.getPluginConfig();
        EventCollectorConfig eventCollectorConfig = pluginConfig.getEventCollectorConfig();
        log.info("EventProvider initialized with Kafka server: {}", eventCollectorConfig.getKafkaBootstrapServer());

        this.sender = new KafkaMessageSender(eventCollectorConfig);
        this.processorFactory = new EventProcessorFactory();
    }

    @Override
    public void report(Event<?> eventObj) {
        Event<?> event = (Event<?>) eventObj.clone();
        // ⭐ 入口同步抓 HLC + UTC ── 上游调 report() 的当下抓取,
        // 不让 64 worker 池异步取值破坏因果顺序;HLC 严格单调,这里保存即真相;
        // 若在 worker 内才取,CONNECT/DISCONNECT 同毫秒发生时 worker 调度抖动会让取值反序.
        final long eventHlc = event.hlc();
        final long eventUtc = event.utc();
        log.info("Received event type={} hlc={} utc={}", event.type(), eventHlc, eventUtc);

        taskQueue.addTask(() -> dispatch(event, eventHlc, eventUtc));
    }

    /**
     * 异步分发单条事件:TOPIC_MAP 找 topic,Factory 找 processor,任一缺失 warn 丢弃.
     *
     * @param event    已 clone 的事件
     * @param eventHlc 入口抓取的 HLC
     * @param eventUtc 入口抓取的 UTC ms
     */
    private void dispatch(Event<?> event, long eventHlc, long eventUtc) {
        Optional<String> topicOpt = Optional.ofNullable(TOPIC_MAP.get(event.type()));
        if (topicOpt.isEmpty()) {
            log.warn("Discarding events of type {} as no mapping exists in TOPIC_MAP.", event.type());
            return;
        }
        Optional<EventProcessor> processorOpt = processorFactory.getProcessor(event.type());
        if (processorOpt.isEmpty()) {
            log.warn("No processor registered for event type: {}", event.type());
            return;
        }
        long startTime = System.currentTimeMillis();
        log.info("Starting execution event type={} hlc={}", event.type(), eventHlc);
        processorOpt.get().process(event, topicOpt.get(), sender, eventHlc, eventUtc);
        log.info("Completed event type={} hlc={} duration={}ms",
            event.type(), eventHlc, System.currentTimeMillis() - startTime);
    }

    @Override
    public void close() {
        taskQueue.shutdown();
        sender.close();
    }
}
