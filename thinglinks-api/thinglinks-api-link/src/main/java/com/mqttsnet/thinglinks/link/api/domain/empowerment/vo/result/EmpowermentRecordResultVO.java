package com.mqttsnet.thinglinks.link.api.domain.empowerment.vo.result;

import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 表单查询方法返回值VO
 * 赋能记录表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-15 17:20:27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@ApiModel(value = "EmpowermentRecordResultVO", description = "赋能记录")
public class EmpowermentRecordResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 应用ID
     */
    @ApiModelProperty(value = "应用ID")
    private String appId;
    /**
     * 赋能标识
     */
    @ApiModelProperty(value = "赋能标识")
    private String empowermentIdentification;
    /**
     * 赋能类型
     */
    @ApiModelProperty(value = "赋能类型")
    private Integer empowermentType;
    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;
    /**
     * 赋能结果
     */
    @ApiModelProperty(value = "赋能结果")
    private String outcome;
    /**
     * 赋能反馈
     */
    @ApiModelProperty(value = "赋能反馈")
    private String feedback;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer status;
    /**
     * 版本
     */
    @ApiModelProperty(value = "版本")
    private String version;
    /**
     * 依赖关系
     */
    @ApiModelProperty(value = "依赖关系")
    private String dependencies;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;


}
