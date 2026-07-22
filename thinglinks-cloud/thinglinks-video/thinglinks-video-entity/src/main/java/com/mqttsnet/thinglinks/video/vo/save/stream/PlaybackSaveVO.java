package com.mqttsnet.thinglinks.video.vo.save.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Description:
 * 录像回放请求 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "录像回放请求参数")
public class PlaybackSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "设备国标编号不能为空")
    @Schema(description = "设备国标编号", required = true)
    private String deviceIdentification;

    @NotBlank(message = "通道国标编号不能为空")
    @Schema(description = "通道国标编号", required = true)
    private String channelIdentification;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "回放开始时间", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "回放结束时间", required = true)
    private LocalDateTime endTime;
}
