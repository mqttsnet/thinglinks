package com.mqttsnet.thinglinks.link.service.device.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mqttsnet.thinglinks.broker.api.RemotePublishActorService;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.ClassInjector;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.DynamicClassLoader;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.DynamicLoaderEngine;
import com.mqttsnet.thinglinks.common.core.dynamicCompilation.bytecode.InjectionSystem;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.enums.DeviceType;
import com.mqttsnet.thinglinks.common.core.enums.ProtocolType;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.SubStringUtil;
import com.mqttsnet.thinglinks.common.core.utils.tdengine.TdUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceDatas;
import com.mqttsnet.thinglinks.link.api.domain.device.model.DeviceInfos;
import com.mqttsnet.thinglinks.link.api.domain.device.model.TopoAddDatas;
import com.mqttsnet.thinglinks.link.api.domain.deviceInfo.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceDatasMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceDatasService;
import com.mqttsnet.thinglinks.link.service.device.DeviceInfoService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 0:27$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 0:27$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
public class DeviceDatasServiceImpl implements DeviceDatasService {

    @Resource
    private DeviceDatasMapper deviceDatasMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductServicesService productServicesService;
    @Autowired
    private RedisService redisService;
    @Resource
    private RemoteTdEngineService remoteTdEngineService;
    @Autowired
    private DeviceInfoService deviceInfoService;
    @Resource
    private RemotePublishActorService remotePublishActorService;
    /**
     * 数据库名称
     */
    @Value("${spring.datasource.dynamic.datasource.master.dbName:thinglinks}")
    private String dataBaseName;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceDatasMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceDatas record) {
        return deviceDatasMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceDatas record) {
        return deviceDatasMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceDatas record) {
        return deviceDatasMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(DeviceDatas record) {
        return deviceDatasMapper.insertSelective(record);
    }

    @Override
    public DeviceDatas selectByPrimaryKey(Long id) {
        return deviceDatasMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceDatas record) {
        return deviceDatasMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceDatas record) {
        return deviceDatasMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceDatas> list) {
        return deviceDatasMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<DeviceDatas> list) {
        return deviceDatasMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<DeviceDatas> list) {
        return deviceDatasMapper.batchInsert(list);
    }

    /**
     * thinglinks-mqtt基础数据处理
     *
     * @param thinglinksMessage
     */
    @Async("linkAsync")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void insertBaseDatas(JSONObject thinglinksMessage) throws Exception {
        String topic = thinglinksMessage.getString("topic");
        String qos = thinglinksMessage.getString("qos");
        String body = thinglinksMessage.getString("body");
        String time = thinglinksMessage.getString("time");
        if (!JSONUtil.isJson(body)) {
            log.error("Topic:{},The entry is empty and ignored", topic);
            return;
        }
        /**
         * Topic	                        Publisher（发布者）  Subscriber（订阅者）	用途
         * /v1/devices/{deviceId}/topo/add	边设备	物联网平台	边设备添加子设备
         * /v1/devices/{deviceId}/topo/addResponse	物联网平台	边设备	物联网平台返回的添加子设备的响应
         * /v1/devices/{deviceId}/topo/delete	边设备	物联网平台	边设备删除子设备
         * /v1/devices/{deviceId}/topo/deleteResponse	物联网平台	边设备	物联网平台返回的删除子设备的响应
         * /v1/devices/{deviceId}/topo/update	边设备	物联网平台	边设备更新子设备状态
         * /v1/devices/{deviceId}/topo/updateResponse	物联网平台	边设备	物联网平台返回的更新子设备状态的响应
         * /v1/devices/{deviceId}/datas	边设备	物联网平台	边设备上报数据
         * /v1/devices/{deviceId}/command	物联网平台	边设备	物联网平台给设备或边设备下发命令
         * /v1/devices/{deviceId}/commandResponse	边设备	物联网平台	边设备返回给物联网平台的命令响应
         */
        //边设备上报数据处理
        if (topic.startsWith("/v1/devices/") && topic.endsWith("/topo/add")) {
            final String deviceIdentification = SubStringUtil.subStr(topic, 12, -9);
            final String payload = this.processingTopoAddTopic(deviceIdentification, body);
            final Map<String, Object> param = new HashMap<>();
            param.put("topic", topic.replace("add", "addResponse"));
            param.put("qos", Integer.valueOf(qos));
            param.put("retain", false);
            param.put("message", payload);
            remotePublishActorService.sendMessage(param);
        } else if (topic.startsWith("/v1/devices/") && topic.endsWith("/topo/delete")) {
            final String deviceIdentification = SubStringUtil.subStr(topic, 12, -12);
            final String payload = this.processingTopoDeleteTopic(deviceIdentification, body);
            final Map<String, Object> param = new HashMap<>();
            param.put("topic", topic.replace("delete", "deleteResponse"));
            param.put("qos", Integer.valueOf(qos));
            param.put("retain", false);
            param.put("message", payload);
            remotePublishActorService.sendMessage(param);
        } else if (topic.startsWith("/v1/devices/") && topic.endsWith("/topo/update")) {
            final String deviceIdentification = SubStringUtil.subStr(topic, 12, -12);
            final String payload = this.processingTopoUpdateTopic(deviceIdentification, body);
            final Map<String, Object> param = new HashMap<>();
            param.put("topic", topic.replace("update", "updateResponse"));
            param.put("qos", Integer.valueOf(qos));
            param.put("retain", false);
            param.put("message", payload);
            remotePublishActorService.sendMessage(param);
        } else if (topic.startsWith("/v1/devices/") && topic.endsWith("/datas")) {
            final String deviceIdentification = SubStringUtil.subStr(topic, 12, -6);
            this.processingDatasTopic(deviceIdentification, body);
        } else if (topic.startsWith("/v1/devices/") && topic.endsWith("/commandResponse")) {
            final String deviceIdentification = SubStringUtil.subStr(topic, 12, -16);
            this.processingTopoCommandResponseTopic(deviceIdentification, body);
        } else {
            //TODO 其他协议自行扩展
            log.info("Other Topic packets are ignored,Topic:{},Body:{},Time:{}", topic, body, time);
        }
    }

    /**
     * 处理/topo/add Topic边设备添加子设备
     *
     * @param deviceIdentification 设备标识
     * @param body                  数据
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String processingTopoAddTopic(String deviceIdentification, String body) throws Exception {
        final TopoAddDatas topoAddDatas = JSON.toJavaObject(JSON.parseObject(body), TopoAddDatas.class);
        Map<Object, Object> responseMaps = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList();
        responseMaps.put("mid", topoAddDatas.getMid());
        responseMaps.put("statusCode", 0);
        responseMaps.put("statusDesc", "successful");
        responseMaps.put("data", dataList);
        final Device device = deviceService.findOneByDeviceIdentification(deviceIdentification);
        if (StringUtils.isNull(device)) {
            log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
            responseMaps.put("statusCode", 1);
            responseMaps.put("statusDesc", "The side device reports data processing, but the device does not exist.");
            return JSON.toJSONString(responseMaps);
        }
        final Product product = productService.findOneByProductIdentificationAndProtocolType(device.getProductIdentification(), device.getProtocolType());
        if (StringUtils.isNull(product)) {
            log.error("The side device reports data processing, but the product does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
            responseMaps.put("statusCode", 1);
            responseMaps.put("statusDesc", "The side device reports data processing, but the product does not exist.");
            return JSON.toJSONString(responseMaps);
        }
        for (DeviceInfos deviceInfos : topoAddDatas.getDeviceInfos()) {
            final DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDid(device.getId());
            deviceInfo.setAppId(device.getAppId());
            deviceInfo.setNodeId(deviceInfos.getNodeId());
            deviceInfo.setNodeName(deviceInfos.getName());
            deviceInfo.setDeviceId(UUID.getUUID());
            deviceInfo.setDescription(deviceInfos.getDescription());
            deviceInfo.setManufacturerId(deviceInfos.getManufacturerId());
            deviceInfo.setModel(deviceInfos.getModel());
            deviceInfo.setConnectStatus(DeviceConnectStatus.INIT.getValue());
            deviceInfo.setShadowEnable(true);
            StringBuilder shadowTableNameBuilder = new StringBuilder();
            // 新增设备管理成功后，创建TD普通表
            List<ProductServices> allByProductIdAndStatus = productServicesService.findAllByProductIdentificationIdAndStatus(product.getProductIdentification(), Constants.ENABLE);
            TableDto tableDto;
            for (ProductServices productServices : allByProductIdAndStatus) {
                tableDto = new TableDto();
                tableDto.setDataBaseName(dataBaseName);
                //超级表命名规则 : 产品类型_产品标识_服务名称
                String superTableName = TdUtils.getSuperTableName(product.getProductType(), product.getProductIdentification(), productServices.getServiceName());
                tableDto.setSuperTableName(superTableName);
                //子表命名规则 : 产品类型_产品标识_服务名称_设备标识（设备唯一标识）
                tableDto.setTableName(TdUtils.getSubTableName(superTableName, deviceInfo.getDeviceId()));
                //Tag的处理
                List<Fields> tagsFieldValues = new ArrayList<>();
                Fields fields = new Fields();
                fields.setFieldValue(device.getDeviceIdentification());
                tagsFieldValues.add(fields);
                tableDto.setTagsFieldValues(tagsFieldValues);
                final R<?> ctResult = remoteTdEngineService.createTable(tableDto);
                if (ctResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                    shadowTableNameBuilder.append(tableDto.getTableName()).append(",");
                    log.info("Create SuperTable Success: " + ctResult.getMsg());
                } else {
                    log.error("Create SuperTable Exception: " + ctResult.getMsg());
                }
            }
            if (shadowTableNameBuilder.length() > 0) {
                deviceInfo.setShadowTableName(shadowTableNameBuilder.substring(0, shadowTableNameBuilder.length() - 1));
            }
            shadowTableNameBuilder.replace(0, shadowTableNameBuilder.length(), "");
            deviceInfo.setCreateBy(device.getCreateBy());
            final int insertSelectiveCount = deviceInfoService.insertSelective(deviceInfo);
            Map responseMap = new HashMap<>();
            if (insertSelectiveCount > 0) {
                responseMap.put("statusCode", 0);
                responseMap.put("statusDesc", "successful");
            } else {
                responseMap.put("statusCode", 1);
                responseMap.put("statusDesc", "abortive");
                log.error("Insert DeviceInfo Exception,DeviceIdentification:{},Body:{}", deviceIdentification, body);
            }
            Map<Object, Object> deviceInfoMap = new HashMap<>();
            deviceInfoMap.put("deviceId", deviceInfo.getDeviceId());
            deviceInfoMap.put("nodeId", deviceInfo.getNodeId());
            deviceInfoMap.put("name", deviceInfo.getNodeName());
            deviceInfoMap.put("description", deviceInfo.getDescription());
            deviceInfoMap.put("manufacturerId", deviceInfo.getManufacturerId());
            deviceInfoMap.put("model", deviceInfo.getModel());
            responseMap.put("deviceInfo", deviceInfoMap);
            dataList.add(responseMap);
        }
        return JSON.toJSONString(responseMaps);
    }

    /**
     * 处理/topo/delete Topic边设备删除子设备
     *
     * @param deviceIdentification 设备标识
     * @param body                  数据
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String processingTopoDeleteTopic(String deviceIdentification, String body) throws Exception {
        Map<Object, Object> responseMaps = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList();
        responseMaps.put("mid", JsonPath.read(body, "$.mid").toString());
        responseMaps.put("statusCode", 0);
        responseMaps.put("statusDesc", "successful");
        responseMaps.put("data", dataList);
        final List<String> deviceIds = JsonPath.read(body, "$.deviceIds[*]");
        if (StringUtils.isNull(deviceIds)) {
            log.error("The side device reports data processing, but the deviceId does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
            responseMaps.put("statusCode", 1);
            responseMaps.put("statusDesc", "The side device reports data processing, but the deviceId does not exist.");
            return JSON.toJSONString(responseMaps);
        }
        for (String deviceId : deviceIds) {
            final int deleteByDeviceIdCount = deviceInfoService.deleteByDeviceId(deviceId);
            Map responseMap = new HashMap<>();
            if (deleteByDeviceIdCount > 0) {
                responseMap.put("statusCode", 0);
                responseMap.put("statusDesc", "successful");
            } else {
                responseMap.put("statusCode", 1);
                responseMap.put("statusDesc", "abortive");
                log.error("Delete DeviceInfo Exception");
            }
            responseMap.put("deviceId", deviceId);
            dataList.add(responseMap);
        }
        return JSON.toJSONString(responseMaps);
    }

    /**
     * 处理/topo/update Topic边设备更新子设备状态
     *
     * @param deviceIdentification 设备标识
     * @param body                  数据
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String processingTopoUpdateTopic(String deviceIdentification, String body) throws Exception {
        JSONObject message = JSON.parseObject(body);
        Map<Object, Object> responseMaps = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList();
        responseMaps.put("mid", JsonPath.read(body, "$.mid").toString());
        responseMaps.put("statusCode", 0);
        responseMaps.put("statusDesc", "successful");
        responseMaps.put("data", dataList);
        JSONArray jsonArray = message.getJSONArray("deviceStatuses");
        if (StringUtils.isNull(jsonArray)) {
            log.error("The side device reports data processing, but the deviceStatus does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
            responseMaps.put("statusCode", 1);
            responseMaps.put("statusDesc", "The side device reports data processing, but the deviceStatus does not exist.");
            return JSON.toJSONString(responseMaps);
        }
        jsonArray.stream().forEach(json -> {
            Map responseMap = new HashMap<>();
            Map deviceStatusMap = (Map) json;
            final String deviceId = deviceStatusMap.get("deviceId").toString();
            final String status = deviceStatusMap.get("status").toString();
            final DeviceInfo deviceInfo = deviceInfoService.findOneByDeviceId(deviceId);
            if (StringUtils.isNull(deviceInfo)) {
                log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
            }
            if ("ONLINE".equals(status)) {
                deviceInfo.setConnectStatus(DeviceConnectStatus.ONLINE.getValue());
            } else if ("OFFLINE".equals(status)) {
                deviceInfo.setConnectStatus(DeviceConnectStatus.OFFLINE.getValue());
            }
            final int updateByPrimaryKeySelectiveCount = deviceInfoService.updateByPrimaryKeySelective(deviceInfo);
            if (updateByPrimaryKeySelectiveCount > 0) {
                responseMap.put("statusCode", 0);
                responseMap.put("statusDesc", "successful");
            } else {
                responseMap.put("statusCode", 1);
                responseMap.put("statusDesc", "abortive");
                log.error("Update DeviceInfo Exception");
            }
            responseMap.put("deviceId", deviceId);
            dataList.add(responseMap);

        });
        return JSON.toJSONString(responseMaps);
    }

    /**
     * 处理datas Topic数据上报
     *
     * @param deviceIdentification 设备标识
     * @param body                  数据
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void processingDatasTopic(String deviceIdentification, String body) throws Exception {
        //协议脚本转换处理
        body = convertToBody(deviceIdentification, body);
        //根据返回的json解析出上报的数据data，所属的服务serviceName，事件发生的时间eventTime
        Map<String, Object> resultMap = StringUtils.jsonToMap(body);
        List<Map<String, Object>> items = (List<Map<String, Object>>) resultMap.get("devices");
        for (Map<String, Object> item : items) {
            final Object deviceId = item.get("deviceId");
            Device device = null;
            if (Boolean.TRUE.equals(redisService.hasKey(Constants.DEVICE_RECORD_KEY + deviceIdentification))) {
                device = redisService.getCacheObject(Constants.DEVICE_RECORD_KEY + deviceIdentification);
            } else {
                device = deviceService.findOneByDeviceIdentification(deviceIdentification);
                if (StringUtils.isNotNull(device)) {
                    redisService.setCacheObject(Constants.DEVICE_RECORD_KEY + deviceIdentification, device);
                } else {
                    log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
                    continue;
                }
            }
            String manufacturerId = "";
            String model = "";
            if (device.getDeviceType().equals(DeviceType.GATEWAY.getValue())) {
                final DeviceInfo oneByDeviceId = deviceInfoService.findOneByDeviceId(deviceId.toString());
                if (StringUtils.isNull(oneByDeviceId)) {
                    log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
                    continue;
                }
                //获取设备的厂商ID和型号
                manufacturerId = oneByDeviceId.getManufacturerId();
                model = oneByDeviceId.getModel();
            } else if (device.getDeviceType().equals(DeviceType.COMMON.getValue())) {
                // TODO 优化取redis产品信息
                Product product = productService.selectByProductIdentification(device.getProductIdentification());
                manufacturerId = product.getManufacturerId();
                model = product.getModel();
            }
            final JSONArray services = JSON.parseArray(item.get("services").toString());
            //如果设备上报的数据为空，不需要存，跳过该循环，进入下个循环
            if (services.isEmpty()) {
                log.error("The side device reports data processing, but the data does not exist,DeviceIdentification:{},Body:{}", deviceIdentification, body);
                continue;
            }
            for (Object service : services) {
                final JSONObject serviceData = JSON.parseObject(service.toString());
                final Object serviceId = serviceData.get("serviceId");
                final Object eventTime = serviceData.get("eventTime");
                Map<String, Object> data = StringUtils.jsonToMap(serviceData.get("data").toString());
                final Product product = productService.findOneByManufacturerIdAndModelAndProtocolTypeAndStatus(manufacturerId, model, ProtocolType.MQTT.getValue(), Constants.ENABLE);
                //超级表命名规则 : 产品类型_产品标识_服务名称
                String superTableName = product.getProductType() + "_" + product.getProductIdentification() + "_" + serviceId.toString();
                //子表命名规则 : 产品类型_产品标识_服务名称_设备标识（设备唯一标识）
                String tableName = superTableName + "_" + deviceId.toString();
                //从redis根据超级表名称取出超级表表结构信息
                final Object cacheObject = redisService.getCacheObject(Constants.TDENGINE_SUPERTABLEFILELDS + superTableName);
                ObjectMapper objectMapper = new ObjectMapper();
                SuperTableDto superTableDto = objectMapper.convertValue(cacheObject, SuperTableDto.class);
                //获取超级表的表结构信息
                final List<Fields> schemaFields = superTableDto.getSchemaFields();
                //如果表结构信息为空，跳过该循环，进入下个循环
                if (schemaFields == null) {
                    continue;
                }
                //取出事件发生时间，并格式化为long类型的毫秒时间戳
                String eventTimeStr = eventTime.toString().replace("T", "").replace("Z", "");
                long eventDataTime = DateUtils.string2MillisWithJdk8(eventTimeStr, "yyyyMMddHHmmss");
                //超级表第一个字段数据类型必须为时间戳,默认Ts为当前系统时间
                schemaFields.get(0).setFieldValue(DateUtils.millisecondStampL());
                //因为超级表的第二个字段为事件发生时间数据类型必须为时间戳，所以直接将索引为1的字段信息对象的字段值设置为eventTime
                schemaFields.get(1).setFieldValue(eventDataTime);
                //循环设置字段值
                for (Fields schemaField : schemaFields) {
                    //根据字段名称获取data里的数据（字段名称为服务的属性，数据上报的格式就是根据属性来的）
                    Object fieldValue = data.get(schemaField.getFieldName());
                    //获取的值不为空才给该字段赋值
                    if (fieldValue != null) {
                        //如果字段为字符串类型，字段值的大小不超过该列宽，才给该字段赋值（如果超过列宽，sql会报错）
                        if (schemaField.getSize() != null && "nchar".equals(schemaField.getDataType().getDataType())) {
                            if (fieldValue.toString().length() <= schemaField.getSize()) {
                                schemaField.setFieldValue(fieldValue);
                            }
                        } else {
                            //其他数据类型，直接赋值
                            schemaField.setFieldValue(fieldValue);
                        }
                    }
                }
                //获取超级表的标签信息
                final List<Fields> tagsFields = superTableDto.getTagsFields();
                //循环设置字段值
                for (Fields tagsField : tagsFields) {
                    //根据业务逻辑，将超级表的标签字段定为
                    // 1:设备标识：deviceIdentification
                    Object fieldValue = deviceId;
                    //获取的值不为空才给该字段赋值
                    if (fieldValue != null) {
                        //如果字段为字符串类型，字段值的大小不超过该列宽，才给该字段赋值（如果超过列宽，sql会报错）
                        if (tagsField.getSize() != null && "nchar".equals(tagsField.getDataType().getDataType())) {
                            if (fieldValue.toString().length() <= tagsField.getSize()) {
                                tagsField.setFieldValue(fieldValue);
                            }
                        } else {
                            //其他数据类型，直接赋值
                            tagsField.setFieldValue(fieldValue);
                        }
                    }
                }
                //字段信息对象集合通过stream流过滤掉没有字段值的字段对象
                List<Fields> schemaFieldsStream = schemaFields.stream().filter(fields -> fields.getFieldValue() != null).collect(Collectors.toList());
                //字段信息对象集合通过stream流过滤掉没有字段值的字段对象
                List<Fields> tagsFieldsStream = tagsFields.stream().filter(fields -> fields.getFieldValue() != null).collect(Collectors.toList());
                //如果字段值只有第一个字段的时间戳，说明上报的数据没有符合该服务的属性，不做保存操作，跳过该循环，进入下个循环
                if (schemaFieldsStream.size() == 1) {
                    continue;
                }
                //设置插入所需参数
                TableDto tableDto = new TableDto();
                tableDto.setDataBaseName(superTableDto.getDataBaseName());
                tableDto.setSuperTableName(superTableDto.getSuperTableName());
                tableDto.setTableName(tableName);
                tableDto.setSchemaFieldValues(schemaFieldsStream);
                tableDto.setTagsFieldValues(tagsFieldsStream);
                //调用插入方法插入数据 TODO 需要改为mq异步处理
                final R<?> insertResult = this.remoteTdEngineService.insertData(tableDto);
                if (insertResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                    log.info("DeviceIdentification: {}, Insert data result: {}", deviceIdentification, ResultEnum.SUCCESS.getMessage());
                } else {
                    log.error("DeviceIdentification: {}, Insert data Exception: {}", deviceIdentification, ResultEnum.FAIL.getMessage());
                }
            }
        }
    }

    /**
     * 处理/commandResponse Topic边设备返回给物联网平台的命令响应
     *
     * @param deviceIdentification 设备标识
     * @param body                  数据
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void processingTopoCommandResponseTopic(String deviceIdentification, String body) throws Exception {

    }

    /**
     * 协议转换处理
     * 根据设备找到所属产品 产品的服务及属性 转换出系统能识别的json 找到这个产品的协议内容即Java代码
     */
    public String convertToBody(String deviceIdentification, String body) {
        if (Boolean.TRUE.equals(redisService.hasKey(Constants.DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT + ProtocolType.MQTT.getValue() + deviceIdentification))) {
            String protocolContent = redisService.get(Constants.DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT + ProtocolType.MQTT.getValue() + deviceIdentification);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            PrintWriter out = new PrintWriter(buffer, true);
            byte[] classBytes = DynamicLoaderEngine.compile(protocolContent, out, null);//传入要执行的代码
            byte[] injectedClass = ClassInjector.injectSystem(classBytes);
            InjectionSystem.inject(null, new PrintStream(buffer, true), null);
            DynamicClassLoader classLoader = new DynamicClassLoader(this.getClass().getClassLoader());
            DynamicLoaderEngine.executeMain(classLoader, injectedClass, out, body);
            body = buffer.toString().trim();
            return body;
        } else {
            return body;
        }
    }
}




