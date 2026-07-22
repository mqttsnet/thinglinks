package com.mqttsnet.thinglinks.video.vo.result.device;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
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
 * 设备告警结果 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备告警结果")
public class VideoDeviceAlarmResultVO implements Serializable, EchoVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "设备国标编号")
    private String deviceIdentification;

    @Schema(description = "通道国标编号")
    private String channelIdentification;

    @Schema(description = "告警级别")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_ALARM_PRIORITY)
    private Integer alarmPriority;

    @Schema(description = "告警方式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_ALARM_METHOD)
    private Integer alarmMethod;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "告警描述")
    private String alarmDescription;

    @Schema(description = "告警类型")
    private Integer alarmType;

    @Schema(description = "告警类型参数")
    private String alarmTypeParam;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "处理状态(0-待处理/1-处理中/2-已处理/3-已忽略)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_DEVICE_ALARM_HANDLE_STATUS)
    private Integer handleStatus;

    @Echo(api = EchoApi.DEF_USER_ID_CLASS)
    @Schema(description = "处理人ID")
    private Long handleUserId;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理结果描述")
    private String handleResult;

    @Echo(api = EchoApi.ORG_ID_CLASS)
    @Schema(description = "创建组织")
    private Long createdOrgId;

    @Schema(description = "删除标记")
    private Integer deleted;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "创建人")
    private Long createdBy;

    @Schema(description = "更新时间")
    private LocalDateTime updatedTime;

    @Schema(description = "更新人")
    private Long updatedBy;
}
