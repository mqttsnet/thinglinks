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
 * 流监控概览统计 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流监控概览")
public class StreamOverviewResultVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "总活跃流数")
    private Integer totalActiveStreams;

    @Schema(description = "在线设备数")
    private Long onlineDeviceCount;

    @Schema(description = "离线设备数")
    private Long offlineDeviceCount;

    @Schema(description = "通道总数")
    private Long totalChannelCount;
}
