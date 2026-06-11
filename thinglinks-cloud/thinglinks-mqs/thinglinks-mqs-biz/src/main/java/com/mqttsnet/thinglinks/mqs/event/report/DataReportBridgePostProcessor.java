package com.mqttsnet.thinglinks.mqs.event.report;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.mqs.bridge.MqsBridgeEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认数据上报后置处理器 ── 把物模型匹配后的结构化数据以 {@code THING_MODEL} 形态桥接出站。
 *
 * <p>复用既有桥接旁路通道({@link MqsBridgeEventProducer} → RocketMQ → rule 规则匹配 → Sink),
 * 与原始报文({@code RAW})桥接并行;桥接规则按 {@code payloadKinds} 选择消费物模型还是原始报文。
 * 投递失败只 warn,不阻断上报落库主链路。
 *
 * @author mqttsnet
 * @since 2026-06-03
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataReportBridgePostProcessor implements DeviceDataReportPostProcessor {

    private final MqsBridgeEventProducer mqsBridgeEventProducer;

    @Override
    public void onReported(DeviceDataReportContext ctx) {
        if (ctx == null || ctx.getNormalized() == null) {
            return;
        }
        try {
            BridgeMessageEnvelope envelope = BridgeMessageEnvelope.builder()
                .appId(ctx.getAppId())
                .productIdentification(ctx.getProductIdentification())
                .deviceIdentification(ctx.getDeviceIdentification())
                .actionType(DeviceActionTypeEnum.PUBLISH.getValue())
                .payloadKind(BridgeMessageEnvelope.PAYLOAD_KIND_THING_MODEL)
                // 物模型结构化数据直接放 rawMessage:下游 Sink/transform/trace 与 RAW 统一处理,0 改动
                .rawMessage(JSON.toJSONString(ctx.getNormalized()))
                .ts(ctx.getTs())
                .build();
            mqsBridgeEventProducer.publishBridgeEvent(envelope);
        } catch (Exception e) {
            log.warn("[DataReportBridge] publish thing-model bridge event failed (non-blocking) device={} err={}",
                ctx.getDeviceIdentification(), e.getMessage());
        }
    }
}
