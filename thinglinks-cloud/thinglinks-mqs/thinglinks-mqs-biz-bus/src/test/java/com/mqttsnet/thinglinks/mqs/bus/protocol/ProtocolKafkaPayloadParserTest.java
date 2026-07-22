package com.mqttsnet.thinglinks.mqs.bus.protocol;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.constants.bus.BusKafkaJsonField;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS Kafka 原始载荷解析")
class ProtocolKafkaPayloadParserTest {

    @Test
    @DisplayName("验证显式 eventType 会统一归一化大小写并保留原始扩展字段")
    void parseUsesExplicitEventTypeAndNormalizesCase() {
        Map<String, Object> json = basePayload();
        json.put(CommonIotConstants.EVENT_TYPE, "publish");
        json.put(BusKafkaJsonField.QOS, "1");
        json.put(BusKafkaJsonField.ORIGINAL_SIZE, "8");
        json.put(CommonIotConstants.EVENT_HLC, "9900");
        json.put(CommonIotConstants.EVENT_UTC, "1780000000000");
        json.put("messageId", "mid-1");

        DeviceProtocolEvent event = ProtocolKafkaPayloadParser.parse(JSON.toJSONString(json),
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC,
            ProtocolTypeEnum.MQTT);

        assertThat(event.getProtocolType()).isEqualTo(ProtocolTypeEnum.MQTT.getValue());
        assertThat(event.getEventType()).isEqualTo(DeviceActionTypeEnum.PUBLISH.getValue());
        assertThat(event.getClientId()).isEqualTo("client-1");
        assertThat(event.getTenantId()).isEqualTo("1");
        assertThat(event.getTopic()).isEqualTo("/v1/devices/device-1/datas");
        assertThat(event.getQos()).isEqualTo(1);
        assertThat(event.getOriginalSize()).isEqualTo(8);
        assertThat(event.getEventHlc()).isEqualTo(9900L);
        assertThat(event.getEventUtc()).isEqualTo(1780000000000L);
        assertThat(event.getRawMessage()).isEqualTo(JSON.toJSONString(json));
        assertThat(event.getExtension()).containsEntry("messageId", "mid-1");
    }

    @Test
    @DisplayName("验证缺少 eventType 时可根据生命周期 topic 推断动作")
    void parseInfersLifecycleActionFromSourceTopicWhenEventTypeMissing() {
        DeviceProtocolEvent event = ProtocolKafkaPayloadParser.parse(JSON.toJSONString(basePayload()),
            KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC,
            ProtocolTypeEnum.MQTT);

        assertThat(event.getEventType()).isEqualTo(DeviceActionTypeEnum.CONNECT.getValue());
    }

    @Test
    @DisplayName("验证未知来源 topic 默认按 PUBLISH 事件进入上行链路")
    void parseFallsBackToPublishForUnknownSourceTopic() {
        DeviceProtocolEvent event = ProtocolKafkaPayloadParser.parse(JSON.toJSONString(basePayload()),
            "unknown.topic",
            ProtocolTypeEnum.WEBSOCKET);

        assertThat(event.getEventType()).isEqualTo(DeviceActionTypeEnum.PUBLISH.getValue());
        assertThat(event.getProtocolType()).isEqualTo(ProtocolTypeEnum.WEBSOCKET.getValue());
    }

    @Test
    @DisplayName("验证空 JSON 或缺少协议类型时提前拒绝，避免后续空指针")
    void parseRejectsBlankJsonOrMissingProtocol() {
        assertThatThrownBy(() -> ProtocolKafkaPayloadParser.parse(" ", "topic", ProtocolTypeEnum.MQTT))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("rawJson is blank");
        assertThatThrownBy(() -> ProtocolKafkaPayloadParser.parse("{}", "topic", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("protocolType is null");
    }

    private static Map<String, Object> basePayload() {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put(CommonIotConstants.CLIENT_ID, "client-1");
        json.put(CommonIotConstants.USER_ID, "user-1");
        json.put(CommonIotConstants.TENANT_ID, "1");
        json.put(BusKafkaJsonField.ADDRESS, "/127.0.0.1:1883");
        json.put(BusKafkaJsonField.TOPIC, "/v1/devices/device-1/datas");
        json.put(BusKafkaJsonField.PAYLOAD, "e30=");
        json.put(BusKafkaJsonField.PAYLOAD_HEX, "7b7d");
        json.put(BusKafkaJsonField.ENCODING, "BASE64");
        return json;
    }
}
