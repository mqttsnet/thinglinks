package com.mqttsnet.thinglinks.link.api.domain.product.vo.result;

import cn.hutool.core.map.MapUtil;;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 表单查询方法返回值VO
 * 产品模型设备服务命令表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@ApiModel(value = "ProductCommandResultVO", description = "产品模型设备服务命令表")
public class ProductCommandResultVO implements Serializable  {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @ApiModelProperty(value = "命令id")
    private Long id;

    /**
     * 服务ID
     */
    @ApiModelProperty(value = "服务ID")
    private Long serviceId;
    /**
     * 指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。
     */
    @ApiModelProperty(value = "指示命令的编码，如门磁的LOCK命令、摄像头的VIDEO_RECORD命令，命令名与参数共同构成一个完整的命令。支持英文大小写、数字及下划线，长度[2,50]。")
    private String commandCode;
    /**
     * 指示命令名称
     */
    @ApiModelProperty(value = "指示命令名称")
    private String commandName;
    /**
     * 命令描述。
     */
    @ApiModelProperty(value = "命令描述。")
    private String description;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;

    @ApiModelProperty(value = "产品请求服务命令属性")
    private List<ProductCommandRequestResultVO> requests;

    @ApiModelProperty(value = "产品响应服务命令属性")
    private List<ProductCommandResponseResultVO> responses;

}
