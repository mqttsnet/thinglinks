package com.mqttsnet.thinglinks.video.vo.save.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 通知订阅保存参数。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知订阅保存参数")
public class VideoNotifySubscriptionSaveVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "订阅名称不能为空")
    @Schema(description = "订阅名称")
    private String subscriptionName;

    @NotBlank(message = "渠道类型不能为空")
    @Schema(description = "渠道类型: NOTICE/DINGTALK/FEISHU/ENTERPRISE_WECHAT/SMS")
    private String channelType;

    @Schema(description = "渠道凭证(JSON)")
    private String channelConfig;

    @NotBlank(message = "消息模板编码不能为空")
    @Schema(description = "消息模板编码(ExtendMsgTemplate.code)")
    private String templateCode;

    @NotBlank(message = "事件类型不能为空")
    @Schema(description = "订阅事件类型(逗号分隔)")
    private String eventTypes;

    @Schema(description = "告警级别过滤(逗号分隔,空=全部)")
    private String priorityFilter;

    @Schema(description = "站内信接收范围: SELF/ORG/CUSTOM")
    private String recipientScope;

    @Schema(description = "接收人用户ID(逗号分隔)")
    private String recipientIds;

    @Schema(description = "@所有人(0=否/1=是)")
    private Integer atAll;

    @Schema(description = "跳转链接模板")
    private String jumpUrlTemplate;

    @Schema(description = "消息内容模板(支持${变量})")
    private String msgTemplate;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态(0=禁用/1=启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
