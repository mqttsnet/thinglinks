package com.mqttsnet.thinglinks.common.core.enums;

/**
 * @EnumDescription: tdEngine支持的数据类型的枚举类
 * @EnumName: DataTypeEnum
 * @Author: thinglinks
 * @Date: 2021-12-27 16:32:53
 */
public enum DataTypeEnum {

    /**
     * 时间戳 缺省精度毫秒（格林威治时间开始）
     */
    TIMESTAMP("timestamp"),

    /**
     * 单字节整形 范围[-127, 127] -128用作于NULL
     */
    TINYINT("tinyint"),

    /**
     * 短整型 范围[-32767, 32767] -32768用作于NULL
     */
    SMALLINT("smallint"),

    /**
     * 整形 范围[-2^31+1, 2^31-1] -2^31用作于NULL
     */
    INT("int"),

    /**
     * 长整型 范围[-2^63+1, 2^63-1] -2^63用作于NULL
     */
    BIGINT("bigint"),

    /**
     * 浮点型 有效位数6-7 范围[-3.4E38, 3.4E38]
     */
    FLOAT("float"),

    /**
     * 双精度浮点型 有效位数15-16 范围[-1.7E308, 1.7E308]
     */
    DOUBLE("double"),

    /**
     * 单字节字符串（建议只用于处理ASCII可见字符）
     */
    BINARY("binary"),

    /**
     * 记录包含多字节字符在内的字符串（如中文字符）
     */
    NCHAR("nchar"),

    /**
     * 布尔型 {true, false}
     */
    BOOL("bool"),

    /**
     * json数据类型 只有tag类型可以是json格式
     */
    JSON("json");

    private final String dataType;

    DataTypeEnum(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }
}
