package com.mqttsnet.thinglinks.link.api.domain.device.vo.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 表单查询条件VO
 * 设备命令下发及响应表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-10-20 17:27:25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "DeviceCommandPageQuery", description = "设备命令下发及响应表")
public class DeviceCommandPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;
    /**
     * 命令标识
     */
    @ApiModelProperty(value = "命令标识")
    private String commandIdentification;
    /**
     * 命令类型(0:命名下发、1:命令响应)
     */
    @ApiModelProperty(value = "命令类型(0:命名下发、1:命令响应)")
    private Integer commandType;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    private Integer status;
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    private String content;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;


}
