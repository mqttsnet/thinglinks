package com.mqttsnet.thinglinks.mqs.bridge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("MQS 设备事件 RocketMQ 投递器")
class MqsBridgeEventProducerTest {

    @Test
    @DisplayName("验证 broker 返回 SEND_OK 时才判定投递成功")
    void publishBridgeEventReturnsTrueOnlyWhenBrokerConfirmsSendOk() {
        RocketmqTemplate template = mock(RocketmqTemplate.class);
        MqsBridgeEventProducer producer = producer(template, 100L, 2);
        BridgeMessageEnvelope envelope = envelope("PING");
        SendResult result = new SendResult();
        result.setSendStatus(SendStatus.SEND_OK);
        result.setMsgId("msg-1");
        when(template.syncSend(eq(destination("PING")), same(envelope), eq(100L))).thenReturn(result);

        boolean sent = producer.publishBridgeEvent(envelope);

        assertThat(sent).isTrue();
        verify(template, times(1)).syncSend(eq(destination("PING")), same(envelope), eq(100L));
    }

    @Test
    @DisplayName("验证 RocketMQ 超时会按配置重试，耗尽后返回失败")
    void publishBridgeEventRetriesAndReturnsFalseWhenRocketMqTimesOut() {
        RocketmqTemplate template = mock(RocketmqTemplate.class);
        MqsBridgeEventProducer producer = producer(template, 100L, 2);
        BridgeMessageEnvelope envelope = envelope("DISCONNECT");
        when(template.syncSend(eq(destination("DISCONNECT")), same(envelope), eq(100L)))
            .thenThrow(new IllegalStateException("wait response timeout"));

        boolean sent = producer.publishBridgeEvent(envelope);

        assertThat(sent).isFalse();
        verify(template, times(2)).syncSend(eq(destination("DISCONNECT")), same(envelope), eq(100L));
    }

    @Test
    @DisplayName("验证未装配 RocketMQTemplate 时直接返回失败且不触发发送")
    void publishBridgeEventReturnsFalseWhenTemplateUnavailable() {
        ObjectProvider<RocketmqTemplate> provider = mockProvider(null);
        MqsBridgeEventProducer producer = new MqsBridgeEventProducer(provider);

        boolean sent = producer.publishBridgeEvent(envelope("PING"));

        assertThat(sent).isFalse();
        verify(provider).getIfAvailable();
    }

    @Test
    @DisplayName("验证 broker 返回非 SEND_OK 时不会误判成功")
    void publishBridgeEventReturnsFalseWhenSendStatusIsNotOk() {
        RocketmqTemplate template = mock(RocketmqTemplate.class);
        MqsBridgeEventProducer producer = producer(template, 100L, 1);
        BridgeMessageEnvelope envelope = envelope("PING");
        SendResult result = new SendResult();
        result.setSendStatus(SendStatus.FLUSH_DISK_TIMEOUT);
        when(template.syncSend(eq(destination("PING")), same(envelope), eq(100L))).thenReturn(result);

        boolean sent = producer.publishBridgeEvent(envelope);

        assertThat(sent).isFalse();
        verify(template, times(1)).syncSend(eq(destination("PING")), same(envelope), eq(100L));
    }

    @Test
    @DisplayName("验证空消息会被跳过，不访问 RocketMQ")
    void publishBridgeEventSkipsNullEnvelope() {
        RocketmqTemplate template = mock(RocketmqTemplate.class);
        MqsBridgeEventProducer producer = producer(template, 100L, 1);

        boolean sent = producer.publishBridgeEvent(null);

        assertThat(sent).isFalse();
        verify(template, never()).syncSend(any(), any(), eq(100L));
    }

    private static MqsBridgeEventProducer producer(RocketmqTemplate template, long timeoutMs, int maxAttempts) {
        MqsBridgeEventProducer producer = new MqsBridgeEventProducer(mockProvider(template));
        ReflectionTestUtils.setField(producer, "sendTimeoutMs", timeoutMs);
        ReflectionTestUtils.setField(producer, "sendMaxAttempts", maxAttempts);
        return producer;
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<RocketmqTemplate> mockProvider(RocketmqTemplate template) {
        ObjectProvider<RocketmqTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(template);
        return provider;
    }

    private static BridgeMessageEnvelope envelope(String actionType) {
        return BridgeMessageEnvelope.builder()
            .traceId("trace-1")
            .tenantId("1")
            .clientId("client-1")
            .actionType(actionType)
            .productIdentification("product-1")
            .deviceIdentification("device-1")
            .build();
    }

    private static String destination(String actionType) {
        return BizMqRouteConstant.Bridge.DEVICE_EVENT + ":" + actionType;
    }
}
