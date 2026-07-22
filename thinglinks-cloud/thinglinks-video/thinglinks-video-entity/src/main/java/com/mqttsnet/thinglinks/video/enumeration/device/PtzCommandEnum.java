package com.mqttsnet.thinglinks.video.enumeration.device;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Description:
 * PTZ 命令类型枚举。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@AllArgsConstructor
public enum PtzCommandEnum {

    DIRECTION("DIRECTION", "方向控制"),
    ZOOM("ZOOM", "变倍控制"),
    COMBINED("COMBINED", "组合控制"),
    STOP("STOP", "停止"),
    PRESET_SET("PRESET_SET", "设置预置位"),
    PRESET_CALL("PRESET_CALL", "调用预置位"),
    PRESET_DELETE("PRESET_DELETE", "删除预置位"),
    CRUISE_START("CRUISE_START", "开始巡航"),
    CRUISE_STOP("CRUISE_STOP", "停止巡航"),
    CRUISE_ADD_POINT("CRUISE_ADD_POINT", "添加巡航点"),
    CRUISE_DELETE_POINT("CRUISE_DELETE_POINT", "删除巡航点"),
    SCAN_START("SCAN_START", "开始扫描"),
    SCAN_STOP("SCAN_STOP", "停止扫描"),
    SCAN_SET_LEFT("SCAN_SET_LEFT", "设置左边界"),
    SCAN_SET_RIGHT("SCAN_SET_RIGHT", "设置右边界"),
    AUX_SWITCH("AUX_SWITCH", "辅助开关"),
    ;

    private final String value;
    private final String desc;

    public static Optional<PtzCommandEnum> fromValue(String value) {
        return Stream.of(values())
                .filter(e -> e.getValue().equals(value))
                .findFirst();
    }
}
