package com.mqttsnet.thinglinks.mqs.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.tds.constant.TdsConstants;
import com.mqttsnet.basic.tds.enumeration.TdDataTypeEnum;
import com.mqttsnet.basic.tds.model.Fields;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.tds.utils.TdsUtils;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.basic.utils.StringUtils;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductCacheVO;
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.device.manager.DeviceManager;
import com.mqttsnet.thinglinks.mqs.event.report.DeviceDataReportContext;
import com.mqttsnet.thinglinks.mqs.event.report.DeviceDataReportPostProcessor;
import com.mqttsnet.thinglinks.mqs.service.DeviceDataProcessingService;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productproperty.vo.result.ProductPropertyResultVO;
import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import com.mqttsnet.thinglinks.productservice.vo.result.ProductServiceResultVO;
import com.mqttsnet.thinglinks.productversion.util.ProductTdsNamer;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.tds.facade.TdsFacade;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Description:
 * 设备数据处理服务实现类
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/10/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceDataProcessingServiceImpl implements DeviceDataProcessingService {

    private final TdsFacade tdsApi;

    private final LinkCacheDataHelper linkCacheDataHelper;

    /**
     * 兜底写回 device.bound_product_version_no 用:设备 boundProductVersionNo 为空且本次上报靠
     * productCacheVO.activeVersionNo 兜底解析出版本号时,异步把它写回 DB,避免每次上报都走兜底分支,后续路由稳定。
     */
    private final DeviceManager deviceManager;

    /**
     * 数据上报后置处理器链(SPI)── 物模型匹配落库后触发,默认实现把结构化数据桥接出站;
     * 二开可再注册 {@code @Component} 实现自定义输出。无实现时为空列表,零开销。
     */
    private final List<DeviceDataReportPostProcessor> dataReportPostProcessors;


    @Override
    public void processDeviceDataReport(TopoDeviceDataReportParam dataReportParam) {
        if (Objects.isNull(dataReportParam)) {
            log.warn("processDeviceDataReport dataReportParam is null");
            return;
        }
        log.info("processDeviceDataReport....dataReportParam:{}", JSON.toJSONString(dataReportParam));
        dataReportParam.getDevices().forEach(device -> {
            log.info("processingDeviceDataTopic Processing result:{}", JSON.toJSONString(device));
            String deviceId = device.getDeviceId();
            Optional<DeviceCacheVO> deviceCacheVOOptional = linkCacheDataHelper.getDeviceCacheVO(deviceId);
            if (deviceCacheVOOptional.isEmpty()) {
                log.warn("processingDeviceDataTopic Device not found:{}", deviceId);
                return;
            }
            DeviceCacheVO deviceCacheVO = deviceCacheVOOptional.get();
            // 路由:优先取设备绑定的版本号;
            //       boundProductVersionNo 为空时回退到产品基础元数据缓存的 activeVersionNo
            //       (向后兼容未走 REGISTER 路径的存量设备 ── 产品元数据走 getProductCacheVO,
            //        productCacheVO 不再内嵌在 deviceCacheVO 上)。
            String boundProductVersionNo = deviceCacheVO.getBoundProductVersionNo();
            boolean rebuiltFromFallback = false;
            if (StrUtil.isBlank(boundProductVersionNo)) {
                // 灰度态(previousFullVersionNo 非空)回退到稳定版而非 activeVersionNo(灰度版):未入灰度组的
                // 存量空版本设备不应把遥测写到灰度超表,与新设备绑稳定版口径一致(见 DeviceServiceImpl#resolveBindVersionForNewDevice)。
                boundProductVersionNo = linkCacheDataHelper
                    .getProductCacheVO(deviceCacheVO.getProductIdentification())
                    .map(p -> StrUtil.blankToDefault(p.getPreviousFullVersionNo(), p.getActiveVersionNo()))
                    .orElse(null);
                rebuiltFromFallback = StrUtil.isNotBlank(boundProductVersionNo);
            }
            if (StrUtil.isBlank(boundProductVersionNo)) {
                log.warn("processingDeviceDataTopic boundProductVersionNo cannot be resolved (device + product activeVersionNo both blank), deviceId:{} productIdentification:{}",
                    deviceId, deviceCacheVO.getProductIdentification());
                return;
            }
            // FALLBACK 时机:此次上报兑底拿到了版本号,异步回写 device 表 + 失效设备缓存,后续上报走 REGISTER 路径。
            if (rebuiltFromFallback) {
                asyncBackfillBoundProductVersion(deviceCacheVO.getDeviceIdentification(), boundProductVersionNo);
            }

            // Optional 风格 ── helper 已自带 read-through DB 回源 (product_version 表反序列化),
            // 此处仅处理 DB 也查不到的极端场景。
            // 灰度路由的关键:按"设备绑定的版本号"解析物模型,灰度场景设备绑了旧版,影子也读旧版。
            // 影子发布(SHADOW)说明:影子是"预建表 + 外部切流"模型 ── 这里只按设备 boundProductVersionNo 路由、
            // 写其绑定版本的表;设备被外部(手动 / 其他业务系统)改绑到影子版本后,本热路径自然把它的数据写进
            // 预建好的影子表。发布时不会、也不需要旁路双写影子超表,影子表在设备切过来之前是空表属预期。
            Optional<ProductModelCacheVO> productModelCacheVOOptional =
                linkCacheDataHelper.resolveProductModelByVersionNo(deviceCacheVO.getProductIdentification(), boundProductVersionNo);
            if (productModelCacheVOOptional.isEmpty()) {
                log.warn("processingDeviceDataTopic Product Model not found (cache + DB miss)...productIdentification:{} versionNo:{} tenantId={} deviceId={}",
                    deviceCacheVO.getProductIdentification(), boundProductVersionNo,
                    deviceCacheVO.getTenantId(), deviceId);
                return;
            }
            ProductModelCacheVO productModelCacheVO = productModelCacheVOOptional.get();
            // productType 直接取自 ProductModelCacheVO 快照(版本一致,无须再查 ProductCacheVO)
            Integer productType = productModelCacheVO.getProductType();
            if (productType == null) {
                log.warn("processingDeviceDataTopic productType is null on snapshot, deviceId:{} productIdentification:{} versionNo:{}", deviceId, deviceCacheVO.getProductIdentification(), boundProductVersionNo);
                return;
            }
            String productTypeDesc = ProductTypeEnum.valueOf(productType).getDesc();

            Map<String, Map<String, Object>> dataMap = new HashMap<>();

            final String boundProductVersionNoForRouting = boundProductVersionNo;
            device.getServices().forEach(service -> {
                String superTableName = ProductTdsNamer.superTableName(
                    productTypeDesc,
                    deviceCacheVO.getProductIdentification(),
                    boundProductVersionNoForRouting,
                    service.getServiceCode());

                String subTableName = TdsUtils.subTableName(superTableName, deviceCacheVO.getDeviceIdentification());

                // 超表结构缓存按 (pi, versionNo, serviceCode, deviceIdentification) 维度切分,
                // 必须带 boundProductVersionNoForRouting(灰度场景设备绑旧版,TD 子表也是旧版结构)
                List<SuperTableDescribeVO> productModelSuperTableCacheVO = linkCacheDataHelper.getProductModelSuperTableCacheVO(
                    deviceCacheVO.getProductIdentification(),
                    boundProductVersionNoForRouting,
                    service.getServiceCode(),
                    deviceCacheVO.getDeviceIdentification());

                // 如果为空，需要做设备的初始化动作，并缓存模型表结构
                if (CollUtil.isEmpty(productModelSuperTableCacheVO)) {
                    R<List<SuperTableDescribeVO>> superTableDescribeVOListR = tdsApi.describeSuperOrSubTable(subTableName);
                    List<SuperTableDescribeVO> existingFields = Optional.ofNullable(superTableDescribeVOListR.getData()).orElse(Collections.emptyList());
                    if (CollUtil.isNotEmpty(existingFields)) {
                        productModelSuperTableCacheVO = existingFields;
                    } else {
                        log.info("设备子表初始化，设备标识：{}，服务标识：{}", deviceCacheVO.getDeviceIdentification(), service.getServiceCode());
                        TableDTO tableDTO = new TableDTO();
                        tableDTO.setSuperTableName(superTableName);
                        tableDTO.setTableName(subTableName);
                        List<Fields> tagsFieldValues = new ArrayList<>();
                        Fields fields = new Fields();
                        fields.setFieldName(TdsConstants.DEVICE_IDENTIFICATION);
                        fields.setFieldValue(deviceCacheVO.getDeviceIdentification());
                        fields.setDataType(TdDataTypeEnum.BINARY);
                        tagsFieldValues.add(fields);
                        tableDTO.setTagsFieldValues(tagsFieldValues);
                        R subTable = tdsApi.createSubTable(tableDTO);
                        if (Boolean.FALSE.equals(subTable.getIsSuccess())) {
                            log.error("设备子表初始化 ，设备标识：{}，服务标识：{}，初始化失败", deviceCacheVO.getDeviceIdentification(), service.getServiceCode());
                            return;
                        }
                        log.info("设备子表初始化，设备标识：{}，服务标识：{}，初始化成功", deviceCacheVO.getDeviceIdentification(), service.getServiceCode());
                        // 查询新的表结构信息存redis，并更新本地变量
                        productModelSuperTableCacheVO = Optional.ofNullable(tdsApi.describeSuperOrSubTable(superTableName).getData()).orElse(Collections.emptyList());
                    }

                    linkCacheDataHelper.setProductModelSuperTableCacheVO(deviceCacheVO.getProductIdentification(),
                        boundProductVersionNoForRouting,
                        service.getServiceCode(),
                        deviceCacheVO.getDeviceIdentification(),
                        productModelSuperTableCacheVO);
                }

                // 数据上报时间(纳秒时间戳)
                Long eventTime = Optional.ofNullable(service.getEventTime())
                    .map(DateUtils::convertOrGenerateNanoseconds)
                    .orElse(DateUtils.nanosecondStampL());


                List<Fields> schemaFieldsList = new ArrayList<>();
                List<Fields> tagsFieldsList = new ArrayList<>();

                Map<String, Object> data = StringUtils.jsonToMap(service.getData().toString());
                dataMap.put(service.getServiceCode(), data);

                productModelSuperTableCacheVO.forEach(superTableDescribeVO -> {
                    Fields fields = new Fields();
                    fields.setFieldName(superTableDescribeVO.getField());
                    TdDataTypeEnum tdDataTypeEnum = TdDataTypeEnum.valueOfByDataType(superTableDescribeVO.getType());
                    fields.setDataType(tdDataTypeEnum);
                    fields.setSize(superTableDescribeVO.getLength());

                    // 按字段名(即属性编码原值)从上报数据取值 ── 编码已在录入时统一规范,系统不做大小写转换
                    String field = superTableDescribeVO.getField();
                    if (field != null && data.containsKey(field)) {
                        fields.setFieldValue(data.get(field));
                    } else if (TdsConstants.EVENT_TIME.equals(superTableDescribeVO.getField())) {
                        // 需要校验下是否为 event_time 时间戳
                        fields.setFieldValue(eventTime);
                    } else if (TdsConstants.TS.equals(superTableDescribeVO.getField())) {
                        // 需要校验下是否为 ts 纳秒时间戳
                        fields.setFieldValue(DateUtils.nanosecondStampL());
                    }

                    if (TdsConstants.TAG.equals(superTableDescribeVO.getNote())) {
                        if (!StringUtils.isEmpty(superTableDescribeVO.getField()) && TdsConstants.DEVICE_IDENTIFICATION.equals(superTableDescribeVO.getField())) {
                            fields.setFieldValue(deviceCacheVO.getDeviceIdentification());
                        }
                        tagsFieldsList.add(fields);
                    } else {
                        schemaFieldsList.add(fields);
                    }
                });


                //字段信息对象集合通过stream流过滤掉没有字段值的字段对象
                List<Fields> schemaFieldsStream = schemaFieldsList.stream().filter(fields -> fields.getFieldValue() != null).collect(Collectors.toList());
                //字段信息对象集合通过stream流过滤掉没有字段值的字段对象
                List<Fields> tagsFieldsStream = tagsFieldsList.stream().filter(fields -> fields.getFieldValue() != null).collect(Collectors.toList());
                //如果字段值只有第一个字段的时间戳，说明上报的数据没有符合该服务的属性，不做保存操作，跳过该循环，进入下个循环
                if (CollUtil.isEmpty(schemaFieldsStream)) {
                    return;
                }
                //设置插入所需参数
                TableDTO tableDTO = new TableDTO();
                tableDTO.setSuperTableName(superTableName);
                tableDTO.setTableName(subTableName);
                tableDTO.setSchemaFieldValues(schemaFieldsStream);
                tableDTO.setTagsFieldValues(tagsFieldsStream);

                log.info("insertTableData param:{}", JSON.toJSONString(tableDTO));
                R insertedResult = tdsApi.insertTableData(tableDTO);
                if (Boolean.TRUE.equals(insertedResult.getIsSuccess())) {
                    log.info("insert  table data success, tableName:{}", subTableName);
                } else {
                    log.error("insert  table data failed, tableName:{}..errorMsg:{}", subTableName, insertedResult.getErrorMsg());
                }

            });

            ProductResultVO productResultVO = Optional.of(productModelCacheVO)
                .map(item -> {
                    // 直接构建服务结果，如果productModelCacheVO为空，则会返回一个空列表
                    List<ProductServiceResultVO> serviceResultVOs = buildServiceResults(dataMap, productModelCacheVO);
                    // 复制属性并设置服务结果
                    ProductResultVO result = BeanPlusUtil.toBeanIgnoreError(productModelCacheVO, ProductResultVO.class);
                    result.setServices(serviceResultVOs);
                    return result;
                })
                .orElseGet(() -> {
                    // 只有当deviceCacheVO为null或者没有找到ProductModelCacheVO时，才会返回一个新的ProductResultVO实例
                    ProductResultVO emptyResult = new ProductResultVO();
                    emptyResult.setServices(Collections.emptyList());
                    return emptyResult;
                });

            // 写入数据收集池
            linkCacheDataHelper.setDeviceDataCollectionPoolCacheVO(Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
                deviceCacheVO.getDeviceIdentification(), productResultVO);

            // 物模型匹配落库后:触发数据上报后置处理器(默认把结构化数据桥接出站),失败不阻断主链路
            fireDataReportPostProcessors(deviceCacheVO, productResultVO, boundProductVersionNo);
        });
    }

    /**
     * 物模型匹配落库后触发后置处理器链(SPI)── 默认实现把结构化数据以 THING_MODEL 形态桥接出站。
     * 任一处理器异常只 warn 不阻断上报主链路;无处理器时直接返回。
     *
     * @param deviceCacheVO         设备缓存(身份)
     * @param productResultVO       物模型匹配后的结构化数据
     * @param boundProductVersionNo 设备绑定的产品发布版本号
     */
    private void fireDataReportPostProcessors(DeviceCacheVO deviceCacheVO, ProductResultVO productResultVO, String boundProductVersionNo) {
        // 无后置处理器 / 物模型未命中任何服务(结构化数据为空)时跳过,避免桥接近空的 THING_MODEL 事件造成下游噪声
        if (CollUtil.isEmpty(dataReportPostProcessors) || productResultVO == null
            || CollUtil.isEmpty(productResultVO.getServices())) {
            return;
        }
        DeviceDataReportContext ctx = DeviceDataReportContext.builder()
            .appId(deviceCacheVO.getAppId())
            .productIdentification(deviceCacheVO.getProductIdentification())
            .deviceIdentification(deviceCacheVO.getDeviceIdentification())
            .boundProductVersionNo(boundProductVersionNo)
            .ts(System.currentTimeMillis())
            .normalized(productResultVO)
            .build();
        dataReportPostProcessors.forEach(p -> {
            try {
                if (p.supports(ctx)) {
                    p.onReported(ctx);
                }
            } catch (Exception ex) {
                log.warn("[DataReportPostProcessor] {} failed (non-blocking) device={} err={}",
                    p.getClass().getSimpleName(), deviceCacheVO.getDeviceIdentification(), ex.getMessage());
            }
        });
    }

    private List<ProductServiceResultVO> buildServiceResults(Map<String, Map<String, Object>> dataMap, ProductModelCacheVO productModelCacheVO) {
        // Check if the productModelCacheVO is null or if getServices() returns null to avoid NullPointerException.
        if (productModelCacheVO == null || productModelCacheVO.getServices() == null) {
            return Collections.emptyList();
        }

        // If dataMap is null, replace it with an empty map to avoid NullPointerException when calling get().
        Map<String, Map<String, Object>> safeDataMap = (dataMap != null) ? dataMap : Collections.emptyMap();

        return productModelCacheVO.getServices().stream()
            .map(productServiceParamVO -> {
                // Use the safeDataMap for getting the service data, handle potential null value from the map.
                Map<String, Object> serviceData = safeDataMap.getOrDefault(productServiceParamVO.getServiceCode(), Collections.emptyMap());

                // Build the property results, which also handles nulls inside the method.
                List<ProductPropertyResultVO> propertyResultVOs = buildPropertyResults(productServiceParamVO, serviceData);

                // Convert the service parameter object into a result object and set the properties.
                ProductServiceResultVO serviceResultVO = BeanPlusUtil.toBeanIgnoreError(productServiceParamVO, ProductServiceResultVO.class);
                serviceResultVO.setProperties(propertyResultVOs); // This will include the property results, even if empty.

                return serviceResultVO;
            })
            .collect(Collectors.toList());
    }


    /**
     * 兜底版本号异步回写:设备 boundProductVersionNo 为空、本次靠 activeVersionNo 兜底解析出版本号时,
     * 把它写回 device.bound_product_version_no 让后续上报稳定走该版本。
     * 主线程 ContextUtil 快照传子线程保证 @DS 切库正确;{@link DeviceManager#fillBoundProductVersionIfBlank} 做 CAS
     * (仅 DB 当前值仍为 NULL 时写);写成功后 {@link LinkCacheDataHelper#deleteDeviceCacheVO} 失效缓存,
     * 下次 read-through 拉到新值;失败仅 warn 不抛,数据上报主路径不受影响。
     *
     * @param deviceIdentification 设备标识
     * @param version              兜底解析出的产品版本号
     */
    private void asyncBackfillBoundProductVersion(String deviceIdentification, String version) {
        if (StrUtil.isBlank(deviceIdentification) || StrUtil.isBlank(version)) {
            return;
        }
        Map<String, String> ctx = ContextUtil.getLocalMap();
        CompletableFuture.runAsync(() -> {
            try {
                ContextUtil.setLocalMap(ctx);
                int affected = deviceManager.fillBoundProductVersionIfBlank(deviceIdentification, version);
                if (affected > 0) {
                    linkCacheDataHelper.deleteDeviceCacheVO(deviceIdentification);
                    log.info("[boundProductVersionNo-backfill] ok device={} version={} affected={}",
                        deviceIdentification, version, affected);
                } else {
                    log.debug("[boundProductVersionNo-backfill] skipped(已有版本或设备不存在) device={} version={}",
                        deviceIdentification, version);
                }
            } catch (Exception ex) {
                log.warn("[boundProductVersionNo-backfill] failed device={} version={} cause={}",
                    deviceIdentification, version, ex.getMessage());
            } finally {
                // common pool 线程复用,用完清租户上下文防泄漏
                ContextUtil.remove();
            }
        });
    }

    private List<ProductPropertyResultVO> buildPropertyResults(ProductServiceParamVO productServiceParamVO, Map<String, Object> dataList) {
        // Check if the provided dataList is null and return an empty list if so
        if (dataList == null) {
            return Collections.emptyList();
        }

        // Now proceed as before, since we're sure dataList is not null
        return productServiceParamVO.getProperties().stream()
            .map(productPropertyParamVO -> {
                String propertyCode = productPropertyParamVO.getPropertyCode();
                // Directly check if dataList contains the property code
                if (dataList.containsKey(propertyCode)) {
                    ProductPropertyResultVO propertyResultVO = BeanPlusUtil.toBeanIgnoreError(productPropertyParamVO, ProductPropertyResultVO.class);
                    propertyResultVO.setPropertyValue(dataList.get(propertyCode));
                    return propertyResultVO;
                } else {
                    // If a property is not present, you might choose to skip it or return a default instance
                    // For example, you can return an instance with null for the property value, or you can simply return null to filter it out later
                    // This example will skip the missing properties
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
