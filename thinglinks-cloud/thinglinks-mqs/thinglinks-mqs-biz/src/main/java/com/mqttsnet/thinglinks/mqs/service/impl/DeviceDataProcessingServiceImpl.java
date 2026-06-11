package com.mqttsnet.thinglinks.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
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
import com.mqttsnet.thinglinks.cache.vo.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productproperty.vo.result.ProductPropertyResultVO;
import com.mqttsnet.thinglinks.productservice.vo.param.ProductServiceParamVO;
import com.mqttsnet.thinglinks.productservice.vo.result.ProductServiceResultVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.service.DeviceDataProcessingService;
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
            ProductModelCacheVO productModelCacheVO = linkCacheDataHelper.getProductModelCacheVO(deviceCacheVO.getProductIdentification());
            if (Objects.isNull(productModelCacheVO)) {
                log.warn("processingDeviceDataTopic Product Model not found...productIdentification:{}", deviceCacheVO.getProductIdentification());
                return;
            }

            Map<String, Map<String, Object>> dataMap = new HashMap<>();

            device.getServices().forEach(service -> {
                Integer productType = Optional.ofNullable(deviceCacheVO.getProductCacheVO())
                        .map(p -> p.getProductType())
                        .orElse(null);
                if (productType == null) {
                    log.warn("processingDeviceDataTopic ProductCacheVO or productType is null, deviceId:{}", deviceId);
                    return;
                }
                String superTableName = TdsUtils.superTableName(ProductTypeEnum.valueOf(productType).getDesc(),
                        deviceCacheVO.getProductIdentification(),
                        service.getServiceCode());

                String subTableName = TdsUtils.subTableName(superTableName, deviceCacheVO.getDeviceIdentification());

                List<SuperTableDescribeVO> productModelSuperTableCacheVO =
                        linkCacheDataHelper.getProductModelSuperTableCacheVO(Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
                                service.getServiceCode(), deviceCacheVO.getDeviceIdentification());

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
                        productModelSuperTableCacheVO = Optional.ofNullable(
                                tdsApi.describeSuperOrSubTable(superTableName).getData()
                        ).orElse(Collections.emptyList());
                    }
                    linkCacheDataHelper.setProductModelSuperTableCacheVO(
                            Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
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

                    // 根据字段名称获取data里的数据
                    if (CollUtil.isNotEmpty(data) && data.containsKey(superTableDescribeVO.getField())) {
                        fields.setFieldValue(data.get(superTableDescribeVO.getField()));
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

                log.info("insertTableData param:{}", JSONUtil.toJsonStr(tableDTO));
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
