package com.mqttsnet.thinglinks.link.service.device.impl;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.JsonPath;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.text.UUID;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.SubStringUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceDatas;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceInfo;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.model.DeviceInfos;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.model.TopoAddDatas;
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
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
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
    public int insertOrUpdateWithBLOBs(DeviceDatas record) {
        return deviceDatasMapper.insertOrUpdateWithBLOBs(record);
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
    public int updateByPrimaryKeyWithBLOBs(DeviceDatas record) {
        return deviceDatasMapper.updateByPrimaryKeyWithBLOBs(record);
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertBaseDatas(JSONObject thinglinksMessage) throws Exception {
        String topic = thinglinksMessage.getString("topic");
        String msg = thinglinksMessage.getString("msg");
        if (Objects.equals(msg, "{}")) {
            log.error("Topic:{},The entry is empty and ignored", topic);
            return;
        }
        /**
         * Topic	                        Publisher（发布者）  Subscriber（订阅者）	用途
         * /v1/devices/{gatewayId}/topo/add	边设备	物联网平台	边设备添加子设备
         * /v1/devices/{gatewayId}/topo/addResponse	物联网平台	边设备	物联网平台返回的添加子设备的响应
         * /v1/devices/{gatewayId}/topo/delete	边设备	物联网平台	边设备删除子设备
         * /v1/devices/{gatewayId}/topo/deleteResponse	物联网平台	边设备	物联网平台返回的删除子设备的响应
         * /v1/devices/{gatewayId}/topo/update	边设备	物联网平台	边设备更新子设备状态
         * /v1/devices/{gatewayId}/topo/updateResponse	物联网平台	边设备	物联网平台返回的更新子设备状态的响应
         * /v1/devices/{deviceId}/datas	边设备	物联网平台	边设备上报数据
         * /v1/devices/{deviceId}/command	物联网平台	边设备	物联网平台给设备或边设备下发命令
         * /v1/devices/{deviceId}/commandResponse	边设备	物联网平台	边设备返回给物联网平台的命令响应
         */
        //边设备上报数据处理
        if (topic.startsWith("/v1/devices/") && topic.endsWith("/topo/add")) {
            log.info("Side equipment report data processing,Topic:{},Msg:{}", topic, msg);
            final String deviceIdentification = SubStringUtil.subStr(topic,12,-9);
            this.processingTopoAddTopic(deviceIdentification,msg);
        }else if(topic.startsWith("/v1/devices/") && topic.endsWith("/topo/delete")){
            log.info("Side equipment report data processing,Topic:{},Msg:{}", topic, msg);
            final String deviceIdentification = SubStringUtil.subStr(topic,12,-12);
            this.processingTopoDeleteTopic(deviceIdentification,msg);
        }else if(topic.startsWith("/v1/devices/") && topic.endsWith("/topo/update")){
            log.info("Side equipment report data processing,Topic:{},Msg:{}", topic, msg);
            final String deviceIdentification = SubStringUtil.subStr(topic,12,-12);
            this.processingTopoUpdateTopic(deviceIdentification,msg);
        }else if(topic.startsWith("/v1/devices/") && topic.endsWith("/datas")){
            log.info("Side equipment report data processing,Topic:{},Msg:{}", topic, msg);
            final String deviceIdentification = SubStringUtil.subStr(topic,12,-6);
            this.processingDatasTopic(deviceIdentification,msg);
        }else if(topic.startsWith("/v1/devices/") && topic.endsWith("/commandResponse")){
            log.info("Side equipment report data processing,Topic:{},Msg:{}", topic, msg);
            final String deviceIdentification = SubStringUtil.subStr(topic,12,-16);
            this.processingTopoCommandResponseTopic(deviceIdentification,msg);
        }else {
            //TODO 其他协议自行扩展
            log.info("Other Topic packets are ignored,Topic:{},Msg:{}", topic, msg);
        }
    }

    /**
     * 处理/topo/add Topic边设备添加子设备
     *
     * @param deviceIdentification 设备标识
     * @param msg                  数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processingTopoAddTopic(String deviceIdentification, String msg) throws Exception{
        final Device device = deviceService.findOneByDeviceIdentification(deviceIdentification);
        if (StringUtils.isNull(device)) {
            log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            return;
        }
        final Product product = productService.findOneByProductIdentificationAndProtocolType(device.getProductIdentification(), device.getProtocolType());
        if (StringUtils.isNull(product)) {
            log.error("The side device reports data processing, but the product does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            return;
        }
        final TopoAddDatas topoAddDatas = JSONObject.toJavaObject(JSONObject.parseObject(msg), TopoAddDatas.class);
        for (DeviceInfos deviceInfos: topoAddDatas.getDeviceInfos()) {
            final DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setDId(device.getId());
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
            List<ProductServices> allByProductIdAndStatus = productServicesService.findAllByProductIdAndStatus(product.getId(), "0");
            TableDto tableDto;
            for (ProductServices productServices : allByProductIdAndStatus) {
                tableDto = new TableDto();
                tableDto.setDataBaseName(dataBaseName);
                //超级表命名规则 : 产品类型_产品标识_服务名称
                String superTableName = product.getProductType()+"_"+product.getProductIdentification()+"_"+productServices.getServiceName();
                tableDto.setSuperTableName(superTableName);
                //子表命名规则 : 产品类型_产品标识_服务名称_设备标识（设备唯一标识）
                tableDto.setTableName(superTableName+"_"+deviceInfo.getDeviceId());
                //Tag的处理
                List<Fields> tagsFieldValues = new ArrayList<>();
                Fields fields = new Fields();
                fields.setFieldValue(device.getDeviceIdentification());
                tagsFieldValues.add(fields);
                tableDto.setTagsFieldValues(tagsFieldValues);
                final R<?> ctResult = remoteTdEngineService.createTable(tableDto);
                if (ctResult.getCode() == 200) {
                    shadowTableNameBuilder.append(tableDto.getTableName()).append(",");
                    log.info("Create SuperTable Success: " + ctResult.getMsg());
                }else {
                    log.error("Create SuperTable Exception: " + ctResult.getMsg());
                }
            }
            if (shadowTableNameBuilder.length() > 0) {
                deviceInfo.setShadowTableName(shadowTableNameBuilder.substring(0, shadowTableNameBuilder.length() - 1));
            }
            shadowTableNameBuilder.replace(0, shadowTableNameBuilder.length(), "");
            deviceInfo.setCreateBy(device.getCreateBy());
            deviceInfoService.insertSelective(deviceInfo);
        }
    }

    /**
     * 处理/topo/delete Topic边设备删除子设备
     *
     * @param deviceIdentification 设备标识
     * @param msg                  数据
     */
    @Override
    public void processingTopoDeleteTopic(String deviceIdentification, String msg) throws Exception {

    }

    /**
     * 处理/topo/update Topic边设备更新子设备状态
     *
     * @param deviceIdentification 设备标识
     * @param msg                  数据
     */
    @Override
    public void processingTopoUpdateTopic(String deviceIdentification, String msg) throws Exception {

    }

    /**
     * 处理datas Topic数据上报
     *
     * @param deviceIdentification 设备标识
     * @param msg                  数据
     */
    @Override
    public void processingDatasTopic(String deviceIdentification, String msg) throws Exception{
        final Device device = deviceService.findOneByDeviceIdentification(deviceIdentification);
        if (StringUtils.isNull(device)) {
            log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            return;
        }
        final Product product = productService.findOneByProductIdentificationAndProtocolType(device.getProductIdentification(), device.getProtocolType());
        if (StringUtils.isNull(product)) {
            log.error("The side device reports data processing, but the product does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            return;
        }
        List<String> serviceId = JsonPath.read(msg, "$.devices[*]..serviceId");
        if (StringUtils.isEmpty(serviceId)) {
            log.error("The side device reports data processing, but the serviceId does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
        }
        for (String serviceName : serviceId) {
            final List<ProductServices> allByProductIdAndServiceNameAndStatus = productServicesService.findAllByProductIdAndServiceNameAndStatus(product.getId(), serviceName, "0");
            if (StringUtils.isEmpty(allByProductIdAndServiceNameAndStatus)) {
                log.error("The side device reports data processing, but the service does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            }

        }
    }

    /**
     * 处理/commandResponse Topic边设备返回给物联网平台的命令响应
     *
     * @param deviceIdentification 设备标识
     * @param msg                  数据
     */
    @Override
    public void processingTopoCommandResponseTopic(String deviceIdentification, String msg) throws Exception {

    }

}


