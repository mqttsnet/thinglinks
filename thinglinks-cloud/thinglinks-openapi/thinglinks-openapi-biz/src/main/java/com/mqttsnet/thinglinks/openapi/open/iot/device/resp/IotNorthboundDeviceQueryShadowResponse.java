package com.mqttsnet.thinglinks.openapi.open.iot.device.resp;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * Description:
 * 北向API-查询设备影子响应
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/01/22
 */
@Data
@Builder
public class IotNorthboundDeviceQueryShadowResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品ID
     */
    private Long id;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 产品ID（模板ID）
     */
    private Long templateId;

    /**
     * 产品标识
     */
    private String productIdentification;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品类型(0:其他, 1:普通产品, 2:网关产品)
     */
    private Integer productType;

    /**
     * 厂商ID
     */
    private String manufacturerId;

    /**
     * 厂商名称
     */
    private String manufacturerName;

    /**
     * 产品型号
     */
    private String model;

    /**
     * 数据格式
     */
    private String dataFormat;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 产品状态(0:启用, 1:停用)
     */
    private Integer productStatus;

    /**
     * 产品版本
     */
    private String activeVersionNo;

    /**
     * 图标
     */
    private String icon;

    /**
     * 产品描述
     */
    private String remark;

    /**
     * 创建人组织
     */
    private Long createdOrgId;

    /**
     * 产品服务列表
     */
    private List<ServiceItem> services;

    /**
     * 服务项
     */
    @Data
    @Builder
    public static class ServiceItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 服务ID
         */
        private Long id;

        /**
         * 产品ID
         */
        private Long productId;

        /**
         * 服务编码
         */
        private String serviceCode;

        /**
         * 服务名称
         */
        private String serviceName;

        /**
         * 服务类型
         */
        private String serviceType;

        /**
         * 服务状态(0:启用, 1:停用)
         */
        private Integer serviceStatus;

        /**
         * 服务描述
         */
        private String description;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人组织
         */
        private Long createdOrgId;

        /**
         * 服务命令列表
         */
        private List<Object> commands;

        /**
         * 服务属性列表
         */
        private List<Object> properties;
    }
}
