package com.mqttsnet.thinglinks.video.empowerment.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Description:
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/22
 */
@Getter
@Schema(title = "TransportProtocolEnum", description = "传输协议")
public enum TransportProtocolEnum {
    TCP("TCP", "TCP"),
    UDP("UDP", "UDP");

    private final String value;
    private final String desc;

    TransportProtocolEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static Optional<TransportProtocolEnum> fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
