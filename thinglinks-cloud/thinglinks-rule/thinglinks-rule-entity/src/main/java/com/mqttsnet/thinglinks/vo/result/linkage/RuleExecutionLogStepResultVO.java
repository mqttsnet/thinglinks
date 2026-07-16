package com.mqttsnet.thinglinks.vo.result.linkage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 场景联动执行步骤展示 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleExecutionLogStepResultVO", description = "场景联动执行步骤")
public class RuleExecutionLogStepResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "步骤日志主键")
    private Long id;

    @Schema(description = "步骤序号")
    private Integer stepNo;

    @Schema(description = "步骤类型: TRIGGER/RULE_MATCH/ACTION")
    private String stepType;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "步骤状态: 00-成功 / 01-失败 / 02-未命中或跳过")
    private String status;

    @Schema(description = "步骤耗时毫秒")
    private Long latencyMs;

    @Schema(description = "输入摘要 JSON 或文本")
    private String inputSummary;

    @Schema(description = "输出摘要 JSON 或文本")
    private String outputSummary;

    @Schema(description = "错误或未命中原因")
    private String errorMsg;

    @Schema(description = "步骤开始时间")
    private LocalDateTime startedAt;

    @Schema(description = "扩展参数")
    private String extendParams;

    @Schema(description = "备注")
    private String remark;
}
