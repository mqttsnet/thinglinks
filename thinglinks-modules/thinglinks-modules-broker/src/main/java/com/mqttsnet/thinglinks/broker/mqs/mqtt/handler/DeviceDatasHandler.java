package com.mqttsnet.thinglinks.broker.mqs.mqtt.handler;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.broker.mqs.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.link.api.RemoteDeviceService;
import com.mqttsnet.thinglinks.link.api.domain.cache.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.cache.product.ProductModelCacheVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.link.common.cache.helper.CacheDataHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: 处理DEVICE_DATA主题
 * @packagename: com.mqttsnet.thinglinks.mqtt.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-05 23:00
 **/
@Slf4j
@Service
public class DeviceDatasHandler extends AbstractMessageHandler implements TopicHandler {

    public DeviceDatasHandler(CacheDataHelper cacheDataHelper,
                                  RemoteDeviceService remoteDeviceService,
                                  RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi,
                                  ProtocolMessageAdapter protocolMessageAdapter) {
        super(cacheDataHelper, remoteDeviceService, remoteMqttBrokerOpenApi, protocolMessageAdapter);
    }

    @Autowired
    private TdsApi tdsApi;


    /**
     * @param topic the MQTT topic the message was received on.
     * @param qos   the quality of service level of the message.
     * @param body  the payload of the message.
     */
    @Override
    public void handle(String topic, String qos, String body) {
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }

