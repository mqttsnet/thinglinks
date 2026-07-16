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
 * 渲染后的告警通知。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "RuleAlarmRenderedNotificationDTO", description = "渲染后的告警通知")
public class RuleAlarmRenderedNotificationDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "渠道类型")
    private Integer channelType;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "内容格式")
    private String format;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "跳转地址")
    private String url;

    @Schema(description = "是否@所有人")
    private Boolean atAll;

    @Schema(description = "接收人")
    private List<RuleAlarmRecipientDTO> recipients;
}
