package com.mqttsnet.thinglinks.dto.linkage.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 场景联动规则执行日志扩展参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleExecutionLogExtendParamsDTO", description = "场景联动规则执行日志扩展参数")
public class RuleExecutionLogExtendParamsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "规则执行耗时毫秒")
    private Long latencyMs;

    @Schema(description = "扩展说明")
    private String detail;
}
