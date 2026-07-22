package com.mqttsnet.thinglinks.video.vo.save.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Description:
 * 设备告警保存 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备告警保存参数")
public class VideoDeviceAlarmSaveVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "设备国标编号不能为空")
    @Schema(description = "设备国标编号")
    private String deviceIdentification;

    @Schema(description = "通道国标编号")
    private String channelIdentification;

    @Schema(description = "告警级别(1-一级/2-二级/3-三级/4-四级)")
    private Integer alarmPriority;

    @Schema(description = "告警方式(1-电话/2-设备/3-短信/4-GPS/5-视频/6-设备故障/7-其他)")
    private Integer alarmMethod;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "告警描述")
    private String alarmDescription;

    @Schema(description = "告警类型")
    private Integer alarmType;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "纬度")
    private Double latitude;
}
