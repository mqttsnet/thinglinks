package com.mqttsnet.thinglinks.link.api.domain.vo.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @program: thinglinks
 * @description: 协议添加网关子设备响应信息
 * @packagename: com.mqttsnet.thinglinks.device.vo.result
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-18 23:03
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
@ApiModel(value = "TopoAddDeviceResultVO", description = "网关子设备响应信息")
public class TopoAddDeviceResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "请求处理的结果码。'0'表示成功。非'0'表示失败。详见附录。", required = true)
    private int statusCode;

    @ApiModelProperty(value = "响应状态描述。")
    private String statusDesc;

    @ApiModelProperty(value = "添加子设备的结果信息。", required = true)
    private List<DataItem> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataItem {

        @ApiModelProperty(value = "请求处理的结果码。'0'表示成功。非'0'表示失败。详见附录。", required = true)
        private int statusCode;

        @ApiModelProperty(value = "响应状态描述。")
        private String statusDesc;

        @ApiModelProperty(value = "设备详细信息")
        private DeviceInfo deviceInfo;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DeviceInfo {

            @ApiModelProperty(value = "设备名称", required = true)
            private String name;

            @ApiModelProperty(value = "厂商ID", required = true)
            private String manufacturerId;

            @ApiModelProperty(value = "设备描述")
            private String description;

            @ApiModelProperty(value = "设备型号")
            private String model;

            @ApiModelProperty(value = "平台生成的设备唯一标识", required = true)
            private String deviceId;

            @ApiModelProperty(value = "设备自身的唯一标识", required = true)
            private String nodeId;
        }
    }
}