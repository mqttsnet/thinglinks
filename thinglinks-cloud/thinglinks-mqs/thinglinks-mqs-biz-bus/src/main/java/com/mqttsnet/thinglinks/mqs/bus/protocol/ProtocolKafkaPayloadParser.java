package com.mqttsnet.thinglinks.mqs.bus.protocol;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mqttsnet.thinglinks.common.constant.CommonIotConstants;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant.Mqs.MqsMqtt;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant.Mqs.MqsTcp;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant.Mqs.MqsWebSocket;
import com.mqttsnet.thinglinks.constants.bus.BusKafkaJsonField;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * → Kafka 上行 JSON → {@link DeviceProtocolEvent} 通用解析器。MQTT/WS/TCP 共用。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@UtilityClass
@Slf4j
public class ProtocolKafkaPayloadParser {

    private static final Map<String, DeviceActionTypeEnum> TOPIC_EVENT_MAP = buildTopicEventMap();

    /**
     * 解析 Kafka JSON 报文为 {@link DeviceProtocolEvent}。
     *
     * @param rawJson      Kafka 上行 JSON 报文
     * @param sourceTopic  来源 topic,用于推断 eventType
     * @param protocolType 协议类型
     * @return 解析得到的设备协议事件
     * @throws IllegalArgumentException rawJson 空或 protocolType 为 null
     */
    public DeviceProtocolEvent parse(String rawJson, String sourceTopic, ProtocolTypeEnum protocolType) {
        if (StrUtil.isBlank(rawJson)) {
            throw new IllegalArgumentException("[bus.parser] rawJson is blank");
        }
        if (protocolType == null) {
            throw new IllegalArgumentException("[bus.parser] protocolType is null");
        }
        JSONObject obj = JSON.parseObject(rawJson);
        // eventHlc / eventUtc 由上游接入层(MQTT 走 BifroMQ 插件、WS 走 broker)统一产出并随报文带上,
        // 此处协议中立地直读即可,无需按协议特判。
        DeviceProtocolEvent event = DeviceProtocolEvent.builder()
            .protocolType(protocolType.getValue())
            .eventType(inferEventType(sourceTopic, obj).getValue())
            .clientId(get(obj, CommonIotConstants.CLIENT_ID, Object::toString))
            .userId(get(obj, CommonIotConstants.USER_ID, Object::toString))
            .address(get(obj, BusKafkaJsonField.ADDRESS, Object::toString))
            .tenantId(get(obj, CommonIotConstants.TENANT_ID, Object::toString))
            .topic(get(obj, BusKafkaJsonField.TOPIC, Object::toString))
            .qos(parseInt(obj, BusKafkaJsonField.QOS))
            .encoding(get(obj, BusKafkaJsonField.ENCODING, Object::toString))
            .originalSize(parseInt(obj, BusKafkaJsonField.ORIGINAL_SIZE))
            .payloadHex(get(obj, BusKafkaJsonField.PAYLOAD_HEX, Object::toString))
            .eventHlc(parseLong(obj, CommonIotConstants.EVENT_HLC))
            .eventUtc(parseLong(obj, CommonIotConstants.EVENT_UTC))
            .rawMessage(rawJson)
            .ts(System.currentTimeMillis())
            .extension(extractExtension(obj))
            .build();
        Optional.ofNullable(get(obj, BusKafkaJsonField.PAYLOAD, Object::toString))
            .filter(StrUtil::isNotBlank)
            .ifPresent(event::setPayload);
        return event;
    }

    /**
     * JSON eventType 显式 → topic 查表 → PUBLISH 兜底。
     *
     * @param sourceTopic 来源 topic
     * @param obj         上行 JSON 对象
     * @return 推断出的事件类型
     */
    private DeviceActionTypeEnum inferEventType(String sourceTopic, JSONObject obj) {
        return Optional.ofNullable(get(obj, CommonIotConstants.EVENT_TYPE, Object::toString))
            .filter(StrUtil::isNotBlank)
            .flatMap(DeviceActionTypeEnum::fromValue)
            .or(() -> Optional.ofNullable(sourceTopic)
                .filter(StrUtil::isNotBlank)
                .map(TOPIC_EVENT_MAP::get))
            .orElse(DeviceActionTypeEnum.PUBLISH);
    }

    /**
     * topic → eventType 启动期一次性查表。三协议各自独立装配,改一个协议不影响其它协议。
     *
     * @return topic 到事件类型的不可变映射
     */
    private static Map<String, DeviceActionTypeEnum> buildTopicEventMap() {
        Map<String, DeviceActionTypeEnum> m = new HashMap<>(40);
        putMqttTopicEvents(m);
        putWebSocketTopicEvents(m);
        putTcpTopicEvents(m);
        return Map.copyOf(m);
    }

