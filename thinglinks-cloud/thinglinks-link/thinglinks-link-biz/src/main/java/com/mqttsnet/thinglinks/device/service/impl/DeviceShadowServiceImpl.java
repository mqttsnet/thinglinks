package com.mqttsnet.thinglinks.device.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.tds.constant.TdsConstants;
import com.mqttsnet.basic.tds.utils.TdsUtils;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.service.DeviceShadowService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceShadowPageQuery;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productproperty.vo.result.ProductPropertyResultVO;
import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import com.mqttsnet.thinglinks.productservice.vo.result.ProductServiceResultVO;
import com.mqttsnet.thinglinks.productversion.util.ProductTdsNamer;
import com.mqttsnet.thinglinks.tds.facade.TdsFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备影子业务层接口实现。
 *
 * @author ShiHuan Sun
 * @version 1.0
 * @email 13733918655@163.com
 * @date 2023-10-12 19:49
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceShadowServiceImpl implements DeviceShadowService {

    private final LinkCacheDataHelper linkCacheDataHelper;

    private final TdsFacade tdsApi;


    /**
     * 查询设备影子信息。版本路由:query.versionNo 非空则严格按该版本快照解析(支持回看历史版本影子),
     * 未指定则 fallback 到设备 boundProductVersionNo(当前实际运行版本)。
     * 失败语义:版本快照不存在 / 已 purge → 返空对象,不静默回退当前版本,避免"选 v2 却看到 v3 字段"的错位。
     *
     * @param deviceShadowPageQuery 查询参数(可选 versionNo)
     * @return 设备影子;设备 / 版本快照不存在均返空对象
     */
    @Override
    public ProductResultVO queryDeviceShadow(DeviceShadowPageQuery deviceShadowPageQuery) {
        return linkCacheDataHelper.getDeviceCacheVO(deviceShadowPageQuery.getDeviceIdentification())
                .flatMap(deviceCacheVO -> {
                    // 优先取入参 versionNo(用户主动选);其次取设备绑定版本号(默认)
                    String effectiveVersionNo = StrUtil.isNotBlank(deviceShadowPageQuery.getVersionNo())
                            ? deviceShadowPageQuery.getVersionNo()
                            : deviceCacheVO.getBoundProductVersionNo();
                    if (StrUtil.isBlank(effectiveVersionNo)) {
                        log.warn("[device-shadow] no versionNo available, deviceId={} productId={}",
                                deviceCacheVO.getDeviceIdentification(),
                                deviceCacheVO.getProductIdentification());
                        return Optional.<ProductResultVO>empty();
                    }
                    return linkCacheDataHelper.resolveProductModelByVersionNo(
                                    deviceCacheVO.getProductIdentification(),
                                    effectiveVersionNo)
                            .map(productModelCacheVO -> buildProductResultVO(
                                    deviceShadowPageQuery,
                                    productModelCacheVO,
                                    effectiveVersionNo));
                })
                .orElseGet(ProductResultVO::new);
    }

    private ProductResultVO buildProductResultVO(DeviceShadowPageQuery deviceShadowPageQuery,
                                                 ProductModelCacheVO productModelCacheVO,
                                                 String boundProductVersionNo) {
        List<ProductServiceResultVO> serviceResultVOs = buildServiceResults(
                deviceShadowPageQuery, productModelCacheVO, boundProductVersionNo);
        ProductResultVO result = BeanPlusUtil.toBeanIgnoreError(productModelCacheVO, ProductResultVO.class);
        result.setServices(serviceResultVOs);
        return result;
    }

    private List<ProductServiceResultVO> buildServiceResults(DeviceShadowPageQuery deviceShadowPageQuery,
                                                             ProductModelCacheVO productModelCacheVO,
                                                             String boundProductVersionNo) {
        List<ProductServiceParamVO> services = Optional.ofNullable(productModelCacheVO)
                .map(ProductModelCacheVO::getServices)
                .orElseGet(Collections::emptyList);
        if (services.isEmpty()) {
            return Collections.emptyList();
        }
        // 判断是否提供了 serviceCode 参数
        if (StrUtil.isNotBlank(deviceShadowPageQuery.getServiceCode())) {
            // 如果指定了 serviceCode，则只查询指定服务的结果
            return services.stream()
                    .filter(Objects::nonNull)
                    .filter(service -> Objects.equals(service.getServiceCode(), deviceShadowPageQuery.getServiceCode()))
                    .map(service -> buildServiceResultVO(deviceShadowPageQuery, productModelCacheVO,
                            BeanPlusUtil.toBeanIgnoreError(service, ProductServiceResultVO.class), boundProductVersionNo))
                    .collect(Collectors.toList());
        } else {
            // 如果没有指定 serviceCode，则查询所有服务
            return services.stream()
                    .filter(Objects::nonNull)
                    .map(service -> buildServiceResultVO(deviceShadowPageQuery, productModelCacheVO,
                            BeanPlusUtil.toBeanIgnoreError(service, ProductServiceResultVO.class), boundProductVersionNo))
                    .collect(Collectors.toList());
        }
    }

    private ProductServiceResultVO buildServiceResultVO(DeviceShadowPageQuery deviceShadowPageQuery,
                                                        ProductModelCacheVO productModelCacheVO,
                                                        ProductServiceResultVO service,
                                                        String boundProductVersionNo) {
        // 路由:用设备绑定的版本序号拼带版本的 stable,确保读取该设备实际数据所在的 TD 子表
        // (灰度场景 device.boundProductVersionNo 可能 ≠ product.activeVersionNo,必须按设备维度走)
        String productTypeDesc = ProductTypeEnum.valueOf(productModelCacheVO.getProductType()).getDesc();
        String stableName = ProductTdsNamer.superTableName(
                productTypeDesc,
                productModelCacheVO.getProductIdentification(),
                boundProductVersionNo,
                service.getServiceCode());
        List<Map<String, Object>> dataList = fetchLastData(
                TdsUtils.subTableName(stableName, deviceShadowPageQuery.getDeviceIdentification()),
                deviceShadowPageQuery);

        ProductServiceResultVO serviceResultVO = BeanPlusUtil.toBeanIgnoreError(service, ProductServiceResultVO.class);
        serviceResultVO.setProperties(buildPropertyResults(service, dataList));
        serviceResultVO.setEchoList(dataList);
        return serviceResultVO;
    }

    private List<Map<String, Object>> fetchLastData(String subTableName, DeviceShadowPageQuery deviceShadowPageQuery) {
        return Optional.ofNullable(tdsApi.getDataInRangeOrLastRecord(subTableName, deviceShadowPageQuery.getStartTime(), deviceShadowPageQuery.getEndTime()))
                .filter(R::getIsSuccess)
                .map(R::getData)
                .orElse(Collections.emptyList());
    }

    private List<ProductPropertyResultVO> buildPropertyResults(ProductServiceResultVO productServiceResultVO, List<Map<String, Object>> dataList) {
        List<Map<String, Object>> firstDataList = Optional.ofNullable(dataList)
                .flatMap(list -> list.stream().findFirst())
                .map(Collections::singletonList)
                .orElse(Collections.singletonList(Collections.emptyMap()));

        return productServiceResultVO.getProperties().stream()
                .flatMap(property -> firstDataList.stream()
                        .map(data -> {
                            ProductPropertyResultVO resultVO = buildPropertyResultVO(property, data);
                            resultVO.getEchoMap().putAll(data);
                            return resultVO;
                        }))
                .collect(Collectors.toList());
    }

    private ProductPropertyResultVO buildPropertyResultVO(ProductPropertyResultVO propertyResultVO, Map<String, Object> data) {
        Optional.ofNullable(data.get(TdsConstants.TS))
                .map(ts -> DateUtils.date2LocalDateTime(DateUtils.parseDatetime(ts.toString())))
                .ifPresent(propertyResultVO::setCreatedTime);
        propertyResultVO.setPropertyValue(data.get(propertyResultVO.getPropertyCode()));
        return propertyResultVO;
    }


}
