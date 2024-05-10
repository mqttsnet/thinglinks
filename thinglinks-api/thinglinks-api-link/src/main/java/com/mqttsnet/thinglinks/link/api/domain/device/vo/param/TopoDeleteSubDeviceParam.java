package com.mqttsnet.thinglinks.link.api.domain.device.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: 网关设备删除子设备数据模型
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:52$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:52$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@ApiModel(value = "TopoDeleteDeviceParam", description = "网关设备删除子设备数据模型")
public class TopoDeleteSubDeviceParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "网关设备标识", notes = "网关设备自身的唯一标识")
    @NotEmpty(message = "网关设备标识不能为空")
    private String gatewayIdentification;

    @ApiModelProperty(value = "子设备标识集合", notes = "子设备标识集合")
    @NotNull(message = "子设备标识集合不能为空")
    private List<String> deviceIds;
}
