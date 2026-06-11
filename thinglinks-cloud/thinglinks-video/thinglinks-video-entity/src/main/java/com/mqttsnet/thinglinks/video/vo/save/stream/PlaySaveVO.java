package com.mqttsnet.thinglinks.video.vo.save.stream;

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
 * 实时点播请求 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "实时点播请求参数")
public class PlaySaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "设备国标编号不能为空")
    @Schema(description = "设备国标编号", required = true, example = "34020000001110000001")
    private String deviceIdentification;

    @NotBlank(message = "通道国标编号不能为空")
    @Schema(description = "通道国标编号", required = true, example = "34020000001320000001")
    private String channelIdentification;
}
