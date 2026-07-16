package com.mqttsnet.thinglinks.mqs.bus.stage.relay;

import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.bus.stage.AbstractDeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.mqs.bridge.MqsBridgeEventProducer;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 桥接旁路投递 Stage,POST 阶段,事件转 {@link BridgeMessageEnvelope} → RocketMQ
 * {@link BizMqRouteConstant.Bridge#DEVICE_EVENT}。复用既有 producer,失败仅 stats。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BridgeRelayStage extends AbstractDeviceEventStage {

    private static final String MQ_ROCKETMQ = "rocketmq";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_FAIL = "fail";
    private static final String PROTOCOL_FALLBACK = "INTERNAL";

    private final MqsBridgeEventProducer mqsBridgeEventProducer;
    private final BusStatsService statsService;

    @Override
    public StagePhaseEnum getPhase() {
        return StagePhaseEnum.POST;
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public boolean supports(DeviceProtocolEvent event) {
        return event != null && event.getEventType() != null;
    }

    @Override
    protected void doExecute(DeviceProtocolEvent event, StageContext context) {
        try {
            BridgeMessageEnvelope envelope = toEnvelope(event);
            warnIfIdentityIncomplete(envelope);
            boolean sent = mqsBridgeEventProducer.publishBridgeEvent(envelope);
            statsService.incrementRelay(getName(), MQ_ROCKETMQ, BizMqRouteConstant.Bridge.DEVICE_EVENT,
                sent ? STATUS_OK : STATUS_FAIL);
        } catch (Exception e) {
            log.warn("{} bridge relay failed traceId={} clientId={} action={} err={}",
                BusConstants.Log.STAGE_FAIL, event.getTraceId(),
                event.getClientId(), event.getEventType(), e.getMessage());
            statsService.incrementRelay(getName(), MQ_ROCKETMQ, BizMqRouteConstant.Bridge.DEVICE_EVENT, STATUS_FAIL);
        }
    }

    /**
     * event → envelope 字段映射.
     * <p>{@code ts} 优先 {@code eventUtc}(事件真实发生瞬间)后 {@code ts}(adapter 入站瞬间)兜底;
     * {@code eventHlc} 透传因果时钟供 rule 端 dedup / 排序;
     * {@code eventUtc} 透传设备事件真实发生瞬间供下游业务展示 / 时序入库.
     */
    private BridgeMessageEnvelope toEnvelope(DeviceProtocolEvent event) {
        return BridgeMessageEnvelope.builder()
            .traceId(event.getTraceId())
            .tenantId(event.getTenantId())
            .protocolType(StrUtil.nullToDefault(event.getProtocolType(), PROTOCOL_FALLBACK))
            .actionType(event.getEventType())
            .clientId(StrUtil.nullToDefault(event.getClientId(), StrUtil.EMPTY))
            .userId(event.getUserId())
            .address(event.getAddress())
            .appId(event.getAppId())
            .productIdentification(event.getProductIdentification())
            .deviceIdentification(event.getDeviceIdentification())
            .topic(event.getTopic())
            .rawMessage(event.getRawMessage())
            .payloadKind(BridgeMessageEnvelope.PAYLOAD_KIND_RAW)
            .ts(Optional.ofNullable(event.getEventUtc()).orElse(event.getTs()))
            .eventHlc(event.getEventHlc())
            .eventUtc(event.getEventUtc())
            .build();
    }

    private void warnIfIdentityIncomplete(BridgeMessageEnvelope envelope) {
        if (StrUtil.hasBlank(envelope.getTenantId(), envelope.getProductIdentification(),
            envelope.getDeviceIdentification())) {
            log.warn("[bus.bridge.relay] identity incomplete before RocketMQ send traceId={} clientId={} tenantId={} product={} device={} action={} topic={}",
                envelope.getTraceId(), envelope.getClientId(), envelope.getTenantId(),
                envelope.getProductIdentification(), envelope.getDeviceIdentification(),
                envelope.getActionType(), envelope.getTopic());
        }
    }
}
