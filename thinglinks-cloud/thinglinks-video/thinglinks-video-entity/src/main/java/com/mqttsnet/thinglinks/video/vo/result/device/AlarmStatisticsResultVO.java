package com.mqttsnet.thinglinks.video.vo.result.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Description:
 * 告警统计结果 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "告警统计结果")
public class AlarmStatisticsResultVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "告警总数")
    private Long totalCount;

    @Schema(description = "未处理数")
    private Long unhandledCount;

    @Schema(description = "按告警级别统计")
    private List<CountItem> byPriority;

    @Schema(description = "按设备统计(TOP10)")
    private List<CountItem> byDevice;

    @Schema(description = "按天统计")
    private List<CountItem> byDay;

    /**
     * 通用统计项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "统计项")
    public static class CountItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "统计维度名称")
        private String name;

        @Schema(description = "统计数量")
        private Long count;
    }
}
