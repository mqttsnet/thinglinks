package com.mqttsnet.thinglinks.video.vo.result.media;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * 流媒体服务器实时性能指标结果 VO。
 *
 * <p>通过实时调用 ZLM HTTP API 采集的性能数据，
 * 供前端详情页指标卡片展示。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "流媒体服务器实时性能指标")
public class VideoMediaServerMetricsResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * CPU 使用率百分比（0.00~100.00）
     */
    @Schema(description = "CPU使用率百分比")
    private BigDecimal cpuUsage;

    /**
     * 内存使用率百分比（0.00~100.00）
     */
    @Schema(description = "内存使用率百分比")
    private BigDecimal memoryUsage;

    /**
     * 当前活跃流数量
     */
    @Schema(description = "当前活跃流数量")
    private Integer currentStreams;

    /**
     * 入网速率（bytes/s）
     */
    @Schema(description = "入网速率(bytes/s)")
    private Long networkInSpeed;

    /**
     * 出网速率（bytes/s）
     */
    @Schema(description = "出网速率(bytes/s)")
    private Long networkOutSpeed;
}
