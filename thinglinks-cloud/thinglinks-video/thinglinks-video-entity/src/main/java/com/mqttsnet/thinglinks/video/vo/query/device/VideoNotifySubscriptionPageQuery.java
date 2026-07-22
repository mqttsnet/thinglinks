package com.mqttsnet.thinglinks.video.vo.query.device;

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

/**
 * Description:
 * 通知订阅分页查询条件 VO。
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
@Schema(description = "通知订阅分页查询")
public class VideoNotifySubscriptionPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订阅名称(模糊)")
    private String subscriptionName;

    @Schema(description = "渠道类型")
    private String channelType;

    @Schema(description = "事件类型(精确)")
    private String eventTypes;

    @Schema(description = "状态(0=禁用/1=启用)")
    private Integer status;

    @Schema(description = "创建组织")
    private Long createdOrgId;
}
