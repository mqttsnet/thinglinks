package com.mqttsnet.thinglinks.video.enumeration.jt1078;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * JT/T 1078 终端设备状态枚举。
 * 用于标识车载终端的在线状态。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Jt1078DeviceStatusEnum", description = "JT/T 1078终端设备状态枚举")
public enum Jt1078DeviceStatusEnum {

    /**
     * 在线
     */
    ONLINE("ONLINE", "在线"),

    /**
     * 离线
     */
    OFFLINE("OFFLINE", "离线"),

    /**
     * 未注册
     */
    UNREGISTERED("UNREGISTERED", "未注册"),
    ;

    /**
     * 状态标识
     */
    private String value;

    /**
     * 中文描述
     */
    private String description;

    /**
     * 根据 value 查找枚举（忽略大小写）。
     *
     * @param value 状态标识值
     * @return 匹配的枚举实例，未匹配时返回 {@link Optional#empty()}
     */
    public static Optional<Jt1078DeviceStatusEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
