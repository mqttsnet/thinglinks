package com.mqttsnet.thinglinks.tdengine.api.domain;

import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * @program: thinglinks
 * @description: 超级表结构VO
 * @packagename: com.mqttsnet.thinglinks.tds.vo.result
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-09-17 21:12
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@ApiModel(value = "SuperTableDescribeVO", description = "超级表结构VO")
public class SuperTableDescribeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    /**
     * 标记
     */
    @ApiModelProperty(value = "标记")
    private String note;
    /**
     * 字段名
     */
    @ApiModelProperty(value = "字段名")
    private String field;
    /**
     * 字段长度
     */
    @ApiModelProperty(value = "字段长度")
    private Integer length;
    /**
     * 字段类型
     */
    @ApiModelProperty(value = "字段类型")
    private String type;


}