    /** MQTT topic → 动作(仅 MQTT;改这里不影响 WS/TCP)。 */
    private static void putMqttTopicEvents(Map<String, DeviceActionTypeEnum> m) {
        m.put(MqsMqtt.THINGLINKS_MQTT_CLIENT_CONNECTED_TOPIC, DeviceActionTypeEnum.CONNECT);
        m.put(MqsMqtt.THINGLINKS_MQTT_CLIENT_DISCONNECTED_TOPIC, DeviceActionTypeEnum.DISCONNECT);
        m.put(MqsMqtt.THINGLINKS_MQTT_SERVER_CONNECTED_TOPIC, DeviceActionTypeEnum.CLOSE);
        m.put(MqsMqtt.THINGLINKS_MQTT_DEVICE_KICKED_TOPIC, DeviceActionTypeEnum.KICKED);
        m.put(MqsMqtt.THINGLINKS_MQTT_SUBSCRIPTION_ACKED_TOPIC, DeviceActionTypeEnum.SUBSCRIBE);
        m.put(MqsMqtt.THINGLINKS_MQTT_UNSUBSCRIPTION_ACKED_TOPIC, DeviceActionTypeEnum.UNSUBSCRIBE);
        m.put(MqsMqtt.THINGLINKS_MQTT_PING_REQ_TOPIC, DeviceActionTypeEnum.PING);
        // BifroMQ Standalone 部署:plugin DISTED → distribution.completed.topic 是设备 PUBLISH 唯一可用来源
        // (broker 无内置 mqtt-to-kafka connector);DISTED body 带完整 PUBLISH 报文(topic/qos/payload/publisher).
        // 注意:DISTED 上下行都触发 ── 下行命令(backend publisher)走到这里时 DeviceCacheEnricher 会 cache miss,
        // 后续 PayloadDecodeStage 无物模型自然 skip,实现天然过滤.
        m.put(MqsMqtt.THINGLINKS_MQTT_DISTRIBUTION_COMPLETED_TOPIC, DeviceActionTypeEnum.PUBLISH);
        m.put(MqsMqtt.THINGLINKS_MQTT_DISTRIBUTION_ERROR_TOPIC, DeviceActionTypeEnum.DISPATCH_ERROR);
    }

    /**
     * WebSocket topic → 动作(仅 WS;改这里不影响 MQTT/TCP)。
     * 只登记 broker WS 端点实际产出的动作:CONNECT / DISCONNECT / PING / PUBLISH / DISPATCH_ERROR;
     * CLOSE(服务端关)/ KICKED(踢线)作生命周期预留,待 broker 接线产出后即生效;
     * WS 无订阅模型,故不设 SUBSCRIBE / UNSUBSCRIBE。
     */
    private static void putWebSocketTopicEvents(Map<String, DeviceActionTypeEnum> m) {
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_CLIENT_CONNECTED_TOPIC, DeviceActionTypeEnum.CONNECT);
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_CLIENT_DISCONNECTED_TOPIC, DeviceActionTypeEnum.DISCONNECT);
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_SERVER_DISCONNECTED_TOPIC, DeviceActionTypeEnum.CLOSE);
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_DEVICE_KICKED_TOPIC, DeviceActionTypeEnum.KICKED);
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_PING_REQ_TOPIC, DeviceActionTypeEnum.PING);
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_DISTRIBUTION_COMPLETED_TOPIC, DeviceActionTypeEnum.PUBLISH);
        m.put(MqsWebSocket.THINGLINKS_WEBSOCKET_DISTRIBUTION_ERROR_TOPIC, DeviceActionTypeEnum.DISPATCH_ERROR);
    }

    /** TCP topic → 动作(仅 TCP;改这里不影响 MQTT/WS)。 */
    private static void putTcpTopicEvents(Map<String, DeviceActionTypeEnum> m) {
        m.put(MqsTcp.THINGLINKS_TCP_CLIENT_CONNECTED_TOPIC, DeviceActionTypeEnum.CONNECT);
        m.put(MqsTcp.THINGLINKS_TCP_CLIENT_DISCONNECTED_TOPIC, DeviceActionTypeEnum.DISCONNECT);
        m.put(MqsTcp.THINGLINKS_TCP_SERVER_DISCONNECTED_TOPIC, DeviceActionTypeEnum.CLOSE);
        m.put(MqsTcp.THINGLINKS_TCP_DEVICE_KICKED_TOPIC, DeviceActionTypeEnum.KICKED);
        m.put(MqsTcp.THINGLINKS_TCP_SUBSCRIPTION_ACKED_TOPIC, DeviceActionTypeEnum.SUBSCRIBE);
        m.put(MqsTcp.THINGLINKS_TCP_UNSUBSCRIPTION_ACKED_TOPIC, DeviceActionTypeEnum.UNSUBSCRIBE);
        m.put(MqsTcp.THINGLINKS_TCP_PING_REQ_TOPIC, DeviceActionTypeEnum.PING);
        m.put(MqsTcp.THINGLINKS_TCP_DISTRIBUTION_COMPLETED_TOPIC, DeviceActionTypeEnum.PUBLISH);
        m.put(MqsTcp.THINGLINKS_TCP_DISTRIBUTION_ERROR_TOPIC, DeviceActionTypeEnum.DISPATCH_ERROR);
    }

    /**
     * 抽取协议级非核心字段进 extension。
     *
     * @param obj 上行 JSON 对象
     * @return 协议级扩展字段映射
     */
    private Map<String, Object> extractExtension(JSONObject obj) {
        Map<String, Object> ext = new LinkedHashMap<>(BusKafkaJsonField.EXTENSION_KEYS.size());
        BusKafkaJsonField.EXTENSION_KEYS.forEach(key ->
            Optional.ofNullable(obj.getOrDefault(key, null)).ifPresent(v -> ext.put(key, v)));
        return ext;
    }

    /**
     * 通用安全提取:null 返 null,异常返 null。
     *
     * @param obj    JSON 对象
     * @param key    字段键
     * @param mapper 值转换函数
     * @param <T>    返回值类型
     * @return 转换后的值;字段缺失或转换异常返回 null
     */
    private static <T> T get(JSONObject obj, String key, Function<Object, T> mapper) {
        Object v = obj.getOrDefault(key, null);
        if (v == null) {
            return null;
        }
        try {
            return mapper.apply(v);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Integer parseInt(JSONObject obj, String key) {
        return get(obj, key, v -> Integer.parseInt(v.toString().trim()));
    }

    private static Long parseLong(JSONObject obj, String key) {
        return get(obj, key, v -> Long.parseLong(v.toString().trim()));
    }
}
