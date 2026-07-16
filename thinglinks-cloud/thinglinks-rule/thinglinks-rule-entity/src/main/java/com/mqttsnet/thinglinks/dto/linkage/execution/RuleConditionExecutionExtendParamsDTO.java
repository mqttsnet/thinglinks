package com.mqttsnet.thinglinks.dto.linkage.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 场景联动条件执行日志扩展参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleConditionExecutionExtendParamsDTO", description = "场景联动条件执行日志扩展参数")
public class RuleConditionExecutionExtendParamsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "条件 UUID")
    private String conditionUuid;

    @Schema(description = "条件类型")
    private Integer conditionType;

    @Schema(description = "是否命中")
    private Boolean evaluationResult;

    @Schema(description = "条件执行耗时毫秒")
    private Long latencyMs;

    @Schema(description = "条件执行明细")
    private String detail;
}
