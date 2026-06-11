package com.mqttsnet.thinglinks.video.enumeration.device;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * PTZ 方向枚举。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
public enum PtzDirectionEnum {

    UP("UP", "上", 0, 1),
    DOWN("DOWN", "下", 0, 2),
    LEFT("LEFT", "左", 1, 0),
    RIGHT("RIGHT", "右", 2, 0),
    LEFT_UP("LEFT_UP", "左上", 1, 1),
    LEFT_DOWN("LEFT_DOWN", "左下", 1, 2),
    RIGHT_UP("RIGHT_UP", "右上", 2, 1),
    RIGHT_DOWN("RIGHT_DOWN", "右下", 2, 2),
    STOP("STOP", "停止", 0, 0),
    ;

    private final String value;
    private final String desc;
    private final Integer leftRight;
    private final Integer upDown;

    public static Optional<PtzDirectionEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }
}
