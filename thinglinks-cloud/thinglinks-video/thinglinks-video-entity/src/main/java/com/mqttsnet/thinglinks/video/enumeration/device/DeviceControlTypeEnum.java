package com.mqttsnet.thinglinks.video.empowerment.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.dom4j.Element;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * Description:
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/5/9
 */
@Getter
@Schema(title = "DeviceControlTypeEnum", description = "设备控制类型")
public enum DeviceControlTypeEnum {

    PTZ("PTZCmd", "云台控制"),
    TELE_BOOT("TeleBoot", "远程启动"),
    RECORD("RecordCmd", "录像控制"),
    GUARD("GuardCmd", "布防撤防"),
    ALARM("AlarmCmd", "告警控制"),
    I_FRAME("IFameCmd", "强制关键帧"),
    DRAG_ZOOM_IN("DragZoomIn", "拉框放大"),
    DRAG_ZOOM_OUT("DragZoomOut", "拉框缩小"),
    HOME_POSITION("HomePosition", "看守位");

    private final String value;
    private final String desc;

    DeviceControlTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static Optional<DeviceControlTypeEnum> fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.getValue().equals(value))
                .findFirst();
    }

    public static DeviceControlTypeEnum typeOf(Element rootElement) {
        for (DeviceControlTypeEnum item : values()) {
            if (!ObjectUtils.isEmpty(rootElement.element(item.value))
                || !ObjectUtils.isEmpty(rootElement.elements(item.value))) {
                return item;
            }
        }
        return null;
    }
}
