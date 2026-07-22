package com.mqttsnet.thinglinks.video.vo.result.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 活跃流信息结果 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活跃流信息")
public class ActiveStreamResultVO implements Serializable {
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

    @Schema(description = "媒体服务器ID")
    private String serverId;

    @Schema(description = "媒体服务器地址")
    private String mediaServerHost;

    @Schema(description = "FLV播放地址")
    private String flvUrl;

    @Schema(description = "Call-ID")
    private String callId;
}
