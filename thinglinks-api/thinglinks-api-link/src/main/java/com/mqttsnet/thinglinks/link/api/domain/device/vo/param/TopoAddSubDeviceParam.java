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
 * @Description: 网关设备添加子设备数据模型
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
@ApiModel(value = "TopoAddDeviceSaveVO", description = "网关设备添加子设备数据模型")
public class TopoAddSubDeviceParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "网关设备标识", notes = "网关设备自身的唯一标识")
    @NotEmpty(message = "网关设备标识不能为空")
    private String gatewayIdentification;

    @ApiModelProperty(value = "子设备信息集合", notes = "子设备信息集合")
    @NotNull(message = "子设备信息集合不能为空")
    private List<DeviceInfos> deviceInfos;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfos {

        @ApiModelProperty(value = "子设备ID", notes = "子设备自身的唯一标识")
        private String nodeId;

        @ApiModelProperty(value = "子设备名称", notes = "子设备名称")
        private String name;

        @ApiModelProperty(value = "子设备描述", notes = "子设备描述")
        private String description;

        @ApiModelProperty(value = "子设备厂商ID", notes = "子设备厂商ID")
        private String manufacturerId;

        @ApiModelProperty(value = "子设备型号", notes = "子设备型号")
        private String model;
    }
}
