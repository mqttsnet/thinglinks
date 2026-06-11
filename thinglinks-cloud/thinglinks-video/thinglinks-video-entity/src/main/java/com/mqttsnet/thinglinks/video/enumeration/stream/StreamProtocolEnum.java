package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * 流协议枚举。
 * 定义流媒体服务器支持的输出协议类型，
 * 用于构建多协议流地址。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "StreamProtocolEnum", description = "流协议枚举")
public enum StreamProtocolEnum {

    RTMP("rtmp", "RTMP协议"),
    RTSP("rtsp", "RTSP协议"),
    FLV("flv", "HTTP-FLV协议"),
    HLS("hls", "HLS协议"),
    TS("ts", "HTTP-TS协议"),
    FMP4("fmp4", "HTTP-FMP4协议"),
    WS_FLV("ws_flv", "WebSocket-FLV协议"),
    WEBRTC("webrtc", "WebRTC协议"),
    ;

    private String value;
    private String desc;

    /**
     * 根据 value 查找枚举
     *
     * @param value 枚举值
     * @return 匹配的枚举实例
     */
    public static Optional<StreamProtocolEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
