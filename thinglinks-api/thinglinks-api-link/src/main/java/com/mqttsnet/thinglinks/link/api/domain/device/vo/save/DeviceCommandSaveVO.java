package com.mqttsnet.thinglinks.link.api.domain.device.vo.save;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 表单保存方法VO
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
@ApiModel(value = "DeviceCommandSaveVO", description = "设备命令下发及响应表")
public class DeviceCommandSaveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    @NotEmpty(message = "请填写设备标识")
    @Size(max = 255, message = "设备标识长度不能超过{max}")
    private String deviceIdentification;

    /**
     * 命令类型(0:命名下发、1:命令响应)
     */
    @ApiModelProperty(value = "命令类型(0:命名下发、1:命令响应)")
    @NotNull(message = "请填写命令类型(0:命名下发、1:命令响应)")
    private Integer commandType;
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态")
    @NotNull(message = "请填写状态")
    private Integer status;
    /**
     * 内容
     */
    @ApiModelProperty(value = "内容")
    @Size(max = 2147483647, message = "内容长度不能超过{max}")
    private String content;
    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;


}
