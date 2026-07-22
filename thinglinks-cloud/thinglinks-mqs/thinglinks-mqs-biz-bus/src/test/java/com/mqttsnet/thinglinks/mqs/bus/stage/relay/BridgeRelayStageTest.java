package com.mqttsnet.thinglinks.mqs.bus.stage.relay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.mqs.bridge.MqsBridgeEventProducer;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("MQS 北向桥接转发阶段")
class BridgeRelayStageTest {

    private final MqsBridgeEventProducer producer = org.mockito.Mockito.mock(MqsBridgeEventProducer.class);
    private final BusStatsService statsService = org.mockito.Mockito.mock(BusStatsService.class);
    private final BridgeRelayStage stage = new BridgeRelayStage(producer, statsService);

    @Test
    @DisplayName("验证原始设备事件会封装为桥接消息并保留事件时间字段")
    void executePublishesRawBridgeEnvelopeWithEventTimeFields() {
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.PUBLISH.getValue());
        when(producer.publishBridgeEvent(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        stage.execute(event, StageContext.create(event));

        ArgumentCaptor<BridgeMessageEnvelope> captor = ArgumentCaptor.forClass(BridgeMessageEnvelope.class);
        verify(producer).publishBridgeEvent(captor.capture());
        BridgeMessageEnvelope envelope = captor.getValue();
        assertThat(envelope.getTraceId()).isEqualTo("trace-1");
        assertThat(envelope.getTenantId()).isEqualTo("1");
        assertThat(envelope.getProtocolType()).isEqualTo(ProtocolTypeEnum.MQTT.getValue());
        assertThat(envelope.getActionType()).isEqualTo(DeviceActionTypeEnum.PUBLISH.getValue());
        assertThat(envelope.getClientId()).isEqualTo("client-1");
        assertThat(envelope.getProductIdentification()).isEqualTo("product-1");
        assertThat(envelope.getDeviceIdentification()).isEqualTo("device-1");
        assertThat(envelope.getTopic()).isEqualTo("/v1/devices/device-1/datas");
        assertThat(envelope.getRawMessage()).isEqualTo("{\"raw\":true}");
        assertThat(envelope.getPayloadKind()).isEqualTo(BridgeMessageEnvelope.PAYLOAD_KIND_RAW);
        assertThat(envelope.getTs()).isEqualTo(1780000000000L);
        assertThat(envelope.getEventUtc()).isEqualTo(1780000000000L);
        assertThat(envelope.getEventHlc()).isEqualTo(9900L);
        verify(statsService).incrementRelay("BridgeRelayStage", "rocketmq",
            BizMqRouteConstant.Bridge.DEVICE_EVENT, "ok");
    }

    @Test
    @DisplayName("验证生产者返回失败时桥接阶段记录失败指标")
    void executeRecordsFailureWhenProducerReturnsFalse() {
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.PUBLISH.getValue());
        when(producer.publishBridgeEvent(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        stage.execute(event, StageContext.create(event));

        verify(statsService).incrementRelay("BridgeRelayStage", "rocketmq",
            BizMqRouteConstant.Bridge.DEVICE_EVENT, "fail");
    }

    @Test
    @DisplayName("验证身份字段不完整时仍按原策略投递并记录成功指标")
    void executeStillPublishesWhenIdentityIncomplete() {
        DeviceProtocolEvent event = DeviceProtocolEvent.builder()
            .traceId("trace-missing-identity")
            .tenantId("1")
            .protocolType(ProtocolTypeEnum.MQTT.getValue())
            .eventType(DeviceActionTypeEnum.PING.getValue())
            .clientId("client-1")
            .rawMessage("{\"ping\":true}")
            .eventUtc(1780000000001L)
            .build();
        when(producer.publishBridgeEvent(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        stage.execute(event, StageContext.create(event));

        ArgumentCaptor<BridgeMessageEnvelope> captor = ArgumentCaptor.forClass(BridgeMessageEnvelope.class);
        verify(producer).publishBridgeEvent(captor.capture());
        assertThat(captor.getValue().getProductIdentification()).isNull();
        assertThat(captor.getValue().getDeviceIdentification()).isNull();
        verify(statsService).incrementRelay("BridgeRelayStage", "rocketmq",
            BizMqRouteConstant.Bridge.DEVICE_EVENT, "ok");
    }

    @Test
    @DisplayName("验证桥接生产者异常时仅记录失败计数，不中断主链路")
    void executeRecordsFailureWhenProducerThrows() {
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.PUBLISH.getValue());
        org.mockito.Mockito.doThrow(new IllegalStateException("mq down"))
            .when(producer).publishBridgeEvent(org.mockito.ArgumentMatchers.any());

        stage.execute(event, StageContext.create(event));

        verify(statsService).incrementRelay("BridgeRelayStage", "rocketmq",
            BizMqRouteConstant.Bridge.DEVICE_EVENT, "fail");
    }

    @Test
    @DisplayName("验证桥接阶段只要求事件动作类型存在")
    void supportsRequiresEventType() {
        assertThat(stage.supports(DeviceProtocolEvent.builder().eventType("publish").build())).isTrue();
        assertThat(stage.supports(DeviceProtocolEvent.builder().build())).isFalse();
        assertThat(stage.supports(null)).isFalse();
    }

    private static DeviceProtocolEvent event(String actionType) {
        return DeviceProtocolEvent.builder()
            .traceId("trace-1")
            .tenantId("1")
            .protocolType(ProtocolTypeEnum.MQTT.getValue())
            .eventType(actionType)
            .clientId("client-1")
            .userId("user-1")
            .address("/127.0.0.1:1883")
            .appId("app-1")
            .productIdentification("product-1")
            .deviceIdentification("device-1")
            .topic("/v1/devices/device-1/datas")
            .rawMessage("{\"raw\":true}")
            .ts(100L)
            .eventUtc(1780000000000L)
            .eventHlc(9900L)
            .build();
    }
}
