package com.mqttsnet.thinglinks.enumeration.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 桥接数据源协议类型（rule_data_source.source_type 字段）。
 * 与 {@code com.mqttsnet.basic.databridge.model.ConnectorType} 1:1 对齐 ──
 * thinglinks-util starter 拿到 source_type 字符串后通过 {@code ConnectorType.valueOf(...)}
 * 反查对应的 Sink/Source 实现。
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeSourceTypeEnum", description = "桥接数据源协议类型枚举")
public enum BridgeSourceTypeEnum {

    KAFKA("KAFKA", "Apache Kafka"),
    REDIS("REDIS", "Redis"),
    ROCKETMQ("ROCKETMQ", "Apache / 阿里云 RocketMQ"),
    RABBITMQ("RABBITMQ", "RabbitMQ"),
    MYSQL("MYSQL", "MySQL"),
    HTTP("HTTP", "HTTP RESTful"),
    WEBHOOK("WEBHOOK", "WebHook（HTTP + HMAC 签名）"),
    MQTT("MQTT", "外部 MQTT broker");

    private String value;
    private String desc;

    public static Optional<BridgeSourceTypeEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeSourceTypeEnum.values())
                        .filter(e -> e.getValue().equals(v))
                        .findFirst());
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
