package com.mqttsnet.thinglinks.common.core.enums;

import io.swagger.annotations.ApiModel;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @EnumDescription: tdEngine支持的数据类型的枚举类
 * @EnumName: DataTypeEnum
 * @Author: thinglinks
 * @Date: 2021-12-27 16:32:53
 */
@Getter
@ApiModel(value = "DataTypeEnum", description = "TD数据类型-枚举")
public enum DataTypeEnum {
    /**
     * 时间戳 缺省精度毫秒（格林威治时间开始）
     */
    TIMESTAMP("timestamp", false),

    /**
     * 单字节整形 范围[-127, 127] -128用作于NULL
     */
    TINYINT("tinyint", false),

    /**
     * 短整型 范围[-32767, 32767] -32768用作于NULL
     */
    SMALLINT("smallint", false),

    /**
     * 整形 范围[-2^31+1, 2^31-1] -2^31用作于NULL
     */
    INT("int", false),

    /**
     * 长整型 范围[-2^63+1, 2^63-1] -2^63用作于NULL
     */
    BIGINT("bigint", false),

    /**
     * 浮点型 有效位数6-7 范围[-3.4E38, 3.4E38]
     */
    FLOAT("float", false),

    /**
     * 双精度浮点型 有效位数15-16 范围[-1.7E308, 1.7E308]
     */
    DOUBLE("double", false),

    /**
     * 单字节字符串（建议只用于处理ASCII可见字符）最大长度16000
     */
    BINARY("binary", true),

    /**
     * 记录包含多字节字符在内的字符串（如中文字符）最大长度4093
     */
    NCHAR("nchar", true),

    /**
     * 布尔型 {true, false}
     */
    BOOL("bool", false),

    /**
     * json数据类型 只有tag类型可以是json格式
     */
    JSON("json", true),

    /**
     * 可变长的二进制数据
     */
    VARBINARY("varBinary", true);

    private final String dataType;
    private final boolean quoted;

    private static final ConcurrentHashMap<String, DataTypeEnum> DATA_TYPE_MAPPING = new ConcurrentHashMap<>();

    static {
        // 标准映射关系
        for (DataTypeEnum type : values()) {
            DATA_TYPE_MAPPING.put(type.dataType.toLowerCase(), type);
        }

        // 特殊映射关系或别名
        DATA_TYPE_MAPPING.put("string", NCHAR);
        DATA_TYPE_MAPPING.put("varchar", NCHAR);
        DATA_TYPE_MAPPING.put("decimal", DOUBLE);
        DATA_TYPE_MAPPING.put("nvarchar", NCHAR);
        DATA_TYPE_MAPPING.put("jsonObject", NCHAR);
        DATA_TYPE_MAPPING.put("int", INT);
        DATA_TYPE_MAPPING.put("bigint", BIGINT);
        DATA_TYPE_MAPPING.put("DateTime", NCHAR);
        DATA_TYPE_MAPPING.put("timestamp", TIMESTAMP);
        DATA_TYPE_MAPPING.put("bool", BOOL);

        // 这里添加其他映射关系。例如：
        DATA_TYPE_MAPPING.put("text", NCHAR); // 例如：将"text"也映射到NCHAR
        // 可以继续根据需要增加更多映射
    }

    /**
     * 通过dataType返回对应的枚举
     *
     * @param dataType 数据类型字符串
     * @return 对应的枚举，如果没有找到则返回null
     */
    public static DataTypeEnum valueOfByDataType(String dataType) {
        if (dataType == null) {
            return NCHAR; // 可替换为默认枚举值，如 UNKNOWN
        }

        return DATA_TYPE_MAPPING.computeIfAbsent(dataType.trim().toLowerCase(), key -> {
            // 这里处理未知的dataType，默认返回null，也可以返回例如：UNKNOWN。
            return NCHAR;
        });
    }


    /**
     * 判断类型是否一致
     *
     * @param otherDataType
     * @return {@link Boolean} ture|false
     */
    public boolean isTypeEqual(String otherDataType) {
        return Optional.ofNullable(otherDataType)
                .map(String::trim)
                .map(this.dataType::equalsIgnoreCase)
                .orElse(false);
    }

    DataTypeEnum(String dataType, boolean quoted) {
        this.dataType = dataType;
        this.quoted = quoted;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isQuoted() {
        return quoted;
    }
}