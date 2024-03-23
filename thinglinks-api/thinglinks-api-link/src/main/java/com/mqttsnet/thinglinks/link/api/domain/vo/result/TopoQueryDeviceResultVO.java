package com.mqttsnet.thinglinks.link.api.domain.vo.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @program: thinglinks
 * @description: 协议查询设备档案信息响应信息
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
@ApiModel(value = "TopoQueryDeviceResultVO", description = "设备档案响应信息")
public class TopoQueryDeviceResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "请求处理的结果码。'0'表示成功。非'0'表示失败。详见附录。", required = true)
    private int statusCode;

    @ApiModelProperty(value = "响应状态描述。")
    private String statusDesc;

    @ApiModelProperty(value = "查询设备的结果信息。", required = true)
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

        @ApiModelProperty(value = "平台生成的设备唯一标识", required = true)
        private String deviceId;

        @ApiModelProperty(value = "设备详细信息")
        private DeviceInfo deviceInfo;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DeviceInfo {

            /**
             * 客户端标识
             */
            @ApiModelProperty(value = "客户端标识")
            private String clientId;

            /**
             * 设备名称
             */
            @ApiModelProperty(value = "设备名称")
            private String deviceName;
            /**
             * 连接实例
             */
            @ApiModelProperty(value = "连接实例")
            private String connector;
            /**
             * 设备描述
             */
            @ApiModelProperty(value = "设备描述")
            private String description;
            /**
             * 设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0
             */
            @ApiModelProperty(value = "设备状态:1启用ENABLE || 2禁用DISABLE||未激活NOTACTIVE 0")
            private Integer deviceStatus;
            /**
             * 连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0
             */
            @ApiModelProperty(value = "连接状态:在线：1ONLINE || 离线：2OFFLINE || 未连接：INIT 0")
            private Integer connectStatus;
            /**
             * 设备标签
             */
            @ApiModelProperty(value = "设备标签")
            private String deviceTags;
            /**
             * 产品标识
             */
            @ApiModelProperty(value = "产品标识")
            private String productIdentification;
            /**
             * 软件版本
             */
            @ApiModelProperty(value = "软件版本")
            private String swVersion;
            /**
             * 固件版本
             */
            @ApiModelProperty(value = "固件版本")
            private String fwVersion;
            /**
             * sdk版本
             */
            @ApiModelProperty(value = "sdk版本")
            private String deviceSdkVersion;
            /**
             * 网关设备id
             */
            @ApiModelProperty(value = "网关设备id")
            private String gatewayId;
            /**
             * 设备类型:0普通设备 || 1网关设备 || 2子设备
             */
            @ApiModelProperty(value = "设备类型:0普通设备 || 1网关设备 || 2子设备 ")
            private Integer nodeType;

            /**
             * 加密密钥
             */
            @ApiModelProperty(value = "加密密钥")
            private String encryptKey;
            /**
             * 加密向量
             */
            @ApiModelProperty(value = "加密向量")
            private String encryptVector;
            /**
             * 签名密钥
             */
            @ApiModelProperty(value = "签名密钥")
            private String signKey;
            /**
             * 传输协议的加密方式：0-明文传输、1-SM4、2-AES
             */
            @ApiModelProperty(value = "传输协议的加密方式：0-明文传输、1-SM4、2-AES ")
            private Integer encryptMethod;
            /**
             * 备注
             */
            @ApiModelProperty(value = "备注")
            private String remark;
        }
    }
}