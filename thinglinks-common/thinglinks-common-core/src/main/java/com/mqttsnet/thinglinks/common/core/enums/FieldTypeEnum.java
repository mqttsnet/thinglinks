package com.mqttsnet.thinglinks.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字符类型
 *
 * @author shisen
 * @date 2022-8-7
 */
@Getter
@AllArgsConstructor
public enum FieldTypeEnum {

    INT("int"),
    STRING("string"),
    DECIMAL("decimal"),
    TIMESTAMP("timestamp"),
    BOOL("bool");
    private String symbol;

    public static FieldTypeEnum getBySymbol(String symbol) {
        for (FieldTypeEnum fieldTypeEnum : values()) {
            if (fieldTypeEnum.getSymbol().equals(symbol)) {
                //获取指定的枚举
                return fieldTypeEnum;
            }
        }
        return null;
    }

}
