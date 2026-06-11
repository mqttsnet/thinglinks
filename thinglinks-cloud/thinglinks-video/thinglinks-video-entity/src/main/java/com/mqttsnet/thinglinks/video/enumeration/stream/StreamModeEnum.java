package com.mqttsnet.thinglinks.video.empowerment.stream;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author mqttsnet
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "StreamModeEnum", description = "数据流传输模式枚举")
public enum StreamModeEnum {

    UDP("UDP", "udp传输"),
    TCP_ACTIVE("TCP_ACTIVE", "tcp主动模式"),
    TCP_PASSIVE("TCP_PASSIVE", "tcp被动模式"),
    ;


    private String value;
    private String desc;


    /**
     * 根据value获取对应的枚举
     *
     * @param value 产生源类型的标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<StreamModeEnum> fromValue(String value) {
        return Stream.of(StreamModeEnum.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
