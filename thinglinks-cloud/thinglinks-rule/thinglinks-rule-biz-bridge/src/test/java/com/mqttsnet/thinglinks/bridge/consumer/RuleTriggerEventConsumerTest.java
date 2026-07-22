package com.mqttsnet.thinglinks.bridge.consumer;

import java.util.Collections;
import java.util.List;

import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.dto.linkage.execution.TriggerEventDTO;
import com.mqttsnet.thinglinks.enumeration.linkage.ConditionTypeEnum;
import com.mqttsnet.thinglinks.service.execution.trigger.DeviceLatestSnapshotService;
import com.mqttsnet.thinglinks.service.execution.trigger.RuleTriggerIndexService;
import com.mqttsnet.thinglinks.service.linkage.RuleService;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("规则触发设备事件消费者")
class RuleTriggerEventConsumerTest {

    private static final String PRODUCT_ID = "product-1";
    private static final String DEVICE_ID = "device-1";
    private static final Long EVENT_UTC = 1783249201000L;
    private static final String THING_MODEL_PAYLOAD = "{}";

    @Mock
    private RuleTriggerIndexService ruleTriggerIndexService;
    @Mock
    private DeviceLatestSnapshotService deviceLatestSnapshotService;
    @Mock
    private RuleService ruleService;
    @InjectMocks
    private RuleTriggerEventConsumer consumer;

    @Test
    @DisplayName("设备身份缺失的消息直接跳过，避免污染触发索引")
    void onTenantMessageShouldSkipInvalidEnvelope() {
        BridgeMessageEnvelope envelope = BridgeMessageEnvelope.builder()
                .traceId("trace-invalid")
                .productIdentification(PRODUCT_ID)
                .build();

        consumer.onTenantMessage(envelope, rawMessage());

        verifyNoInteractions(ruleTriggerIndexService, deviceLatestSnapshotService, ruleService);
    }

    @Test
    @DisplayName("物模型消息没有原始内容时不更新快照、不查询规则")
    void onTenantMessageShouldSkipThingModelWithoutRawMessage() {
        BridgeMessageEnvelope envelope = publishThingModel(null);

        consumer.onTenantMessage(envelope, rawMessage());

        verifyNoInteractions(ruleTriggerIndexService, deviceLatestSnapshotService, ruleService);
    }

    @Test
    @DisplayName("物模型消息消费后先更新最新快照；未命中属性规则时不触发规则执行")
    void onTenantMessageShouldUpdateSnapshotAndStopWhenNoPropertyRules() {
        BridgeMessageEnvelope envelope = publishThingModel(THING_MODEL_PAYLOAD);
        when(ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue(), PRODUCT_ID, DEVICE_ID))
                .thenReturn(Collections.emptyList());

        consumer.onTenantMessage(envelope, rawMessage());

