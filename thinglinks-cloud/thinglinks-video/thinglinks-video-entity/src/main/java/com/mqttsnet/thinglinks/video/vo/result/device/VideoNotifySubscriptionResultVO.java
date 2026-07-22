package com.mqttsnet.thinglinks.video.vo.result.device;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Description:
 * 通知订阅结果 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "通知订阅结果")
public class VideoNotifySubscriptionResultVO implements Serializable, EchoVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "订阅名称")
    private String subscriptionName;

    @Schema(description = "渠道类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = "NOTIFY_CHANNEL_TYPE")
    private String channelType;

    @Schema(description = "消息模板编码")
    private String templateCode;

    @Schema(description = "订阅事件类型(逗号分隔)")
    private String eventTypes;

    @Schema(description = "告警级别过滤")
    private String priorityFilter;

    @Schema(description = "站内信接收范围")
    private String recipientScope;

    @Schema(description = "接收人用户ID(逗号分隔)")
    private String recipientIds;

    @Schema(description = "@所有人")
    private Integer atAll;

    @Schema(description = "跳转链接模板")
    private String jumpUrlTemplate;

    @Schema(description = "消息内容模板")
    private String msgTemplate;

    @Schema(description = "状态(0=禁用/1=启用)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Echo(api = EchoApi.ORG_ID_CLASS)
    @Schema(description = "创建组织")
    private Long createdOrgId;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private Long updatedBy;
}
