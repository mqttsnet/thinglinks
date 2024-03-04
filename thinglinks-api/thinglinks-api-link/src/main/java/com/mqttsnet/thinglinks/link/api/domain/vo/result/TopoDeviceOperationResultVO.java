package com.mqttsnet.thinglinks.link.api.domain.vo.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: 设备操作结果数据模型
 * @packagename: com.mqttsnet.thinglinks.device.vo.result
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-22 14:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@ApiModel(value = "TopoDeviceOperationResultVO", description = "设备操作结果数据ResultVO")
public class TopoDeviceOperationResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "请求处理的结果码", notes = "“0”表示成功。非“0”表示失败。详见附录。")
    private Integer statusCode;

    @ApiModelProperty(value = "响应状态描述", notes = "响应状态描述", required = false)
    private String statusDesc;

    @ApiModelProperty(value = "操作结果信息", notes = "操作结果信息")
    private List<OperationRsp> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Accessors(chain = true)
    @ApiModel(value = "OperationRsp", description = "操作结果结果数据模型")
    public static class OperationRsp implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "设备ID", notes = "设备ID，平台生成的设备唯一标识")
        private String deviceId;

        @ApiModelProperty(value = "请求处理的结果码", notes = "“0”表示成功。非“0”表示失败。详见附录。")
        private Integer statusCode;

        @ApiModelProperty(value = "响应状态描述", notes = "响应状态描述", required = false)
        private String statusDesc;
    }
}