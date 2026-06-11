package com.mqttsnet.thinglinks.video.enumeration.gb28181;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * SIP 信令传输协议枚举。
 * 定义 GB/T 28181 中 SIP 信令的传输方式，
 * 影响 Via 头域 transport 参数和 SipProvider 选择。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "SipTransportEnum", description = "SIP信令传输协议枚举")
public enum SipTransportEnum {

    /**
     * UDP 传输（默认）
     */
    UDP("UDP", "UDP传输"),

    /**
     * TCP 传输
     */
    TCP("TCP", "TCP传输"),
    ;

    private String value;
    private String desc;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 匹配的枚举实例
     */
    public static Optional<SipTransportEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }

    /**
     * 判断是否为 TCP 传输
     *
     * @return 是否为 TCP
     */
    public boolean isTcp() {
        return TCP.equals(this);
    }

    /**
     * 判断是否为 UDP 传输
     *
     * @return 是否为 UDP
     */
    public boolean isUdp() {
        return UDP.equals(this);
    }
}
