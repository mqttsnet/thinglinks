package com.mqttsnet.thinglinks.mqs.event.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.common.event.bridge.BridgeMessageEnvelope;
import com.mqttsnet.thinglinks.mqs.bridge.MqsBridgeEventProducer;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("MQS 数据上报后置桥接处理器")
class DataReportBridgePostProcessorTest {

    private final MqsBridgeEventProducer producer = org.mockito.Mockito.mock(MqsBridgeEventProducer.class);
    private final DataReportBridgePostProcessor processor = new DataReportBridgePostProcessor(producer);

    @Test
    @DisplayName("验证物模型归一化结果会封装为北向桥接消息")
    void onReportedPublishesThingModelEnvelope() {
        when(producer.publishBridgeEvent(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        ProductResultVO normalized = ProductResultVO.builder()
            .appId("app-1")
            .productIdentification("product-1")
            .productName("Water Meter")
            .build();

        processor.onReported(DeviceDataReportContext.builder()
            .appId("app-1")
            .productIdentification("product-1")
            .deviceIdentification("device-1")
            .ts(1780000000000L)
            .normalized(normalized)
            .build());

        ArgumentCaptor<BridgeMessageEnvelope> captor = ArgumentCaptor.forClass(BridgeMessageEnvelope.class);
        verify(producer).publishBridgeEvent(captor.capture());
        BridgeMessageEnvelope envelope = captor.getValue();
        assertThat(envelope.getAppId()).isEqualTo("app-1");
        assertThat(envelope.getProductIdentification()).isEqualTo("product-1");
        assertThat(envelope.getDeviceIdentification()).isEqualTo("device-1");
        assertThat(envelope.getActionType()).isEqualTo(DeviceActionTypeEnum.PUBLISH.getValue());
        assertThat(envelope.getPayloadKind()).isEqualTo(BridgeMessageEnvelope.PAYLOAD_KIND_THING_MODEL);
        assertThat(envelope.getTs()).isEqualTo(1780000000000L);
        assertThat(envelope.getEventUtc()).isEqualTo(1780000000000L);
        assertThat(envelope.getRawMessage()).contains("\"productIdentification\":\"product-1\"");
    }

    @Test
    @DisplayName("验证桥接投递未被 RocketMQ 确认时不影响主流程")
    void onReportedSwallowsUnconfirmedProducerResult() {
        when(producer.publishBridgeEvent(org.mockito.ArgumentMatchers.any())).thenReturn(false);

        processor.onReported(DeviceDataReportContext.builder()
            .appId("app-1")
            .productIdentification("product-1")
            .deviceIdentification("device-1")
            .ts(1780000000000L)
            .normalized(ProductResultVO.builder().productIdentification("product-1").build())
            .build());

        verify(producer).publishBridgeEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证上下文为空或缺少归一化数据时不投递桥接消息")
    void onReportedSkipsNullContextOrMissingNormalizedData() {
        processor.onReported(null);
        processor.onReported(DeviceDataReportContext.builder()
            .deviceIdentification("device-1")
            .build());

        verify(producer, never()).publishBridgeEvent(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("验证桥接投递失败被吞掉，避免影响设备数据落库主流程")
    void onReportedSwallowsProducerFailure() {
        org.mockito.Mockito.doThrow(new IllegalStateException("mq down"))
            .when(producer).publishBridgeEvent(org.mockito.ArgumentMatchers.any());

        processor.onReported(DeviceDataReportContext.builder()
            .appId("app-1")
            .productIdentification("product-1")
            .deviceIdentification("device-1")
            .ts(1780000000000L)
            .normalized(ProductResultVO.builder().productIdentification("product-1").build())
            .build());

        verify(producer).publishBridgeEvent(org.mockito.ArgumentMatchers.any());
    }
}