        verify(deviceLatestSnapshotService).updateLatest(PRODUCT_ID, DEVICE_ID, EVENT_UTC, THING_MODEL_PAYLOAD);
        verifyNoInteractions(ruleService);
    }

    @Test
    @DisplayName("物模型消息命中属性触发规则时，按属性条件类型携带消息内值执行规则")
    void onTenantMessageShouldDispatchPropertyRulesWithThingModelEvent() {
        BridgeMessageEnvelope envelope = publishThingModel(THING_MODEL_PAYLOAD);
        when(ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue(), PRODUCT_ID, DEVICE_ID))
                .thenReturn(List.of("rule-a", "rule-b"));
        ArgumentCaptor<TriggerEventDTO> eventCaptor = ArgumentCaptor.forClass(TriggerEventDTO.class);

        consumer.onTenantMessage(envelope, rawMessage());

        verify(ruleService).triggerRulePolicyForEvent(nullable(Long.class), eq("rule-a"),
                eq(ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue()), eventCaptor.capture());
        verify(ruleService).triggerRulePolicyForEvent(nullable(Long.class), eq("rule-b"),
                eq(ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue()), any());
        TriggerEventDTO triggerEvent = eventCaptor.getValue();
        assertEquals(PRODUCT_ID, triggerEvent.getProductIdentification());
        assertEquals(DEVICE_ID, triggerEvent.getDeviceIdentification());
        assertEquals("PUBLISH", triggerEvent.getActionType());
        assertEquals(BridgeMessageEnvelope.PAYLOAD_KIND_THING_MODEL, triggerEvent.getPayloadKind());
        assertEquals(EVENT_UTC, triggerEvent.getEventUtc());
        assertEquals(THING_MODEL_PAYLOAD, triggerEvent.getRawMessage());
        assertNotNull(triggerEvent.getThingModel());
    }

    @Test
    @DisplayName("物模型内容不是合法 JSON 时只更新快照并停止执行，避免把坏数据送进规则策略")
    void onTenantMessageShouldStopWhenThingModelPayloadCannotBeParsed() {
        String brokenPayload = "{";
        BridgeMessageEnvelope envelope = publishThingModel(brokenPayload);
        when(ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_PROPERTIES_TRIGGER.getValue(), PRODUCT_ID, DEVICE_ID))
                .thenReturn(List.of("rule-a"));

        consumer.onTenantMessage(envelope, rawMessage());

        verify(deviceLatestSnapshotService).updateLatest(PRODUCT_ID, DEVICE_ID, EVENT_UTC, brokenPayload);
        verifyNoInteractions(ruleService);
    }

    @Test
    @DisplayName("原始 PUBLISH 消息只走动作触发索引；未命中动作规则时不触发规则执行")
    void onTenantMessageShouldUseActionIndexForRawPublish() {
        BridgeMessageEnvelope envelope = publishRaw();
        when(ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue(), PRODUCT_ID, DEVICE_ID))
                .thenReturn(Collections.emptyList());

        consumer.onTenantMessage(envelope, rawMessage());

        verifyNoInteractions(deviceLatestSnapshotService);
        verifyNoInteractions(ruleService);
    }

    @Test
    @DisplayName("生命周期消息命中动作触发规则时，按动作条件类型携带实时事件执行规则")
    void onTenantMessageShouldDispatchLifecycleActionRules() {
        BridgeMessageEnvelope envelope = lifecycle("DISCONNECT");
        when(ruleTriggerIndexService.findTriggeredRules(
                ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue(), PRODUCT_ID, DEVICE_ID))
                .thenReturn(List.of("rule-offline"));
        ArgumentCaptor<TriggerEventDTO> eventCaptor = ArgumentCaptor.forClass(TriggerEventDTO.class);

        consumer.onTenantMessage(envelope, rawMessage());

        verify(ruleService).triggerRulePolicyForEvent(nullable(Long.class), eq("rule-offline"),
                eq(ConditionTypeEnum.DEVICE_ACTION_TRIGGER.getValue()), eventCaptor.capture());
        TriggerEventDTO triggerEvent = eventCaptor.getValue();
        assertEquals(PRODUCT_ID, triggerEvent.getProductIdentification());
        assertEquals(DEVICE_ID, triggerEvent.getDeviceIdentification());
        assertEquals("DISCONNECT", triggerEvent.getActionType());
        assertEquals(BridgeMessageEnvelope.PAYLOAD_KIND_RAW, triggerEvent.getPayloadKind());
        assertEquals(EVENT_UTC, triggerEvent.getEventUtc());
    }

    private BridgeMessageEnvelope publishThingModel(String rawMessage) {
        return baseEnvelope("PUBLISH", BridgeMessageEnvelope.PAYLOAD_KIND_THING_MODEL)
                .toBuilder()
                .rawMessage(rawMessage)
                .build();
    }

    private BridgeMessageEnvelope publishRaw() {
        return baseEnvelope("PUBLISH", BridgeMessageEnvelope.PAYLOAD_KIND_RAW);
    }

    private BridgeMessageEnvelope lifecycle(String actionType) {
        return baseEnvelope(actionType, BridgeMessageEnvelope.PAYLOAD_KIND_RAW);
    }

    private BridgeMessageEnvelope baseEnvelope(String actionType, String payloadKind) {
        return BridgeMessageEnvelope.builder()
                .traceId("trace-rule-1")
                .productIdentification(PRODUCT_ID)
                .deviceIdentification(DEVICE_ID)
                .clientId("client-1")
                .actionType(actionType)
                .payloadKind(payloadKind)
                .topic("/v1/devices/" + DEVICE_ID + "/up")
                .ts(EVENT_UTC + 1)
                .eventUtc(EVENT_UTC)
                .build();
    }

    private MessageExt rawMessage() {
        MessageExt raw = new MessageExt();
        raw.setMsgId("msg-rule-1");
        return raw;
    }
}
