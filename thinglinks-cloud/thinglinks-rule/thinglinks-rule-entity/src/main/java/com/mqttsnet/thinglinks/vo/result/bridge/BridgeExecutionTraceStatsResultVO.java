package com.mqttsnet.thinglinks.vo.result.bridge;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 桥接执行日志统计 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "BridgeExecutionTraceStatsResultVO", description = "桥接执行日志统计")
public class BridgeExecutionTraceStatsResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前筛选条件下总记录数")
    private Long total;

    @Schema(description = "成功记录数")
    private Long success;

    @Schema(description = "失败记录数")
    private Long failed;

    @Schema(description = "部分成功记录数")
    private Long partial;

    @Schema(description = "死信记录数")
    private Long deadLetter;

    @Schema(description = "平均耗时毫秒")
    private Long avgLatencyMs;

    @Schema(description = "按时间分桶的执行趋势")
    private List<TimelinePoint> timeline;

    @Schema(description = "触发量 Top 规则")
    private List<TopRule> topRules;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "BridgeExecutionTraceStatsTimelinePoint", description = "桥接执行日志统计时间点")
    public static class TimelinePoint implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "时间标签")
        private String timeLabel;

        @Schema(description = "成功记录数")
        private Long success;

        @Schema(description = "失败记录数")
        private Long failed;

        @Schema(description = "部分成功记录数")
        private Long partial;

        @Schema(description = "死信记录数")
        private Long deadLetter;

        @Schema(description = "平均耗时毫秒")
        private Long avgLatencyMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "BridgeExecutionTraceStatsTopRule", description = "桥接执行日志触发量 Top 规则")
    public static class TopRule implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "桥接规则 ID")
        private Long bridgeRuleId;

        @Schema(description = "触发次数")
        private Long count;

        @Schema(description = "成功记录数")
        private Long success;

        @Schema(description = "失败记录数")
        private Long failed;

        @Schema(description = "死信记录数")
        private Long deadLetter;
    }
}
