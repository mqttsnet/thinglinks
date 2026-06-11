package com.mqttsnet.thinglinks.video.dto.onvif;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * ONVIF Media Profile（一台设备通常有"主码流 / 子码流"等多路）。
 *
 * @author mqttsnet
 * @since 2026-04-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "ONVIF Media Profile")
public class OnvifProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Profile token，{@code GetStreamUri} / PTZ 调用时必传。 */
    @Schema(description = "Profile token")
    private String token;

    /** Profile 名称（厂商定义，如 {@code "MainStream"} / {@code "SubStream"}）。 */
    @Schema(description = "Profile 名称")
    private String name;

    /** 视频编码（H264 / H265 / JPEG）。 */
    @Schema(description = "视频编码")
    private String videoEncoding;

    /** 分辨率宽度。 */
    @Schema(description = "分辨率宽度")
    private Integer width;

    /** 分辨率高度。 */
    @Schema(description = "分辨率高度")
    private Integer height;

    /** 帧率（fps）。 */
    @Schema(description = "帧率(fps)")
    private Integer frameRate;

    /** 码率（kbps）。 */
    @Schema(description = "码率(kbps)")
    private Integer bitrate;

    /** RTSP 拉流 URL（由 {@code GetStreamUri(token)} 单独获取后填入）。 */
    @Schema(description = "RTSP URL（GetStreamUri 后填入）")
    private String streamUri;
}
