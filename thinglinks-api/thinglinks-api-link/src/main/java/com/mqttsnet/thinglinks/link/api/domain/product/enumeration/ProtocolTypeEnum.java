package com.mqttsnet.thinglinks.link.api.domain.product.enumeration;

import io.swagger.annotations.ApiModel;
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
@ApiModel(value = "ProtocolTypeEnum", description = "协议类型")
public enum ProtocolTypeEnum {

    /**
     * MQTT协议
     */
    MQTT("MQTT", "MQTT协议"),

    HTTP("HTTP", "HTTP协议"),

    TCP("TCP", "TCP协议"),

    WEBSOCKET("WEBSOCKET", "WEBSOCKET协议")

    ;

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