        // Extract variables from the topic
        Map<String, String> stringStringMap = protocolMessageAdapter.extractVariables(topic);
        String version = stringStringMap.get("version");
        String deviceId = stringStringMap.get("deviceId");

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            return;
        }

        try {
            ProtocolDataMessageDTO protocolDataMessageDTO = protocolMessageAdapter.parseProtocolDataMessage(body);
            // 构造 EncryptionDetails 对象
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .build();
            String dataBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);


            // Parse body
            TopoDeviceDataReportParam deviceDataParam = new TopoDeviceDataReportParam();
            try {
                deviceDataParam = JSON.toJavaObject(JSON.parseObject(dataBody), TopoDeviceDataReportParam.class);
            } catch (Exception e) {
                log.warn("The protocol format is incorrect");
                return;
            }
            String resultDataBody = processingTopicMessage(deviceDataParam);

            // Handle result
            ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);
            String resultData = JSON.toJSONString(handleResult);

            // Determine response topic based on request topic
            String responseTopic = "/dataResponse";
            // Generate response topic string
            String responseTopicStr = generateResponseTopic(version, deviceId, responseTopic);

            // Push message to MQTT to notify device of successful/failed data report
            sendMessage(responseTopicStr, qos, resultData, String.valueOf(deviceCacheVO.getAppId()));
        } catch (Exception e) {
            log.error("Failed to decrypt the message", e);
        }
    }

    /**
     * Process /device/data Topic for device data reporting
     *
     * @param deviceDataParam device data
     * @return Processing result json
     */
    @Override
    protected String processingTopicMessage(Object deviceDataParam) throws Exception {
        log.info("processingDeviceDataTopic Processing result:{}", JSON.toJSONString(deviceDataParam));
        TopoDeviceDataReportParam dataReportParam = BeanPlusUtil.toBeanIgnoreError(deviceDataParam, TopoDeviceDataReportParam.class);
        dataReportParam.getDevices().forEach(device -> {
            log.info("processingDeviceDataTopic Processing result:{}", JSON.toJSONString(device));
            String deviceId = device.getDeviceId();
            DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
            if (deviceCacheVO == null) {
                log.warn("processingDeviceDataTopic Device not found:{}", deviceId);
                return;
            }
            ProductModelCacheVO productModelCacheVO = getProductModelCacheVO(deviceCacheVO.getProductIdentification());
            if (productModelCacheVO == null) {
                log.warn("processingDeviceDataTopic Product Model not found:{}", deviceCacheVO.getProductIdentification());
                return;
            }

            Map<String, Map<String, Object>> dataMap = new HashMap<>();

            device.getServices().forEach(service -> {
                String superTableName = TdsUtils.superTableName(ProductTypeEnum.valueOf(deviceCacheVO.getProductCacheVO().getProductType()).getDesc(),
                        deviceCacheVO.getProductIdentification(),
                        service.getServiceCode());

                String subTableName = TdsUtils.subTableName(superTableName, deviceCacheVO.getDeviceIdentification());

                List<SuperTableDescribeVO> productModelSuperTableCacheVO =
                        getProductModelSuperTableCacheVO(Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
                                service.getServiceCode(), deviceCacheVO.getDeviceIdentification());

                //如果是空，需要做设备的初始化动作，并缓存模型表结构
                if (CollUtil.isEmpty(productModelSuperTableCacheVO)) {
                    R<List<SuperTableDescribeVO>> superTableDescribeVOListR = tdsApi.describeSuperOrSubTable(subTableName);

                    List<SuperTableDescribeVO> existingFields = Optional.ofNullable(superTableDescribeVOListR.getData()).orElse(Collections.emptyList());

                    if (existingFields.isEmpty()) {
                        log.info("设备初始化，设备标识：{}，服务标识：{}", deviceCacheVO.getDeviceIdentification(), service.getServiceCode());
                        TableDTO tableDTO = new TableDTO();
                        tableDTO.setSuperTableName(superTableName);
                        tableDTO.setTableName(subTableName);
                        List<Fields> tagsFieldValues = new ArrayList<>();
                        Fields fields = new Fields();
                        fields.setFieldName(TdsConstants.DEVICE_IDENTIFICATION);
                        fields.setFieldValue(deviceCacheVO.getDeviceIdentification());
                        fields.setDataType(DataTypeEnum.BINARY);
                        tagsFieldValues.add(fields);
                        tableDTO.setTagsFieldValues(tagsFieldValues);
                        R subTable = tdsApi.createSubTable(tableDTO);
                        if (Boolean.TRUE.equals(subTable.getIsSuccess())) {
                            log.info("设备初始化，设备标识：{}，服务标识：{}，初始化成功", deviceCacheVO.getDeviceIdentification(), service.getServiceCode());
                            // 查询新的表结构信息存redis
                            setProductModelSuperTableCacheVO(Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
                                    service.getServiceCode(), deviceCacheVO.getDeviceIdentification(), tdsApi.describeSuperOrSubTable(superTableName).getData());

                        } else {
                            log.warn("设备初始化 ，设备标识：{}，服务标识：{}，初始化失败", deviceCacheVO.getDeviceIdentification(), service.getServiceCode());
                            return;
                        }
                    } else {
                        setProductModelSuperTableCacheVO(Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
                                service.getServiceCode(), deviceCacheVO.getDeviceIdentification(), existingFields);
                    }

                }

                Long eventTime = Optional.ofNullable(service.getEventTime())
                        .map(Object::toString)
                        .flatMap(s -> {
                            try {
                                return Optional.of(Long.parseLong(s));
                            } catch (NumberFormatException e) {
                                return Optional.empty();
                            }
                        })
                        .orElse(System.currentTimeMillis());
                List<Fields> schemaFieldsList = new ArrayList<>();
                List<Fields> tagsFieldsList = new ArrayList<>();

                Map<String, Object> data = StringUtils.jsonToMap(service.getData().toString());
                dataMap.put(service.getServiceCode(), data);

                productModelSuperTableCacheVO.forEach(superTableDescribeVO -> {
                    Fields fields = new Fields();
                    fields.setFieldName(superTableDescribeVO.getField());
                    DataTypeEnum dataTypeEnum = DataTypeEnum.valueOfByDataType(superTableDescribeVO.getType());
                    fields.setDataType(dataTypeEnum);
                    fields.setSize(superTableDescribeVO.getLength());

                    // 根据字段名称获取data里的数据
                    if (CollUtil.isNotEmpty(data) && data.containsKey(superTableDescribeVO.getField())) {
                        fields.setFieldValue(data.get(superTableDescribeVO.getField()));
                    } else if (TdsConstants.EVENT_TIME.equals(superTableDescribeVO.getField())) {
                        // 需要校验下是否为时间戳
                        fields.setFieldValue(eventTime);
                    } else if (TdsConstants.TS.equals(superTableDescribeVO.getField())) {
                        // 需要校验下是否为时间戳
                        fields.setFieldValue(DateUtils.millisecondStampL());
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

                R insertedResult = tdsApi.insertTableData(tableDTO);

                if (Boolean.TRUE.equals(insertedResult.getIsSuccess())) {
                    log.info("insert  table data success, tableName:{}", subTableName);
                } else {
                    log.error("insert  table data failed, tableName:{}", subTableName);
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

            log.info("productResultVO: {}", JSON.toJSONString(productResultVO));
            setDeviceDataCollectionPoolCacheVO(Optional.ofNullable(deviceCacheVO.getProductIdentification()).orElse(""),
                    deviceCacheVO.getDeviceIdentification(), productResultVO);
        });

        return JSON.toJSONString("");
    }

    private List<ProductServiceResultVO> buildServiceResults(Map<String, Map<String, Object>> dataMap, ProductModelCacheVO productModelCacheVO) {
        // Check if the productModelCacheVO is null or if getServices() returns null to avoid NullPointerException.
        if (productModelCacheVO == null || productModelCacheVO.getServices() == null) {
            return Collections.emptyList(); // Return an empty list if there are no services.
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
                .filter(Objects::nonNull) // Filter out any nulls if the property code was not present in dataList
                .collect(Collectors.toList());
    }


}
