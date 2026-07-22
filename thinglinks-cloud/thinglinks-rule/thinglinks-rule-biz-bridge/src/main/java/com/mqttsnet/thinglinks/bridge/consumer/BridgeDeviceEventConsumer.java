package com.mqttsnet.thinglinks.bridge.consumer;

import java.util.List;

import com.mqttsnet.basic.rocketmq.listener.AbstractTenantAwareRocketmqListener;
import com.mqttsnet.thinglinks.bridge.dispatcher.SinkDispatcher;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeRuleMatcher;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 桥接设备事件 RocketMQ 消费器。
 *
 * <p>mqs.MqsBridgeEventProducer → {@link BizMqRouteConstant.Bridge#DEVICE_EVENT} → 本 Consumer
 * → matcher 命中 N 条 → dispatcher 顺序 dispatch(下游 Sink 各自异步发送)。
 *
 * <p>{@link AbstractTenantAwareRocketmqListener} 自动从 header(X-Thinglinks-LocalMap)恢复
 * ContextUtil.LocalMap,traceId / tenantId / tenantBasePoolName 跟同步线程一致。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(
    topic = BizMqRouteConstant.Bridge.DEVICE_EVENT,
    consumerGroup = BizMqRouteConstant.Groups.BRIDGE_DEVICE_EVENT,
    selectorExpression = "*",
    messageModel = MessageModel.CLUSTERING,
    consumeMode = ConsumeMode.CONCURRENTLY
)
public class BridgeDeviceEventConsumer extends AbstractTenantAwareRocketmqListener<BridgeMessageEnvelope> {

    private final BridgeRuleMatcher matcher;
    private final SinkDispatcher dispatcher;

    @Override
    protected Class<BridgeMessageEnvelope> getBodyClass() {
        return BridgeMessageEnvelope.class;
    }

    @Override
    protected void onTenantMessage(BridgeMessageEnvelope envelope, MessageExt raw) {
        if (envelope == null) {
            log.warn("[BridgeDeviceEventConsumer] received null envelope, msgId={}", raw.getMsgId());
            return;
        }
        String traceId = envelope.getTraceId();
        String clientId = envelope.getClientId();
        String actionType = envelope.getActionType();
        log.info("[BridgeDeviceEventConsumer] consume msgId={} traceId={} clientId={} product={} device={} action={} payloadKind={} topic={}",
            raw.getMsgId(), traceId, clientId, envelope.getProductIdentification(),
            envelope.getDeviceIdentification(), actionType, envelope.getPayloadKind(), envelope.getTopic());
        long matchStart = System.currentTimeMillis();
        try {
            List<DataBridgeCacheVO> hits = matcher.matchOutbound(envelope);
            long matchLatencyMs = System.currentTimeMillis() - matchStart;
            if (hits.isEmpty()) {
                log.info("[BridgeDeviceEventConsumer] no matched bridge rules traceId={} clientId={} action={} topic={} latency={}ms",
                    traceId, clientId, actionType, envelope.getTopic(), matchLatencyMs);
                return;
            }
            log.info("[BridgeDeviceEventConsumer] matched {} rules traceId={} clientId={} action={} topic={} latency={}ms",
                hits.size(), traceId, clientId, actionType, envelope.getTopic(), matchLatencyMs);
            // 串行 dispatch:命中规则各自独立 trace,下游 Sink 连接池复用 + 异步重试,整体吞吐由 RocketMQ 线程控制
            hits.forEach(rule -> dispatcher.dispatch(rule, envelope, matchLatencyMs));
        } catch (Exception e) {
            // 让 RocketMQ 触发 broker 重投(超 max-reconsume-times 进死信 group)
            log.error("[BridgeDeviceEventConsumer] dispatch failed, will retry msgId={} traceId={} clientId={} action={}",
                raw.getMsgId(), traceId, clientId, actionType, e);
            throw e;
        }
    }
}
