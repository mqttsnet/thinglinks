package com.mqttsnet.thinglinks.video.enumeration.stream;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * 回放倍速枚举。
 * 定义录像回放支持的播放速度。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "PlaybackSpeedEnum", description = "回放倍速枚举")
public enum PlaybackSpeedEnum {

    SPEED_0_25(0.25, "0.25倍速"),
    SPEED_0_5(0.5, "0.5倍速"),
    SPEED_1(1.0, "正常速度"),
    SPEED_2(2.0, "2倍速"),
    SPEED_4(4.0, "4倍速"),
    ;

    private Double value;
    private String desc;

    /**
     * 根据倍速值查找对应的枚举实例
     *
     * @param value 倍速值（如 0.25、1.0、2.0、4.0）
     * @return 匹配的 {@link PlaybackSpeedEnum} 实例，未匹配返回 {@link Optional#empty()}
     */
    public static Optional<PlaybackSpeedEnum> fromValue(Double value) {
        if (value == null) {
            return Optional.empty();
        }
        return Stream.of(values())
                .filter(e -> Double.compare(e.getValue(), value) == 0)
                .findFirst();
    }
}
