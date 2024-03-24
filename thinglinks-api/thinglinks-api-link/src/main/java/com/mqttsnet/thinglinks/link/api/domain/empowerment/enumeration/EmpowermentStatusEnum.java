package com.mqttsnet.thinglinks.link.api.domain.empowerment.enumeration;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: 赋能状态类型
 * @packagename: com.mqttsnet.thinglinks.empowerment.enumeration
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-09-18 23:38
 **/
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "EmpowermentStatus", description = "赋能状态类型")
public enum EmpowermentStatusEnum {

    /**
     * 计划中
     */
    PLANNING(0, "计划中"),

    /**
     * 执行中
     */
    IN_PROGRESS(1, "执行中"),

    /**
     * 执行完成
     */
    COMPLETED(2, "执行完成"),

    ;

    private Integer value;
    private String desc;

    /**
     * 可选值
     */
    public static final List<Integer> STATE_COLLECTION = Arrays.asList(PLANNING.value, IN_PROGRESS.value, COMPLETED.value);

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
