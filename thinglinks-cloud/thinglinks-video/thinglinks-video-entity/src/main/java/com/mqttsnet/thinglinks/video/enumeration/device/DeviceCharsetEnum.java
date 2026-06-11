package com.mqttsnet.thinglinks.video.empowerment.device;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author mqttsnet
 */

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "DeviceCharsetEnum", description = "设备字符集枚举")
public enum DeviceCharsetEnum {

    UTF_8("UTF-8", "UTF-8"),
    GB2312("GB2312", "GB2312"),
    ;


    private String value;
    private String desc;


    /**
     * 根据value获取对应的枚举
     *
     * @param value 标识
     * @return 返回对应的枚举，如果没找到则返回 Optional.empty()
     */
    public static Optional<DeviceCharsetEnum> fromValue(String value) {
        return Stream.of(DeviceCharsetEnum.values())
                .filter(type -> type.getValue().equalsIgnoreCase(value))
                .findFirst();
    }
}
