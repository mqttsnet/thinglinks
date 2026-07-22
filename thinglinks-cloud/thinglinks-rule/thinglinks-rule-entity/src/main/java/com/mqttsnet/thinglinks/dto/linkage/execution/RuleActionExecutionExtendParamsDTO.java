package com.mqttsnet.thinglinks.dto.linkage.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 场景联动动作执行日志扩展参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleActionExecutionExtendParamsDTO", description = "场景联动动作执行日志扩展参数")
public class RuleActionExecutionExtendParamsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "规则条件ID")
    private Long ruleConditionId;

    @Schema(description = "动作类型")
    private Integer actionType;

    @Schema(description = "动作名称")
    private String actionName;

    @Schema(description = "是否执行成功")
    private Boolean result;

    @Schema(description = "动作耗时毫秒")
    private Long latencyMs;

    @Schema(description = "备注")
    private String remark;
}
