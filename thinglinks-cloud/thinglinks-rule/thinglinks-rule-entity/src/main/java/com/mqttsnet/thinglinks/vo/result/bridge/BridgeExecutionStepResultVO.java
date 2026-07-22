package com.mqttsnet.thinglinks.vo.result.bridge;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询返回值 VO
 * 桥接执行步骤明细（链路回放展示用）
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "BridgeExecutionStepResultVO", description = "桥接执行步骤明细返回值")
public class BridgeExecutionStepResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联 trace ID")
    private String traceId;

    @Schema(description = "步骤顺序号（从1起）")
    private Integer stepNo;

    @Schema(description = "类型：INGEST / RULE_MATCH / RATE_LIMIT / TRANSFORM / SINK_SEND / DEAD_LETTER / INBOUND_FORWARD")
    private String stepType;

    @Schema(description = "步骤可读名称（中文）")
    private String stepName;

    @Schema(description = "状态：00-成功 / 01-失败 / 02-跳过")
    private String status;

    @Schema(description = "本步骤耗时（毫秒）")
    private Integer latencyMs;

    @Schema(description = "输入摘要 JSON")
    private String inputSummary;

    @Schema(description = "输出摘要 JSON")
    private String outputSummary;

    @Schema(description = "失败错误（status=01 时填）")
    private String errorMsg;

    @Schema(description = "步骤开始时间（毫秒精度）")
    private LocalDateTime startedAt;

    @Schema(description = "扩展参数（步骤特异协议数据 JSON）")
    private String extendParams;

    @Schema(description = "备注")
    private String remark;
}
