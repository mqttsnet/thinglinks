package com.mqttsnet.thinglinks.video.vo.save.device;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * 设备远程控制请求 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备远程控制请求参数")
public class DeviceControlSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "设备国标编号不能为空")
    @Schema(description = "设备国标编号", required = true)
    private String deviceIdentification;

    @NotBlank(message = "通道国标编号不能为空")
    @Schema(description = "通道国标编号", required = true)
    private String channelIdentification;

    @NotBlank(message = "控制类型不能为空")
    @Schema(description = "控制类型(TeleBoot=远程启动/RecordCmd=录像控制/GuardCmd=布防撤防/AlarmCmd=告警控制/IFameCmd=强制关键帧/HomePosition=看守位)", required = true)
    private String controlType;

    @Schema(description = "控制参数(如RecordCmd的Record/StopRecord, GuardCmd的SetGuard/ResetGuard)", example = "Record")
    private String controlValue;
}
