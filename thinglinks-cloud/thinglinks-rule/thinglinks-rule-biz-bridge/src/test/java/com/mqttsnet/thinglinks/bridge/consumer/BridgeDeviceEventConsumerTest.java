package com.mqttsnet.thinglinks.bridge.consumer;

import java.util.Collections;
import java.util.List;

import com.mqttsnet.thinglinks.bridge.dispatcher.SinkDispatcher;
import com.mqttsnet.thinglinks.bridge.matcher.BridgeRuleMatcher;
import com.mqttsnet.thinglinks.cache.vo.bridge.DataBridgeCacheVO;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("桥接设备事件消费者")
class BridgeDeviceEventConsumerTest {

    private static final String TRACE_ID = "trace-bridge-1";

    @Mock
    private BridgeRuleMatcher matcher;
    @Mock
    private SinkDispatcher dispatcher;
    @InjectMocks
    private BridgeDeviceEventConsumer consumer;

    @Test
    @DisplayName("收到空信封时只记录日志，不进入规则匹配")
    void onTenantMessageShouldSkipNullEnvelope() {
        consumer.onTenantMessage(null, rawMessage());

        verifyNoInteractions(matcher, dispatcher);
    }

    @Test
    @DisplayName("消费到设备事件但没有命中桥接规则时，不执行下游派发")
    void onTenantMessageShouldNotDispatchWhenNoBridgeRuleMatched() {
        BridgeMessageEnvelope envelope = envelope();
        when(matcher.matchOutbound(envelope)).thenReturn(Collections.emptyList());

        consumer.onTenantMessage(envelope, rawMessage());

        verify(matcher).matchOutbound(envelope);
        verifyNoInteractions(dispatcher);
    }

    @Test
    @DisplayName("消费到设备事件且命中多条桥接规则时，逐条派发并传递匹配耗时")
    void onTenantMessageShouldDispatchEveryMatchedRule() {
        BridgeMessageEnvelope envelope = envelope();
        DataBridgeCacheVO firstRule = DataBridgeCacheVO.builder().id(1L).ruleName("first").build();
        DataBridgeCacheVO secondRule = DataBridgeCacheVO.builder().id(2L).ruleName("second").build();
        when(matcher.matchOutbound(envelope)).thenReturn(List.of(firstRule, secondRule));

        consumer.onTenantMessage(envelope, rawMessage());

        verify(dispatcher).dispatch(eq(firstRule), eq(envelope), anyLong());
        verify(dispatcher).dispatch(eq(secondRule), eq(envelope), anyLong());
    }

    @Test
    @DisplayName("桥接派发异常时继续抛出，让 RocketMQ 按消费失败重试")
    void onTenantMessageShouldRethrowDispatchExceptionForRocketMqRetry() {
        BridgeMessageEnvelope envelope = envelope();
        DataBridgeCacheVO rule = DataBridgeCacheVO.builder().id(1L).ruleName("failed").build();
        when(matcher.matchOutbound(envelope)).thenReturn(List.of(rule));
        doThrow(new IllegalStateException("dispatch failed"))
                .when(dispatcher).dispatch(eq(rule), eq(envelope), anyLong());

        assertThrows(IllegalStateException.class, () -> consumer.onTenantMessage(envelope, rawMessage()));

        verify(matcher).matchOutbound(envelope);
    }

    @Test
    @DisplayName("匹配器异常时不吞掉异常，避免 RocketMQ 误判消费成功")
    void onTenantMessageShouldRethrowMatcherExceptionForRocketMqRetry() {
        BridgeMessageEnvelope envelope = envelope();
        when(matcher.matchOutbound(envelope)).thenThrow(new IllegalStateException("match failed"));

        assertThrows(IllegalStateException.class, () -> consumer.onTenantMessage(envelope, rawMessage()));

        verifyNoInteractions(dispatcher);
    }

    private BridgeMessageEnvelope envelope() {
        return BridgeMessageEnvelope.builder()
                .traceId(TRACE_ID)
                .appId("app-1")
                .productIdentification("product-1")
                .deviceIdentification("device-1")
                .clientId("client-1")
                .actionType("PUBLISH")
                .payloadKind(BridgeMessageEnvelope.PAYLOAD_KIND_RAW)
                .topic("/v1/devices/device-1/up")
                .build();
    }

    private MessageExt rawMessage() {
        MessageExt raw = new MessageExt();
        raw.setMsgId("msg-bridge-1");
        return raw;
    }
}
