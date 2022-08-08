package com.mqttsnet.thinglinks.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 比较值
 *
 * @author shisen
 * @date 2022-8-7
 */
@Getter
@AllArgsConstructor
public enum OperatorEnum {

    eq("="),
    not("!="),
    gt(">"),
    lt("<"),
    gte(">="),
    lte("<="),
    between("between");

    private String symbol;

    public static OperatorEnum getBySymbol(String symbol) {
        for (OperatorEnum operatorEnum : values()) {
            if (operatorEnum.getSymbol().equals(symbol)) {
                //获取指定的枚举
                return operatorEnum;
            }
        }
        return null;
    }

}
