package com.mqttsnet.thinglinks.video.vo.result.stream;

import com.mqttsnet.thinglinks.video.dto.media.stream.StreamURL;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 实时点播结果 VO。
 * 包含多协议流地址，前端可根据需要选择协议播放。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时点播结果")
public class PlayResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备国标编号")
    private String deviceIdentification;

    @Schema(description = "通道国标编号")
    private String channelIdentification;

    @Schema(description = "应用名")
    private String app;

    @Schema(description = "流ID")
    private String stream;

    @Schema(description = "流媒体服务器IP")
    private String mediaServerIp;

    @Schema(description = "HTTP-FLV流地址")
    private StreamURL flv;

    @Schema(description = "HTTPS-FLV流地址")
    private StreamURL httpsFlv;

    @Schema(description = "WebSocket-FLV流地址")
    private StreamURL wsFlv;

    @Schema(description = "HLS流地址")
    private StreamURL hls;

    @Schema(description = "HTTPS-HLS流地址")
    private StreamURL httpsHls;

    @Schema(description = "RTMP流地址")
    private StreamURL rtmp;

    @Schema(description = "RTSP流地址")
    private StreamURL rtsp;

    @Schema(description = "HTTP-FMP4流地址")
    private StreamURL fmp4;

    @Schema(description = "HTTP-TS流地址")
    private StreamURL ts;

    @Schema(description = "WebRTC流地址")
    private StreamURL rtc;

    @Schema(description = "RTCS流地址")
    private StreamURL rtcs;

    @Schema(description = "Call-ID")
    private String callId;
}
