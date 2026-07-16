package com.mqttsnet.thinglinks.mqs.event.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 统一设备事件组装器")
class CommonDeviceEventAssemblerTest {

    private final CommonDeviceEventAssembler assembler = new CommonDeviceEventAssembler();

    @Test
    @DisplayName("验证协议事件可映射为统一设备事件，并归一化协议和动作大小写")
    void assembleMapsProtocolEventToCommonDeviceEventAndNormalizesActionCase() {
        DeviceProtocolEvent protocolEvent = DeviceProtocolEvent.builder()
            .protocolType("mqtt")
            .eventType("publish")
            .tenantId("1")
            .appId("app-1")
            .clientId("client-1")
            .deviceIdentification("device-1")
            .productIdentification("product-1")
            .topic("/v1/devices/device-1/datas")
            .qos(1)
            .payload("e30=")
            .payloadHex("7b7d")
            .encoding("BASE64")
            .originalSize(2)
            .eventUtc(1780000000000L)
            .eventHlc(9000L)
            .rawMessage("{}")
            .extension(Map.of("frameId", "f-1"))
            .build();

        Optional<CommonDeviceEvent> result = assembler.assemble(protocolEvent);

        assertThat(result).isPresent();
        CommonDeviceEvent event = result.get();
        assertThat(event.getProtocolType()).isEqualTo(ProtocolTypeEnum.MQTT.getValue());
        assertThat(event.getActionType()).isEqualTo(DeviceActionTypeEnum.PUBLISH);
        assertThat(event.getTenantId()).isEqualTo("1");
        assertThat(event.getClientId()).isEqualTo("client-1");
        assertThat(event.getDeviceIdentification()).isEqualTo("device-1");
        assertThat(event.getProductIdentification()).isEqualTo("product-1");
        assertThat(event.getTopic()).isEqualTo("/v1/devices/device-1/datas");
        assertThat(event.getQos()).isEqualTo(1);
        assertThat(event.getPayload()).isEqualTo("e30=");
        assertThat(event.getPayloadHex()).isEqualTo("7b7d");
        assertThat(event.getEncoding()).isEqualTo("BASE64");
        assertThat(event.getOriginalSize()).isEqualTo(2);
        assertThat(event.getTs()).isEqualTo(1780000000000L);
        assertThat(event.getEventUtc()).isEqualTo(1780000000000L);
        assertThat(event.getEventHlc()).isEqualTo(9000L);
        assertThat(event.getRawMessage()).isEqualTo("{}");
    }

    @Test
    @DisplayName("验证缺少必要字段或未知动作时不生成业务事件")
    void assembleReturnsEmptyForMissingRequiredFieldsOrUnknownAction() {
        assertThat(assembler.assemble(null)).isEmpty();
        assertThat(assembler.assemble(DeviceProtocolEvent.builder()
            .eventType(DeviceActionTypeEnum.CONNECT.getValue())
            .build())).isEmpty();
        assertThat(assembler.assemble(DeviceProtocolEvent.builder()
            .tenantId("1")
            .eventType("not-exists")
            .build())).isEmpty();
    }
}
