package com.mqttsnet.thinglinks.vo.param.linkage;

import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmChannelTemplateDTO;
import com.mqttsnet.thinglinks.dto.alarm.RuleAlarmRecipientDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 规则通知模板预览参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleNotificationPreviewParamVO", description = "规则通知模板预览参数")
public class RuleNotificationPreviewParamVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "告警标识")
    private String alarmIdentification;

    @Schema(description = "全局接收人")
    private List<RuleAlarmRecipientDTO> recipients;

    @Schema(description = "渠道模板")
    private List<RuleAlarmChannelTemplateDTO> channelTemplates;

    @Schema(description = "前端传入的预览变量,用于覆盖默认示例值")
    private Map<String, Object> sampleVariables;
}
