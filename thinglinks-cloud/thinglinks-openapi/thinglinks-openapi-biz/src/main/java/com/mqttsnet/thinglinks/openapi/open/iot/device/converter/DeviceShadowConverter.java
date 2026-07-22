package com.mqttsnet.thinglinks.openapi.open.iot.device.converter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.thinglinks.openapi.open.iot.device.resp.IotNorthboundDeviceQueryShadowResponse;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productservice.vo.result.ProductServiceResultVO;
import lombok.experimental.UtilityClass;

/**
 * Description:
 * 北向API-设备影子查询响应转换器
 * 负责将内部 ProductResultVO 转换为 OpenAPI 响应类
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026/01/22
 */
@UtilityClass
public class DeviceShadowConverter {

    /**
     * 转换 ProductResultVO 为 IotNorthboundDeviceQueryShadowResponse
     *
     * @param productResultVO 产品结果VO
     * @return 北向API设备影子查询响应
     */
    public static IotNorthboundDeviceQueryShadowResponse convert(ProductResultVO productResultVO) {
        if (Objects.isNull(productResultVO)) {
            return null;
        }

        return IotNorthboundDeviceQueryShadowResponse.builder()
                .id(productResultVO.getId())
                .appId(productResultVO.getAppId())
                .templateId(productResultVO.getTemplateId())
                .productIdentification(productResultVO.getProductIdentification())
                .productName(productResultVO.getProductName())
                .productType(productResultVO.getProductType())
                .manufacturerId(productResultVO.getManufacturerId())
                .manufacturerName(productResultVO.getManufacturerName())
                .model(productResultVO.getModel())
                .dataFormat(productResultVO.getDataFormat())
                .deviceType(productResultVO.getDeviceType())
                .protocolType(productResultVO.getProtocolType())
                .productStatus(productResultVO.getProductStatus())
                .activeVersionNo(productResultVO.getActiveVersionNo())
                .icon(productResultVO.getIcon())
                .remark(productResultVO.getRemark())
                .createdOrgId(productResultVO.getCreatedOrgId())
                .services(convertServices(productResultVO.getServices()))
                .build();
    }

    /**
     * 转换产品服务列表
     *
     * @param services 产品服务VO列表
     * @return 服务项列表
     */
    private static List<IotNorthboundDeviceQueryShadowResponse.ServiceItem> convertServices(List<ProductServiceResultVO> services) {
        if (CollUtil.isEmpty(services)) {
            return Collections.emptyList();
        }

        return services.stream()
                .map(DeviceShadowConverter::convertService)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个产品服务
     *
     * @param service 产品服务VO
     * @return 服务项
     */
    private static IotNorthboundDeviceQueryShadowResponse.ServiceItem convertService(ProductServiceResultVO service) {
        if (Objects.isNull(service)) {
            return null;
        }
        return IotNorthboundDeviceQueryShadowResponse.ServiceItem.builder()
                .id(service.getId())
                .productId(service.getProductId())
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .serviceType(service.getServiceType())
                .serviceStatus(service.getServiceStatus())
                .description(service.getDescription())
                .remark(service.getRemark())
                .createdOrgId(service.getCreatedOrgId())
                .commands(CollUtil.isNotEmpty(service.getCommands()) ? service.getCommands().stream().map(Object.class::cast).collect(Collectors.toList()) : Collections.emptyList())
                .properties(CollUtil.isNotEmpty(service.getProperties()) ? service.getProperties().stream().map(Object.class::cast).collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
}
