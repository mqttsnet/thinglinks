package com.mqttsnet.thinglinks.enumeration.bridge;

import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 入站订阅源处理方式（rule_subscription_source.target_handler 字段）。
 * 决定了入站消息如何还原成平台内部事件。
 * </p>
 *
 * <p>枚举的 {@code value} 直接引用 {@link BizMqRouteConstant.Tags} 中的同名常量,
 * 与 RocketMQ tag 保持单一来源 ── 业务字段(target_handler) 与消息路由 tag 永不脱节。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "BridgeTargetHandlerEnum", description = "入站处理方式枚举")
public enum BridgeTargetHandlerEnum {

    /**
     * 伪装成设备 publish ── 走 mqs 协议链路完整解析（含物模型）。
     */
    MQTT_FORWARD(BizMqRouteConstant.Tags.INGRESS_MQTT_FORWARD, "伪装设备publish"),

    /**
     * 直接写 DeviceAction 表 ── 跳过物模型解析。
     */
    RAW_INSERT(BizMqRouteConstant.Tags.INGRESS_RAW_INSERT, "直接写DeviceAction"),

    /**
     * 触发场景联动 ── 调 rule-api 触发联动规则。
     */
    RULE_TRIGGER(BizMqRouteConstant.Tags.INGRESS_RULE_TRIGGER, "触发场景联动");

    private String value;
    private String desc;

    public static Optional<BridgeTargetHandlerEnum> fromValue(String value) {
        return Optional.ofNullable(value)
                .flatMap(v -> Stream.of(BridgeTargetHandlerEnum.values())
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
