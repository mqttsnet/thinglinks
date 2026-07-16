package com.mqttsnet.thinglinks.vo.result.linkage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 场景联动执行日志统计 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleExecutionLogStatsResultVO", description = "场景联动执行日志统计")
public class RuleExecutionLogStatsResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前筛选条件下总记录数")
    private Long total;

    @Schema(description = "已完成记录数")
    private Long completed;

    @Schema(description = "执行中记录数")
    private Long executing;

    @Schema(description = "未执行记录数")
    private Long notExecuted;

    @Schema(description = "平均耗时毫秒")
    private Long avgLatencyMs;

    @Schema(description = "按时间分桶的执行趋势")
    private List<TimelinePoint> timeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "RuleExecutionLogStatsTimelinePoint", description = "场景联动执行日志统计时间点")
    public static class TimelinePoint implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "时间标签")
        private String timeLabel;

        @Schema(description = "已完成记录数")
        private Long completed;

        @Schema(description = "执行中记录数")
        private Long executing;

        @Schema(description = "未执行记录数")
        private Long notExecuted;

        @Schema(description = "平均耗时毫秒")
        private Long avgLatencyMs;
    }
}
