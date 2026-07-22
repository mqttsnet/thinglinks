package com.mqttsnet.thinglinks.dto.alarm.channel.site;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 站内信告警渠道配置。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "SiteMessageParamDTO", description = "站内信告警渠道配置")
public class SiteMessageParamDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "提醒方式: 01-待办 02-预警 03-提醒", example = "02")
    private String remindMode;

    @Schema(description = "打开方式: 01-页面 02-弹窗 03-新开窗口", example = "01")
    private String target;

    @Schema(description = "是否自动已读", example = "false")
    private Boolean autoRead;

    @Schema(description = "默认处理地址", example = "/#/engine/linkage")
    private String url;

    @Schema(description = "站内信接收人员工ID列表")
    private List<String> recipientList;
}
