package com.mqttsnet.thinglinks.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 触发条件
 *
 * @author shisen
 * @date 2022-8-7
 */
@Getter
@AllArgsConstructor
public enum ConditionTypeEnum {
    //条件类型(0:匹配设备触发、1:指定设备触发、2:按策略定时触发)
    MATCH(0),
    SPECIFY(1),
    STRATEGY(2);

    private Integer symbol;

    public static ConditionTypeEnum getBySymbol(Integer symbol) {
        for (ConditionTypeEnum conditionTypeEnum : values()) {
            if (conditionTypeEnum.getSymbol().equals(symbol)) {
                //获取指定的枚举
                return conditionTypeEnum;
            }
        }
        return null;
    }
}
