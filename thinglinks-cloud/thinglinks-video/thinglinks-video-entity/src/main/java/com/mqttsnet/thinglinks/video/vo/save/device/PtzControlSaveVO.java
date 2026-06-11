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
 * PTZ 云台控制请求 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "PTZ云台控制请求参数")
public class PtzControlSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "设备国标编号不能为空")
    @Schema(description = "设备国标编号", required = true)
    private String deviceIdentification;

    @NotBlank(message = "通道国标编号不能为空")
    @Schema(description = "通道国标编号", required = true)
    private String channelIdentification;

    @NotBlank(message = "PTZ命令不能为空")
    @Schema(description = "PTZ命令(DIRECTION/ZOOM/COMBINED/STOP/PRESET_SET/PRESET_CALL/PRESET_DELETE/CRUISE_START/CRUISE_STOP等)", required = true)
    private String command;

    @Schema(description = "方向(UP/DOWN/LEFT/RIGHT/LEFT_UP/LEFT_DOWN/RIGHT_UP/RIGHT_DOWN/STOP)", example = "UP")
    private String direction;

    @Schema(description = "移动速度(0-255)", example = "128")
    private Integer moveSpeed;

    @Schema(description = "变倍方向(0=停止,1=缩小,2=放大)", example = "2")
    private Integer zoomDirection;

    @Schema(description = "变倍速度(0-255)", example = "128")
    private Integer zoomSpeed;

    @Schema(description = "预置位编号(1-255)", example = "1")
    private Integer presetId;

    @Schema(description = "巡航组编号", example = "1")
    private Integer cruiseId;

    @Schema(description = "巡航速度", example = "50")
    private Integer cruiseSpeed;

    @Schema(description = "巡航停留时间(秒)", example = "10")
    private Integer cruiseStayTime;

    @Schema(description = "扫描组编号", example = "1")
    private Integer scanId;

    @Schema(description = "扫描速度", example = "50")
    private Integer scanSpeed;

    @Schema(description = "辅助开关编号(1-255)", example = "1")
    private Integer switchId;

    @Schema(description = "辅助开关状态(true=开,false=关)", example = "true")
    private Boolean switchOn;
}
