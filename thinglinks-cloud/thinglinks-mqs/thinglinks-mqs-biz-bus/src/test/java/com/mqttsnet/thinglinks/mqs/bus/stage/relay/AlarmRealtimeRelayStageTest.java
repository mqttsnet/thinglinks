package com.mqttsnet.thinglinks.mqs.bus.stage.relay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;

@DisplayName("MQS 告警实时转发阶段")
class AlarmRealtimeRelayStageTest {

    private final BusStatsService statsService = org.mockito.Mockito.mock(BusStatsService.class);

    @Test
    @DisplayName("验证 RocketMQ 模板不存在时跳过转发并记录 skip-no-template")
    void executeSkipsWhenRocketmqTemplateMissing() {
        AlarmRealtimeRelayStage stage = new AlarmRealtimeRelayStage(provider(null), statsService);
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.PUBLISH.getValue());

        stage.execute(event, StageContext.create(event));

        verify(statsService).incrementRelay("AlarmRealtimeRelayStage", "rocketmq",
            BizMqRouteConstant.Alarm.REALTIME, "skip-no-template");
    }

    @Test
    @DisplayName("验证设备事件按产品 tag 转发到告警实时 topic 并记录异步成功")
    void executeSendsToProductTagAndRecordsCallbackSuccess() {
        RocketmqTemplate template = org.mockito.Mockito.mock(RocketmqTemplate.class);
        AlarmRealtimeRelayStage stage = new AlarmRealtimeRelayStage(provider(template), statsService);
        DeviceProtocolEvent event = event("publish");

        stage.execute(event, StageContext.create(event));

        ArgumentCaptor<SendCallback> callbackCaptor = ArgumentCaptor.forClass(SendCallback.class);
        verify(template).asyncSend(org.mockito.ArgumentMatchers.eq(BizMqRouteConstant.Alarm.REALTIME + ":product-1"),
            org.mockito.ArgumentMatchers.eq(event), callbackCaptor.capture());
        SendResult sendResult = org.mockito.Mockito.mock(SendResult.class);
        callbackCaptor.getValue().onSuccess(sendResult);
        verify(statsService).incrementRelay("AlarmRealtimeRelayStage", "rocketmq",
            BizMqRouteConstant.Alarm.REALTIME, "ok");
    }

    @Test
    @DisplayName("验证异步发送回调失败时记录告警转发失败")
    void executeRecordsAsyncCallbackFailure() {
        RocketmqTemplate template = org.mockito.Mockito.mock(RocketmqTemplate.class);
        AlarmRealtimeRelayStage stage = new AlarmRealtimeRelayStage(provider(template), statsService);
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.ERROR.getValue());

        stage.execute(event, StageContext.create(event));

        ArgumentCaptor<SendCallback> callbackCaptor = ArgumentCaptor.forClass(SendCallback.class);
        verify(template).asyncSend(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(event), callbackCaptor.capture());
        callbackCaptor.getValue().onException(new IllegalStateException("send failed"));
        verify(statsService).incrementRelay("AlarmRealtimeRelayStage", "rocketmq",
            BizMqRouteConstant.Alarm.REALTIME, "fail");
    }

    @Test
    @DisplayName("验证调用 asyncSend 同步抛错时记录 fail-sync")
    void executeRecordsSyncInvocationFailure() {
        RocketmqTemplate template = org.mockito.Mockito.mock(RocketmqTemplate.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("name server down"))
            .when(template).asyncSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        AlarmRealtimeRelayStage stage = new AlarmRealtimeRelayStage(provider(template), statsService);
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.DISPATCH_ERROR.getValue());

        stage.execute(event, StageContext.create(event));

        verify(statsService).incrementRelay("AlarmRealtimeRelayStage", "rocketmq",
            BizMqRouteConstant.Alarm.REALTIME, "fail-sync");
    }

    @Test
    @DisplayName("验证告警实时转发只覆盖 PUBLISH、ERROR、DISPATCH_ERROR")
    void supportsPublishErrorAndDispatchErrorCaseInsensitively() {
        AlarmRealtimeRelayStage stage = new AlarmRealtimeRelayStage(provider(null), statsService);

        assertThat(stage.supports(event("publish"))).isTrue();
        assertThat(stage.supports(event("error"))).isTrue();
        assertThat(stage.supports(event("dispatch_error"))).isTrue();
        assertThat(stage.supports(event(DeviceActionTypeEnum.PING.getValue()))).isFalse();
    }

    private static DeviceProtocolEvent event(String actionType) {
        return DeviceProtocolEvent.builder()
            .traceId("trace-1")
            .eventType(actionType)
            .productIdentification("product-1")
            .build();
    }

    private static ObjectProvider<RocketmqTemplate> provider(RocketmqTemplate template) {
        return new ObjectProvider<>() {
            @Override
            public RocketmqTemplate getObject(Object... args) throws BeansException {
                return template;
            }

            @Override
            public RocketmqTemplate getIfAvailable() throws BeansException {
                return template;
            }

            @Override
            public RocketmqTemplate getIfUnique() throws BeansException {
                return template;
            }

            @Override
            public RocketmqTemplate getObject() throws BeansException {
                return template;
            }

            @Override
            public Iterator<RocketmqTemplate> iterator() {
                return template == null ? java.util.Collections.emptyIterator()
                    : java.util.List.of(template).iterator();
            }
        };
    }
}
