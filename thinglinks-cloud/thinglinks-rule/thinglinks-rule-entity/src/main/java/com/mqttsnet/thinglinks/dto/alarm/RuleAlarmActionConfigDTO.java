package com.mqttsnet.thinglinks.dto.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 场景联动触发告警动作配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleAlarmActionConfigDTO", description = "场景联动触发告警动作配置")
public class RuleAlarmActionConfigDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "配置版本;2=渠道模板配置")
    private Integer version;

    @Schema(description = "告警标识")
    private String alarmIdentification;

    /**
     * 兼容历史字段:多个手机号用逗号分隔。
     */
    @Schema(description = "兼容历史通知手机号")
    private String atPhone;

    /**
     * 兼容历史字段:告警内容。
     */
    @Schema(description = "兼容历史告警内容")
    private String contentData;

    @Schema(description = "全局接收人")
    private List<RuleAlarmRecipientDTO> recipients;

    @Schema(description = "渠道模板配置")
    private List<RuleAlarmChannelTemplateDTO> channelTemplates;
}
