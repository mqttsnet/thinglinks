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
 * 规则通知模板变量。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleNotificationVariableResultVO", description = "规则通知模板变量")
public class RuleNotificationVariableResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "变量键,例如 rule.name")
    private String key;

    @Schema(description = "模板占位符,例如 ${rule.name}")
    private String placeholder;

    @Schema(description = "变量名称")
    private String label;

    @Schema(description = "变量说明")
    private String description;

    @Schema(description = "示例值")
    private String sample;

    @Schema(description = "适用渠道;为空表示所有渠道")
    private List<Integer> channelTypes;
}
