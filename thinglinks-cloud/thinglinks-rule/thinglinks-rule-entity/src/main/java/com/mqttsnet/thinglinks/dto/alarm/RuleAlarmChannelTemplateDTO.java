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
 * 告警渠道模板配置。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleAlarmChannelTemplateDTO", description = "告警渠道模板配置")
public class RuleAlarmChannelTemplateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道类型")
    private Integer channelType;

    @Schema(description = "是否启用该渠道模板")
    private Boolean enabled;

    @Schema(description = "内容格式: MARKDOWN/NOTICE/TEXT")
    private String format;

    @Schema(description = "标题模板")
    private String titleTemplate;

    @Schema(description = "内容模板")
    private String contentTemplate;

    @Schema(description = "跳转地址模板,站内信使用")
    private String urlTemplate;

    @Schema(description = "是否@所有人,机器人渠道使用")
    private Boolean atAll;

    @Schema(description = "渠道级接收人;为空时使用动作全局接收人")
    private List<RuleAlarmRecipientDTO> recipients;
}
