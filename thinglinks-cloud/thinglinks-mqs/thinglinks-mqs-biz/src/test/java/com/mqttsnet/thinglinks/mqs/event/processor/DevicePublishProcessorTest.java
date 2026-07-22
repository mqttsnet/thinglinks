package com.mqttsnet.thinglinks.mqs.event.processor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.uplink.source.UplinkMessageEventSource;
import com.mqttsnet.thinglinks.mqs.transform.InboundScriptTransformer;
import com.mqttsnet.thinglinks.mqs.uplink.handler.TopicHandler;
import com.mqttsnet.thinglinks.mqs.uplink.handler.factory.TopicHandlerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 设备数据上报处理器")
class DevicePublishProcessorTest {

    private final TopicHandlerFactory topicHandlerFactory = mock(TopicHandlerFactory.class);
    private final InboundScriptTransformer transformer = mock(InboundScriptTransformer.class);
    private final DevicePublishProcessor processor = new DevicePublishProcessor(topicHandlerFactory, transformer);

    @Test
    @DisplayName("验证处理器只接收 PUBLISH 数据上行动作")
    void supportsOnlyPublishAction() {
        org.assertj.core.api.Assertions.assertThat(processor.supports(DeviceActionTypeEnum.PUBLISH)).isTrue();
        org.assertj.core.api.Assertions.assertThat(processor.supports(DeviceActionTypeEnum.PING)).isFalse();
    }

    @Test
    @DisplayName("验证 topic 为空时不执行脚本转换和 topic handler 匹配")
    void processSkipsBlankTopicWithoutTransforming() {
        processor.process(event(" "));

        verify(transformer, never()).resolveEventSource(org.mockito.ArgumentMatchers.any());
        verify(topicHandlerFactory, never()).findMatchingHandler(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("验证上行脚本转换后的 topic 能匹配并调用对应 handler")
    void processUsesTransformedSourceAndMatchingHandler() {
        CommonDeviceEvent event = event("/vendor/raw");
        UplinkMessageEventSource source = UplinkMessageEventSource.builder()
            .topic("/v1/devices/device-1/datas")
            .payload("{\"temperature\":24}")
            .qos("1")
            .build();
        TopicHandler handler = mock(TopicHandler.class);
        when(transformer.resolveEventSource(event)).thenReturn(source);
        when(topicHandlerFactory.findMatchingHandler(source.getTopic())).thenReturn(handler);

        processor.process(event);

        verify(handler).handle(source);
    }

    @Test
    @DisplayName("验证转换成功但无 handler 匹配时安全跳过")
    void processSkipsWhenNoHandlerMatched() {
        CommonDeviceEvent event = event("/vendor/raw");
        UplinkMessageEventSource source = UplinkMessageEventSource.builder()
            .topic("/vendor/raw")
            .payload("{}")
            .build();
        when(transformer.resolveEventSource(event)).thenReturn(source);
        when(topicHandlerFactory.findMatchingHandler(source.getTopic())).thenReturn(null);

        processor.process(event);

        verify(topicHandlerFactory).findMatchingHandler(source.getTopic());
    }

    private static CommonDeviceEvent event(String topic) {
        return CommonDeviceEvent.builder()
            .actionType(DeviceActionTypeEnum.PUBLISH)
            .clientId("client-1")
            .topic(topic)
            .qos(1)
            .payload("{}")
            .build();
    }
}
