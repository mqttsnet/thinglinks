package com.mqttsnet.thinglinks.tdengine.api.domain.model;

import cn.hutool.core.bean.BeanUtil;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FieldsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段数据类型
     */
    private String dataType;

    /**
     * 字段字节大小
     */
    private Integer size;

    public FieldsVO() {
    }

    public FieldsVO(String fieldName) {
        this.fieldName = fieldName;
    }

    public FieldsVO(String fieldName, String dataType) {
        this.fieldName = fieldName;
        this.dataType = dataType;
    }

    public FieldsVO(String fieldName, String dataType, Integer size) {
        this.fieldName = fieldName;
        this.dataType = dataType;
        this.size = size;
    }

    public static List<Fields> toFieldsList(List<FieldsVO> fieldsVOList) {
        return fieldsVOList.stream()
                .map(fieldsVO -> BeanUtil.toBeanIgnoreError(fieldsVO, Fields.class))
                .collect(Collectors.toList());
    }

    public static Fields toFields(FieldsVO fieldsVo) {
        return BeanUtil.toBeanIgnoreError(fieldsVo, Fields.class);
    }
}
