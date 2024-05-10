package com.mqttsnet.thinglinks.tdengine.api.domain;

import com.mqttsnet.thinglinks.common.core.enums.DataTypeEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassDescription: 建表的字段实体类
 * @ClassName: Fields
 * @Author: thinglinks
 * @Date: 2021-12-28 09:09:04
 * @Version 1.0
 */
@Data
public class Fields implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段值
     */
    private Object fieldValue;

    /**
     * 字段数据类型
     */
    private DataTypeEnum dataType;

    /**
     * 字段字节大小
     */
    private Integer size;

    public Fields() {
    }

    public Fields(String fieldName) {
        this.fieldName = fieldName;
    }

    public Fields(String fieldName, DataTypeEnum dataType) {
        this.fieldName = fieldName;
        this.dataType = dataType;
    }

    public Fields(String fieldName, DataTypeEnum dataType, Integer size) {
        this.fieldName = fieldName;
        this.dataType = dataType;
        this.size = size;
    }

    public Fields(String fieldName, Object fieldValue, DataTypeEnum dataType, Integer size) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.dataType = dataType;
        this.size = size;
    }
}
