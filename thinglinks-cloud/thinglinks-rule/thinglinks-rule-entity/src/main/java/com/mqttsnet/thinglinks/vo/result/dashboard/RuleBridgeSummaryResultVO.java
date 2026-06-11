package com.mqttsnet.thinglinks.vo.result.dashboard;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 北向集成 / 数据桥接 仪表盘统计 VO。
 *
 * <p>用于资产统计页右栏「北向集成 · 数据桥接」看板:数据源 / 桥接规则 / 订阅源 的总量与启用数,
 * 以及今日桥接执行的成功 / 失败 / 死信分布(算成功率)。所有指标基于 {@code selectCount} 聚合,无新增表。</p>
 *
 * @author mqttsnet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(title = "RuleBridgeSummaryResultVO", description = "数据桥接仪表盘统计VO")
public class RuleBridgeSummaryResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ===== 数据源(rule_data_source)=====
    @Schema(description = "数据源总数")
    private Long dataSourceTotal;

    @Schema(description = "数据源启用数")
    private Long dataSourceEnabled;

    @Schema(description = "数据源健康数(health_status=HEALTHY)")
    private Long dataSourceHealthy;

    @Schema(description = "数据源异常数(health_status=DEGRADED/DOWN)")
    private Long dataSourceAbnormal;

    // ===== 桥接规则(rule_data_bridge)=====
    @Schema(description = "桥接规则总数")
    private Long bridgeRuleTotal;

    @Schema(description = "桥接规则启用数")
    private Long bridgeRuleEnabled;

    @Schema(description = "出站规则数(direction=10)")
    private Long bridgeRuleOutbound;

    @Schema(description = "入站规则数(direction=20)")
    private Long bridgeRuleInbound;

    // ===== 订阅源(rule_subscription_source)=====
    @Schema(description = "订阅源总数")
    private Long subscriptionTotal;

    @Schema(description = "订阅源启用数")
    private Long subscriptionEnabled;

    // ===== 今日桥接执行(rule_bridge_execution_trace)=====
    @Schema(description = "今日执行总数")
    private Long todayExecTotal;

    @Schema(description = "今日成功数(status=00)")
    private Long todaySuccess;

    @Schema(description = "今日失败数(status=01)")
    private Long todayFailed;

    @Schema(description = "今日部分成功数(status=02)")
    private Long todayPartial;

    @Schema(description = "今日死信数(status=03)")
    private Long todayDeadLetter;
}
