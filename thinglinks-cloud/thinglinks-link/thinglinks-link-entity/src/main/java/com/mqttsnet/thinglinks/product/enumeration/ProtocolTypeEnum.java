package com.mqttsnet.thinglinks.product.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>
 * 协议类型
 * </p>
 *
 * @author shihuan sun
 * @date 2023-04-14
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "ProtocolTypeEnum", description = "协议类型")
public enum ProtocolTypeEnum {

    /**
     * MQTT协议
     */
    MQTT("MQTT", "MQTT协议"),

    HTTP("HTTP", "HTTP协议"),

    TCP("TCP", "TCP协议"),

    WEBSOCKET("WEBSOCKET", "WEBSOCKET协议"),

    /**
     * 桥接入站(订阅源拉取的外部数据)──{@code SubscriptionSourceLifecycleManager} 用此标识。
     */
    BRIDGE_INGRESS("BRIDGE_INGRESS", "桥接入站协议(订阅源)");

    private String value;
    private String desc;

    public static Optional<ProtocolTypeEnum> fromValue(String value) {
        return Stream.of(ProtocolTypeEnum.values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
