package com.mqttsnet.thinglinks.video.vo.update.device;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 通知订阅更新参数。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(description = "通知订阅更新参数")
public class VideoNotifySubscriptionUpdateVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "请填写id", groups = SuperEntity.Update.class)
    private Long id;

    @Schema(description = "订阅名称")
    private String subscriptionName;

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "渠道凭证(JSON)")
    private String channelConfig;

    @Schema(description = "消息模板编码")
    private String templateCode;

    @Schema(description = "订阅事件类型(逗号分隔)")
    private String eventTypes;

    @Schema(description = "告警级别过滤(逗号分隔)")
    private String priorityFilter;

    @Schema(description = "站内信接收范围: SELF/ORG/CUSTOM")
    private String recipientScope;

    @Schema(description = "接收人用户ID(逗号分隔)")
    private String recipientIds;

    @Schema(description = "@所有人(0=否/1=是)")
    private Integer atAll;

    @Schema(description = "跳转链接模板")
    private String jumpUrlTemplate;

    @Schema(description = "消息内容模板")
    private String msgTemplate;

    @Schema(description = "状态(0=禁用/1=启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
