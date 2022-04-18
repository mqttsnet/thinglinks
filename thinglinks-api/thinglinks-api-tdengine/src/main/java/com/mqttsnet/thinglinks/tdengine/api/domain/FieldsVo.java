package com.mqttsnet.thinglinks.tdengine.api.domain;

import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassDescription: 建表的字段实体类的vo类
 * @ClassName: FieldsVo
 * @Author: thinglinks
 * @Date: 2021-12-28 11:30:06
 * @Version 1.0
 */
@Data
public class FieldsVo {
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

    /**
    *@MethodDescription 字段实体类转为vo类
    *@param fields 字段实体类
    *@return FieldsVo 字段实体vo类
    *@author thinglinks
    *@Date 2021/12/28 13:48
    */
    public static FieldsVo fieldsTranscoding(Fields fields) throws SQLException {
        if (StringUtils.isBlank(fields.getFieldName()) || fields.getDataType() == null) {
            //TODO 修改抛出的异常类，改为自定义异常类
            throw new SQLException("invalid operation: fieldName or dataType can not be null");
        }
        FieldsVo fieldsVo = new FieldsVo();
        fieldsVo.setFieldName(fields.getFieldName());
        fieldsVo.setDataType(fields.getDataType().getDataType());
        fieldsVo.setSize(fields.getSize());
        return fieldsVo;
    }

    /**
     *@MethodDescription 字段实体类集合转为vo类集合
     *@param fieldsList 字段实体类集合
     *@return List<FieldsVo> 字段实体vo类集合
     *@author thinglinks
     *@Date 2021/12/28 14:00
     */
    public static List<FieldsVo> fieldsTranscoding(List<Fields> fieldsList) throws SQLException{
        List<FieldsVo> fieldsVoList = new ArrayList<>();
        for (Fields fields : fieldsList) {
            fieldsVoList.add(fieldsTranscoding(fields));
        }
        return fieldsVoList;
    }
}
