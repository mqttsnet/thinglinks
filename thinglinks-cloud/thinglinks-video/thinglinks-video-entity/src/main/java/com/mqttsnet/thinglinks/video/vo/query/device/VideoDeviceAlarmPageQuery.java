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
import java.time.LocalDateTime;

/**
 * Description:
 * 设备告警分页查询条件 VO。
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
@Schema(description = "设备告警分页查询")
public class VideoDeviceAlarmPageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备国标编号")
    private String deviceIdentification;

    @Schema(description = "通道国标编号")
    private String channelIdentification;

    @Schema(description = "告警级别(1-4)")
    private Integer alarmPriority;

    @Schema(description = "告警方式")
    private Integer alarmMethod;

    @Schema(description = "告警类型")
    private Integer alarmType;

    @Schema(description = "处理状态(0-待处理/1-处理中/2-已处理/3-已忽略)")
    private Integer handleStatus;

    @Schema(description = "告警开始时间")
    private LocalDateTime startTime;

    @Schema(description = "告警结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建组织")
    private Long createdOrgId;
}
