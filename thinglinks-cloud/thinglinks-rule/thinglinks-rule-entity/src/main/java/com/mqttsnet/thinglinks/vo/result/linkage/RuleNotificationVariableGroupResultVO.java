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
 * 规则通知模板变量分组。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleNotificationVariableGroupResultVO", description = "规则通知模板变量分组")
public class RuleNotificationVariableGroupResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "分组编码")
    private String groupCode;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "变量列表")
    private List<RuleNotificationVariableResultVO> variables;
}
