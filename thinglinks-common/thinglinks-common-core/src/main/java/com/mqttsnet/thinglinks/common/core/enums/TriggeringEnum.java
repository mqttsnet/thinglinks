package com.mqttsnet.thinglinks.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 触发机制
 *
 * @author shisen
 * @date 2022-8-7
 */
@Getter
@AllArgsConstructor
public enum TriggeringEnum {
    // 触发机制 0:全部，1:任意一个
    ALL(0),
    ANY(1);

    private Integer symbol;

    public static TriggeringEnum getBySymbol(Integer symbol) {
        for (TriggeringEnum triggeringEnum : values()) {
            if (triggeringEnum.getSymbol().equals(symbol)) {
                //获取指定的枚举
                return triggeringEnum;
            }
        }
        return null;
    }
}
