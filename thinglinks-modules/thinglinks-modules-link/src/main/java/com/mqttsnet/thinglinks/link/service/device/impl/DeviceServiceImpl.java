package com.mqttsnet.thinglinks.link.service.device.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mqttsnet.thinglinks.broker.api.RemoteMqttBrokerOpenApi;
import com.mqttsnet.thinglinks.common.core.constant.CacheConstants;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.common.core.enums.DeviceTopicEnum;
import com.mqttsnet.thinglinks.common.core.enums.DeviceType;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.core.utils.tdengine.TdUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import com.mqttsnet.thinglinks.link.api.domain.device.enumeration.MqttProtocolTopoStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.device.model.DeviceParams;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.result.TopoQueryDeviceResultVO;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceLocationService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.device.DeviceTopicService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description: 设备管理业务层接口实现类
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
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RedisService redisService;
    @Resource
    private RemoteMqttBrokerOpenApi remoteMqttBrokerOpenApi;
    @Autowired
    private DeviceTopicService deviceTopicService;
    @Autowired
    private ProductService productService;
    @Autowired
    private DeviceLocationService deviceLocationService;
    @Autowired
    private ProductServicesService productServicesService;
    @Resource
    private RemoteTdEngineService remoteTdEngineService;

    @Value("${spring.datasource.dynamic.datasource.master.dbName:thinglinks}")
    private String dataBaseName;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        return deviceMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setUpdateBy(sysUser.getUserName());
        return deviceMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setUpdateBy(sysUser.getUserName());
        return deviceMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        return deviceMapper.insertSelective(record);
    }

    @Override
    public Device selectByPrimaryKey(Long id) {
        return deviceMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setUpdateBy(sysUser.getUserName());
        return deviceMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setUpdateBy(sysUser.getUserName());
        return deviceMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<Device> list) {
        return deviceMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<Device> list) {
        return deviceMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<Device> list) {
        return deviceMapper.batchInsert(list);
    }


    @Override
    public int updateConnectStatusByClientId(String updatedConnectStatus, String clientId) {
        log.info("更新设备连接状态为: {} , clientId: {}", updatedConnectStatus, clientId);
        return deviceMapper.updateConnectStatusByClientId(updatedConnectStatus, clientId);
    }


    @Override
    public Device findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(String clientId, String userName, String password, String deviceStatus, String protocolType) {
        return deviceMapper.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientId, userName, password, deviceStatus, protocolType);
    }

    @Override
    public List<Device> findByAll(Device device) {
        return deviceMapper.findByAll(device);
    }

    @Override
    public Device findOneById(Long id) {
        return deviceMapper.findOneById(id);
    }

    /**
     * 查询设备管理
     *
     * @param id 设备管理主键
     * @return 设备管理
     */
    @Override
    public Device selectDeviceById(Long id) {
        return deviceMapper.selectDeviceById(id);
    }

    /**
     * 查询设备管理列表
     *
     * @param device 设备管理
     * @return 设备管理
     */
    @Override
    public List<Device> selectDeviceList(Device device) {
        return deviceMapper.selectDeviceList(device);
    }

    /**
     * 新增设备管理
     *
     * @param deviceParams 设备管理
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int insertDevice(DeviceParams deviceParams) throws Exception {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Device device = new Device();
        BeanUtils.copyProperties(deviceParams, device);
        device.setConnectStatus(DeviceConnectStatusEnum.INIT.getValue());
        device.setCreateBy(sysUser.getUserName());
        final int insertDeviceCount = deviceMapper.insertOrUpdateSelective(device);
        if (insertDeviceCount > 0) {
            //设备位置信息存储
            DeviceLocation deviceLocation = new DeviceLocation();
            BeanUtils.copyProperties(deviceParams.getDeviceLocation(), deviceLocation);
            deviceLocation.setDeviceIdentification(device.getDeviceIdentification());
            deviceLocationService.insertOrUpdateSelective(deviceLocation);
            //基础TOPIC集合
            Map<String, String> topicMap = new HashMap<>();
            if (DeviceType.GATEWAY.getValue().equals(device.getDeviceType())) {
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/add", "边设备添加子设备");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/addResponse", "物联网平台返回的添加子设备的响应");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/delete", "边设备删除子设备");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/deleteResponse", "物联网平台返回的删除子设备的响应");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/update", "边设备更新子设备状态");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/updateResponse", "物联网平台返回的更新子设备状态的响应");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/datas", "边设备上报数据");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/command", "物联网平台给设备或边设备下发命令");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/commandResponse", "边设备返回给物联网平台的命令响应");

                // 添加OTA更新命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaCommand", "物联网平台给网关设备下发OTA远程升级命令");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaCommandResponse", "网关设备返回给物联网平台的OTA远程升级命令响应");

                // 添加OTA拉取命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaPull", "网关设备拉取物联网平台的最新软固件信息");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaPullResponse", "物联网平台响应软固件信息给设备");

                // 添加OTA上报命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaReport", "网关设备向物联网平台上报软固件版本");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaReportResponse", "物联网平台接收到上报软固件信息响应");

                // 添加OTA读取命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaRead", "物联网平台读取设备软固件版本");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaReadResponse", "网关设备回复物联网平台读取设备固件版本指令");

            } else if (DeviceType.COMMON.getValue().equals(device.getDeviceType())) {
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/datas", "普通设备上报数据");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/command", "物联网平台给普通设备下发命令");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/commandResponse", "普通设备返回给物联网平台的命令响应");
                // 添加OTA更新命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaCommand", "物联网平台给普通设备下发OTA远程升级命令");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaCommandResponse", "普通设备返回给物联网平台的OTA远程升级命令响应");

                // 添加OTA拉取命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaPull", "普通设备拉取物联网平台的最新软固件信息");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaPullResponse", "物联网平台响应软固件信息给普通设备");

                // 添加OTA上报命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaReport", "普通设备向物联网平台上报软固件版本");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaReportResponse", "物联网平台接收到上报软固件信息响应");

                // 添加OTA读取命令和响应
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaRead", "物联网平台读取设备软固件版本");
                topicMap.put("/" + device.getDeviceSdkVersion() + "/devices/" + device.getDeviceIdentification() + "/topo/otaReadResponse", "普通设备回复物联网平台读取设备固件版本指令");

            }
            //设备基础Topic数据存储
            for (Map.Entry<String, String> entry : topicMap.entrySet()) {
                DeviceTopic deviceTopic = new DeviceTopic();
                deviceTopic.setDeviceIdentification(device.getDeviceIdentification());
                deviceTopic.setType(DeviceTopicEnum.BASIS.getKey());
                deviceTopic.setTopic(entry.getKey());
                if (entry.getKey().startsWith("/" + device.getDeviceSdkVersion() + "/devices/") && entry.getKey().endsWith("datas")) {
                    deviceTopic.setPublisher("边设备");
                    deviceTopic.setSubscriber("物联网平台");
                } else if (entry.getKey().startsWith("/" + device.getDeviceSdkVersion() + "/devices/") && entry.getKey().endsWith("commandResponse")) {
                    deviceTopic.setPublisher("边设备");
                    deviceTopic.setSubscriber("物联网平台");
                } else if (entry.getKey().startsWith("/" + device.getDeviceSdkVersion() + "/devices/") && (entry.getKey().endsWith("Response") || entry.getKey().endsWith("command"))) {
                    deviceTopic.setPublisher("物联网平台");
                    deviceTopic.setSubscriber("边设备");
                } else {
                    deviceTopic.setPublisher("边设备");
                    deviceTopic.setSubscriber("物联网平台");
                }
                deviceTopic.setRemark(entry.getValue());
                deviceTopic.setCreateBy(sysUser.getUserName());
                deviceTopicService.insertSelective(deviceTopic);
            }
        }
        return insertDeviceCount;
    }

    /**
     * 修改设备管理
     *
     * @param deviceParams 设备管理
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int updateDevice(DeviceParams deviceParams) throws Exception {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Device device = new Device();
        BeanUtils.copyProperties(deviceParams, device);
        device.setUpdateBy(sysUser.getUserName());
        final int insertDeviceCount = deviceMapper.insertOrUpdateSelective(device);
        if (insertDeviceCount > 0) {
            //设备位置信息存储
            DeviceLocation deviceLocation = new DeviceLocation();
            BeanUtils.copyProperties(deviceParams.getDeviceLocation(), deviceLocation);
            deviceLocationService.insertOrUpdateSelective(deviceLocation);
        }
        return deviceMapper.updateDevice(device);
    }

    /**
     * 批量删除设备管理
     *
     * @param ids 需要删除的设备管理主键
     * @return 结果
     */
    @Override
    public int deleteDeviceByIds(Long[] ids) {
        return deviceMapper.deleteDeviceByIds(ids);
    }

    /**
     * 删除设备管理信息
     *
     * @param id 设备管理主键
     * @return 结果
     */
    @Override
    public int deleteDeviceById(Long id) {
        return deviceMapper.deleteDeviceById(id);
    }

    @Override
    public Device findOneByClientId(String clientId) {
        return deviceMapper.findOneByClientId(clientId);
    }

    @Override
    public Device findOneByClientIdAndDeviceIdentification(String clientId, String deviceIdentification) {
        return deviceMapper.findOneByClientIdAndDeviceIdentification(clientId, deviceIdentification);
    }

    @Override
    public Device findOneByDeviceIdentification(String deviceIdentification) {
        return deviceMapper.findOneByDeviceIdentification(deviceIdentification);
    }

    @Override
    public Device findOneByClientIdOrderByDeviceIdentification(String clientId) {
        return deviceMapper.findOneByClientIdOrderByDeviceIdentification(clientId);
    }

    @Override
    public Device findOneByClientIdOrDeviceIdentification(String clientId, String deviceIdentification) {
        return deviceMapper.findOneByClientIdOrDeviceIdentification(clientId, deviceIdentification);
    }

    /**
     * 设备信息缓存失效
     *
     * @param clientId
     * @return
     */
    @Override
    public Boolean cacheInvalidation(String clientId) {
        Device oneByClientId = deviceMapper.findOneByClientId(clientId);
        //设备信息缓存失效 删除缓存 更新数据库设备状态
        if (StringUtils.isNotNull(oneByClientId)) {
            //删除缓存
            redisService.delete(CacheConstants.DEF_DEVICE + oneByClientId.getDeviceIdentification());
            //更新数据库设备状态
            Device device = new Device();
            device.setId(oneByClientId.getId());
            device.setConnectStatus(DeviceConnectStatusEnum.OFFLINE.getValue());
            deviceMapper.updateByPrimaryKeySelective(device);
        }
        return true;
    }

    /**
     * 批量断开设备连接端口
     *
     * @param ids
     * @return
     */
    @Override
    public Boolean disconnect(Long[] ids) {
        final List<Device> deviceList = deviceMapper.findAllByIdIn(Arrays.asList(ids));
        if (StringUtils.isEmpty(deviceList)) {
            return false;
        }
        final List<String> clientIdentifiers = deviceList.stream().map(Device::getClientId).collect(Collectors.toList());
        final R r = remoteMqttBrokerOpenApi.closeConnection(clientIdentifiers);
        log.info("主动断开设备ID: {} 连接 , Broker 处理结果: {}", clientIdentifiers, r.toString());
        return r.getCode() == ResultEnum.SUCCESS.getCode();
    }

    @Override
    public Long countDistinctClientIdByConnectStatus(String connectStatus) {
        return deviceMapper.countDistinctClientIdByConnectStatus(connectStatus);
    }

    @Override
    public List<String> selectByProductIdentification(String productIdentification) {
        return deviceMapper.selectByProductIdentification(productIdentification);
    }

    /**
     * 客户端身份认证
     *
     * @param clientIdentifier 客户端
     * @param username         用户名
     * @param password         密码
     * @param deviceStatus     设备状态
     * @param protocolType     协议类型
     * @return
     */
    @Override
    public Device clientAuthentication(String clientIdentifier, String username, String password, String deviceStatus, String protocolType) {
        final Device device = this.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientIdentifier, username, password, deviceStatus, protocolType);
        if (Optional.ofNullable(device).isPresent()) {
            //缓存设备信息
            redisService.setCacheObject(CacheConstants.DEF_DEVICE + device.getDeviceIdentification(), device, 300L + Long.parseLong(DateUtils.getRandom(1)), TimeUnit.SECONDS);
            //更改设备在线状态为在线
            this.updateConnectStatusByClientId(DeviceConnectStatusEnum.ONLINE.getValue(), clientIdentifier);
            return device;
        }
        return null;
    }

    @Override
    public List<Device> findAllByIdIn(Collection<Long> idCollection) {
        return deviceMapper.findAllByIdIn(idCollection);
    }

    @Override
    public List<Device> findAllByProductIdentification(String productIdentification) {
        return deviceMapper.findAllByProductIdentification(productIdentification);
    }

    @Override
    public Device selectByProductIdentificationAndDeviceIdentification(String productIdentification, String deviceIdentification) {
        return deviceMapper.selectByProductIdentificationAndDeviceIdentification(productIdentification, deviceIdentification);
    }

    /**
     * 查询设备详细信息
     *
     * @param id
     * @return
     */
    @Override
    public DeviceParams selectDeviceModelById(Long id) {
        DeviceParams deviceParams = new DeviceParams();
        BeanUtils.copyProperties(this.selectDeviceById(id), deviceParams);
        deviceParams.setDeviceLocation(deviceLocationService.findOneByDeviceIdentification(deviceParams.getDeviceIdentification()));
        return deviceParams;
    }

    /**
     * 查询普通设备影子数据
     *
     * @param ids       需要查询的普通设备id
     * @param startTime 开始时间 格式：yyyy-MM-dd HH:mm:ss
     * @param endTime   结束时间 格式：yyyy-MM-dd HH:mm:ss
     * @return 普通设备影子数据
     */
    @Override
    public Map<String, List<Map<String, Object>>> getDeviceShadow(String ids, String startTime, String endTime) {
        List<Long> idCollection = Arrays.stream(ids.split(",")).mapToLong(Long::parseLong).boxed().collect(Collectors.toList());
        List<Device> devices = deviceMapper.findAllByIdInAndStatus(idCollection, "ENABLE");
        if (StringUtils.isNull(devices)) {
            log.error("查询普通设备影子数据失败，普通设备不存在");
            return null;
        }
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        devices.forEach(device -> {
            Product product = productService.selectByProductIdentification(device.getProductIdentification());
            if (StringUtils.isNull(product)) {
                log.error("查询普通设备影子数据失败，设备对应的产品不存在");
                return;
            }
            List<ProductServices> productServicesLis = productServicesService.findAllByProductIdentificationIdAndStatus(product.getProductIdentification(), Constants.ENABLE);
            if (StringUtils.isNull(productServicesLis)) {
                log.error("查询普通设备影子数据失败，普通设备services不存在");
                return;
            }
            productServicesLis.forEach(productServices -> {
                String superTableName = TdUtils.getSuperTableName(product.getProductType(), product.getProductIdentification(), productServices.getServiceCode());
                String shadowTableName = TdUtils.getSubTableName(superTableName, device.getDeviceIdentification());
                SelectDto selectDto = new SelectDto();
                selectDto.setDataBaseName(dataBaseName);
                selectDto.setTableName(shadowTableName);
                if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
                    selectDto.setFieldName("ts");
                    selectDto.setStartTime(DateUtils.localDateTime2Millis(DateUtils.dateToLocalDateTime(DateUtils.strToDate(startTime))));
                    selectDto.setEndTime(DateUtils.localDateTime2Millis(DateUtils.dateToLocalDateTime(DateUtils.strToDate(endTime))));
                    R<?> dataByTimestamp = remoteTdEngineService.getDataByTimestamp(selectDto);
                    if (StringUtils.isNull(dataByTimestamp)) {
                        log.error("查询普通设备影子数据失败，普通设备影子数据不存在");
                    } else {
                        map.put(shadowTableName, (List<Map<String, Object>>) dataByTimestamp.getData());
                        log.info("查询普通设备影子数据成功，普通设备影子数据：{}", dataByTimestamp.getData());

                    }
                } else {
                    R<?> lastData = remoteTdEngineService.getLastData(selectDto);
                    if (StringUtils.isNull(lastData)) {
                        log.error("查询普通设备影子数据失败，普通设备影子数据不存在");
                    } else {
                        map.put(shadowTableName, (List<Map<String, Object>>) lastData.getData());
                        log.info("查询普通设备影子数据成功，普通设备影子数据：{}", lastData.getData());

                    }
                }

            });
        });
        return map;
    }


    public List<Device> selectDeviceByDeviceIdentificationList(List<String> deviceIdentificationList) {
        return deviceMapper.selectDeviceByDeviceIdentificationList(deviceIdentificationList);
    }


    /**
     * MQTT协议下上报设备数据
     *
     * @param topoDeviceDataReportParam 上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @Override
    public TopoDeviceOperationResultVO deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return null;
    }

    /**
     * Http协议下上报设备数据
     *
     * @param topoDeviceDataReportParam 上报参数
     * @return {@link TopoDeviceOperationResultVO} 上报结果
     */
    @Override
    public TopoDeviceOperationResultVO deviceDataReportByHttp(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return null;
    }

    /**
     * Queries device information using the MQTT protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @Override
    public TopoQueryDeviceResultVO queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
        return queryDeviceInfo(topoQueryDeviceParam);
    }

    /**
     * Queries device information using the HTTP protocol.
     *
     * @param topoQueryDeviceParam The device query parameters.
     * @return {@link TopoQueryDeviceResultVO} The result of the device query.
     */
    @Override
    public TopoQueryDeviceResultVO queryDeviceByHttp(TopoQueryDeviceParam topoQueryDeviceParam) {
        return queryDeviceInfo(topoQueryDeviceParam);
    }


    /**
     * Queries device information based on provided parameters.
     *
     * @param topoQueryDeviceParam Parameters for querying device information.
     * @return {@link TopoQueryDeviceResultVO} containing the results of the device query.
     */
    private TopoQueryDeviceResultVO queryDeviceInfo(TopoQueryDeviceParam topoQueryDeviceParam) {
        // Create an instance for the result
        TopoQueryDeviceResultVO topoQueryDeviceResultVO = new TopoQueryDeviceResultVO();

        // Create a list to store the results of device information queries
        List<TopoQueryDeviceResultVO.DataItem> deviceInfoList = Optional.ofNullable(topoQueryDeviceParam.getDeviceIds())
                .orElse(Collections.emptyList())
                .stream()
                .distinct()
                .map(deviceIdentification -> {
                    TopoQueryDeviceResultVO.DataItem dataItem = new TopoQueryDeviceResultVO.DataItem();
                    try {
                        dataItem.setDeviceId(deviceIdentification);
                        // Attempt to find device information based on the identification
                        Optional<Device> optionalDevice = Optional.ofNullable(deviceMapper.findOneByDeviceIdentification(deviceIdentification));
                        TopoQueryDeviceResultVO.DataItem.DeviceInfo deviceInfo = optionalDevice
                                .map(device -> BeanUtil.toBean(device, TopoQueryDeviceResultVO.DataItem.DeviceInfo.class))
                                .orElse(new TopoQueryDeviceResultVO.DataItem.DeviceInfo());

                        // Set device information and status based on query result
                        dataItem.setDeviceInfo(deviceInfo)
                                .setStatusCode(optionalDevice.isPresent() ? MqttProtocolTopoStatusEnum.SUCCESS.getValue() : MqttProtocolTopoStatusEnum.FAILURE.getValue())
                                .setStatusDesc(optionalDevice.isPresent() ? MqttProtocolTopoStatusEnum.SUCCESS.getDesc() : "Device not found");
                    } catch (Exception e) {
                        // Handle any exceptions and set the error information in the data item
                        dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                                .setStatusDesc("Error querying device: " + e.getMessage());
                    }
                    return dataItem;
                })
                .collect(Collectors.toList());

        // Set the list of device information into the result instance
        topoQueryDeviceResultVO.setData(deviceInfoList)
                .setStatusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .setStatusDesc("Query completed");
        return topoQueryDeviceResultVO;
    }

    @Override
    public Long findDeviceTotal() {
        return deviceMapper.findDeviceTotal();
    }

    @Override
    public List<Device> findDevices() {
        return deviceMapper.findDevices();
    }
}

