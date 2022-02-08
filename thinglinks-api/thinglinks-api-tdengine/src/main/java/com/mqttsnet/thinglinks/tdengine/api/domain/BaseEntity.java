package com.mqttsnet.thinglinks.tdengine.api.domain;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @ClassDescription: tdEngine的基础实体类
 * @ClassName: BaseEntity
 * @Author: thinglinks
 * @Date: 2021-12-30 14:39:25
 * @Version 1.0
 */
@Data
public class BaseEntity {

    /**
     * 数据库名称
     */
    @NotBlank(message = "invalid operation: databaseName can not be empty")
    private String databaseName;

    /**
     * 超级表名称
     */
    @NotBlank(message = "invalid operation: superTableName can not be empty")
    private String superTableName;
}
