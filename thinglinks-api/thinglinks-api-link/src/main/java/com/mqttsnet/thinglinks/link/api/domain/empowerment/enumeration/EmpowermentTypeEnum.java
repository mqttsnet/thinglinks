package com.mqttsnet.thinglinks.link.api.domain.empowerment.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: 赋能类型
 * @packagename: com.mqttsnet.thinglinks.empowerment.enumeration
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-09-16 21:50
 **/
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "EmpowermentTypeEnum", description = "赋能类型")
public enum EmpowermentTypeEnum {

    /**
     * 未知
     */
    UNKNOWN(0, "未知"),

    /**
     * 产品
     */
    PRODUCT(1, "产品"),

    /**
     * 设备
     */
    DEVICE(2, "设备"),

    ;

    private Integer value;
    private String desc;

    /**
     * 可选值
     */
    public static final List<Integer> TYPE_COLLECTION = Arrays.asList(UNKNOWN.value, PRODUCT.value, DEVICE.value);


    public static EmpowermentTypeEnum valueOf(Integer value) {
        return Arrays.stream(values())
                     .filter(type -> type.getValue().equals(Optional.ofNullable(value).orElse(-1))) // 使用一个不存在的默认值，如-1
                     .findFirst()
                     .orElse(UNKNOWN); // 或者您可以选择返回一个默认的枚举值，比如UNKNOWN
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
