package com.mqttsnet.thinglinks.openapi.open.iot.product.resp;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * 北向API-查询产品物模型响应结果
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/02/02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotNorthboundProductGetThingModelResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     * @mock APP_001
     */
    private String appId;

    /**
     * 产品标识
     * @mock PROD_001
     */
    private String productIdentification;

    /**
     * 模板ID
     * @mock 1001
     */
    private Long templateId;

    /**
     * 产品名称
     * @mock 智能温湿度传感器
     */
    private String productName;

    /**
     * 产品类型：1-普通产品COMMON，2-网关产品GATEWAY，0-其他未知产品
     * @mock 1
     */
    private Integer productType;

    /**
     * 厂商ID
     * @mock MANUFACTURER_001
     */
    private String manufacturerId;

    /**
     * 厂商名称
     * @mock 物联网科技有限公司
     */
    private String manufacturerName;

    /**
     * 产品型号
     * @mock TH-S100
     */
    private String model;

    /**
     * 数据格式
     * @mock JSON
     */
    private String dataFormat;

    /**
     * 设备类型
     * @mock sensor
     */
    private String deviceType;

    /**
     * 协议类型：MQTT、HTTP、MODBUS、OPC_UA
     * @mock MQTT
     */
    private String protocolType;

    /**
     * 产品版本
     * @mock 1.0.0
     */
    private String activeVersionNo;

    /**
     * 产品描述/备注
     * @mock 用于采集环境温湿度数据
     */
    private String remark;

    /**
     * 产品模型服务列表
     */
    private List<ThingModelService> services;

    /**
     * 物模型服务
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingModelService implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 产品ID
         * @mock 1001
         */
        private Long productId;

        /**
         * 服务编码:支持英文大小写、数字、下划线和中划线
         * @mock temperature_humidity
         */
        private String serviceCode;

        /**
         * 服务名称
         * @mock 温湿度服务
         */
        private String serviceName;

        /**
         * 服务类型
         * @mock sensor
         */
        private String serviceType;

        /**
         * 状态(0启用 1停用)
         * @mock 0
         */
        private Integer serviceStatus;

        /**
         * 服务的描述信息
         * @mock 温湿度数据采集服务
         */
        private String description;

        /**
         * 备注
         * @mock 温湿度服务备注
         */
        private String remark;

        /**
         * 服务命令列表
         */
        private List<ThingModelCommand> commands;

        /**
         * 服务属性列表
         */
        private List<ThingModelProperty> properties;
    }

    /**
     * 物模型属性
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingModelProperty implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 服务ID
         * @mock 1001
         */
        private Long serviceId;

        /**
         * 属性编码
         * @mock temperature
         */
        private String propertyCode;

        /**
         * 属性名称
         * @mock 温度
         */
        private String propertyName;

        /**
         * 数据类型：string、int、decimal、DateTime、jsonObject
         * @mock decimal
         */
        private String datatype;

        /**
         * 属性描述
         * @mock 环境温度值
         */
        private String description;

        /**
         * 枚举值列表
         * @mock OPEN,CLOSE
         */
        private String enumlist;

        /**
         * 最大值（仅当dataType为int、decimal时生效）
         * @mock 100
         */
        private String max;

        /**
         * 字符串最大长度（仅当dataType为string、DateTime时生效）
         * @mock 255
         */
        private String maxlength;

        /**
         * 访问模式：R可读、W可写、E属性值更改时上报数据，取值范围：R、RW、RE、RWE
         * @mock RWE
         */
        private String method;

        /**
         * 最小值（仅当dataType为int、decimal时生效）
         * @mock -40
         */
        private String min;

        /**
         * 是否必填：0非必填 1必填
         * @mock 1
         */
        private String required;

        /**
         * 步长
         * @mock 0.1
         */
        private String step;

        /**
         * 单位
         * @mock ℃
         */
        private String unit;

        /**
         * 图标
         * @mock temperature.png
         */
        private String icon;

        /**
         * 备注
         * @mock 温度属性备注
         */
        private String remark;
    }

    /**
     * 物模型命令
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingModelCommand implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 服务ID
         * @mock 1001
         */
        private Long serviceId;

        /**
         * 命令编码
         * @mock SET_TEMPERATURE
         */
        private String commandCode;

        /**
         * 命令名称
         * @mock 设置温度阈值
         */
        private String commandName;

        /**
         * 命令描述
         * @mock 设置温度报警阈值
         */
        private String description;

        /**
         * 备注
         * @mock 命令备注
         */
        private String remark;

        /**
         * 命令请求参数列表
         */
        private List<ThingModelCommandRequest> requests;

        /**
         * 命令响应参数列表
         */
        private List<ThingModelCommandResponse> responses;
    }

    /**
     * 物模型命令请求参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingModelCommandRequest implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 服务ID
         * @mock 1001
         */
        private Long serviceId;

        /**
         * 命令ID
         * @mock 2001
         */
        private Long commandId;

        /**
         * 参数编码
         * @mock threshold
         */
        private String parameterCode;

        /**
         * 参数名称
         * @mock 阈值
         */
        private String parameterName;

        /**
         * 参数描述
         * @mock 温度报警阈值
         */
        private String parameterDescription;

        /**
         * 数据类型：string、int、decimal
         * @mock decimal
         */
        private String datatype;

        /**
         * 枚举值列表
         * @mock
         */
        private String enumlist;

        /**
         * 最大值
         * @mock 100
         */
        private String max;

        /**
         * 字符串最大长度
         * @mock 255
         */
        private String maxlength;

        /**
         * 最小值
         * @mock -40
         */
        private String min;

        /**
         * 是否必填：0非必填 1必填
         * @mock 1
         */
        private String required;

        /**
         * 步长
         * @mock 0.1
         */
        private String step;

        /**
         * 单位
         * @mock ℃
         */
        private String unit;

        /**
         * 备注
         * @mock 请求参数备注
         */
        private String remark;
    }

    /**
     * 物模型命令响应参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingModelCommandResponse implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 命令ID
         * @mock 2001
         */
        private Long commandId;

        /**
         * 服务ID
         * @mock 1001
         */
        private Long serviceId;

        /**
         * 参数编码
         * @mock result
         */
        private String parameterCode;

        /**
         * 参数名称
         * @mock 执行结果
         */
        private String parameterName;

        /**
         * 参数描述
         * @mock 命令执行结果
         */
        private String parameterDescription;

        /**
         * 数据类型：string、int、decimal
         * @mock string
         */
        private String datatype;

        /**
         * 枚举值列表
         * @mock SUCCESS,FAILED
         */
        private String enumlist;

        /**
         * 最大值
         * @mock
         */
        private String max;

        /**
         * 字符串最大长度
         * @mock 255
         */
        private String maxlength;

        /**
         * 最小值
         * @mock
         */
        private String min;

        /**
         * 是否必填：0非必填 1必填
         * @mock 1
         */
        private String required;

        /**
         * 步长
         * @mock
         */
        private String step;

        /**
         * 单位
         * @mock
         */
        private String unit;

        /**
         * 备注
         * @mock 响应参数备注
         */
        private String remark;
    }

}
