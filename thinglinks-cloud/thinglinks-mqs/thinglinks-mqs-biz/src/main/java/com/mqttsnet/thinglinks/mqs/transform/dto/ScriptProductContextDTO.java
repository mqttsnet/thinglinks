package com.mqttsnet.thinglinks.mqs.transform.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 脚本绑定 · 产品上下文 DTO。
 *
 * <p>从 {@code ProductCacheVO} 映射而来,作为前置转换 Groovy 脚本里 {@code product} 绑定变量的承载体。
 * 独立 DTO,不暴露 {@code echoMap} 等缓存内部字段;不污染原缓存。
 *
 * <p>脚本里用法:{@code product.protocolType} / {@code product.dataFormat} / {@code product.model} …
 *
 * @author mqttsnet
 */
@Data
public class ScriptProductContextDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String appId;
    private Long templateId;
    private String productName;
    private String productIdentification;
    /** 产品类型:1普通 2网关 0未知 */
    private Integer productType;
    private String manufacturerId;
    private String manufacturerName;
    private String model;
    private String dataFormat;
    private String deviceType;
    private String protocolType;
    /** 状态:0启用 1停用 */
    private Integer productStatus;
    /** 当前生效版本序号 */
    private String activeVersionNo;
    private String icon;
    private String remark;
    private Long createdOrgId;
}
