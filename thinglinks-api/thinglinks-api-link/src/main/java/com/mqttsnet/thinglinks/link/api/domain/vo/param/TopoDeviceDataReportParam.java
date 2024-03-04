package com.mqttsnet.thinglinks.link.api.domain.vo.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: 设备数据上报数据模型
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
@ApiModel(value = "TopoDeviceDataReportParam", description = "设备数据上报数据模型")
public class TopoDeviceDataReportParam implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备数据", notes = "设备数据")
    @NotNull(message = "设备数据不能为空")
    private List<DeviceS> devices;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Accessors(chain = true)
    @ApiModel(value = "DeviceS", description = "设备数据模型")
    public static class DeviceS implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "设备唯一标识", notes = "平台生成的设备唯一标识")
        @NotEmpty(message = "设备唯一标识不能为空")
        private String deviceId;

        @ApiModelProperty(value = "服务列表", notes = "服务列表")
        @NotNull(message = "服务列表不能为空")
        private List<Services> services;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Accessors(chain = true)
        @ApiModel(value = "Services", description = "服务数据模型")
        public static class Services implements Serializable {
            private static final long serialVersionUID = 1L;

            @ApiModelProperty(value = "服务编码", notes = "服务编码，对应平台产品服务编码")
            @NotEmpty(message = "服务编码不能为空")
            private String serviceCode;

            @ApiModelProperty(value = "服务数据", notes = "服务数据，不固定内容")
            @NotNull(message = "服务数据不能为空")
            private Object data;

            @ApiModelProperty(value = "事件时间", notes = "时间格式：13位毫秒时间戳。例如，1622552643000表示2021年6月1日17时24分3秒（UTC时间）。")
            @NotEmpty(message = "事件时间不能为空")
            private Long eventTime;
        }
    }
}
