package com.mqttsnet.thinglinks.link.service.device.impl;

import com.mqttsnet.thinglinks.broker.api.RemotePublishActorService;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.enums.DeviceTopicEnum;
import com.mqttsnet.thinglinks.common.core.enums.DeviceType;
import com.mqttsnet.thinglinks.common.core.enums.ResultEnum;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import com.mqttsnet.thinglinks.link.api.domain.device.model.DeviceParams;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.common.core.utils.tdengine.TdUtils;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceLocationService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.device.DeviceTopicService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
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
    private RemotePublishActorService remotePublishActorService;
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
	public int updateConnectStatusByClientId(String updatedConnectStatus,String clientId){
		 return deviceMapper.updateConnectStatusByClientId(updatedConnectStatus,clientId);
	}

	@Override
	public Device findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(String clientId,String userName,String password,String deviceStatus,String protocolType){
		 return deviceMapper.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientId,userName,password,deviceStatus,protocolType);
	}

	@Override
	public List<Device> findByAll(Device device){
		 return deviceMapper.findByAll(device);
	}

	@Override
	public Device findOneById(Long id){
		 return deviceMapper.findOneById(id);
	}

    /**
     * 查询设备管理
     *
     * @param id 设备管理主键
     * @return 设备管理
     */
    @Override
    public Device selectDeviceById(Long id)
    {
        return deviceMapper.selectDeviceById(id);
    }

    /**
     * 查询设备管理列表
     *
     * @param device 设备管理
     * @return 设备管理
     */
    @Override
    public List<Device> selectDeviceList(Device device)
    {
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
    public int insertDevice(DeviceParams deviceParams)throws Exception {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        Device device = new Device();
        BeanUtils.copyProperties(deviceParams,device);
        Device oneByClientIdAndDeviceIdentification = deviceMapper.findOneByClientIdOrDeviceIdentification(device.getClientId(), device.getDeviceIdentification());
        if(StringUtils.isNotNull(oneByClientIdAndDeviceIdentification)){
            throw new Exception("设备编号或者设备标识已存在");
        }
        device.setConnectStatus(DeviceConnectStatus.INIT.getValue());
        device.setCreateBy(sysUser.getUserName());
        final int insertDeviceCount = deviceMapper.insertOrUpdateSelective(device);
        if (insertDeviceCount>0){
            //设备位置信息存储
            DeviceLocation deviceLocation = new DeviceLocation();
            BeanUtils.copyProperties(deviceParams.getDeviceLocation(),deviceLocation);
            deviceLocationService.insertOrUpdateSelective(deviceLocation);
            //基础TOPIC集合
            Map<String, String> topicMap = new HashMap<>();
            if (device.getDeviceType().equals(DeviceType.GATEWAY.getValue())){
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/add","边设备添加子设备");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/addResponse","物联网平台返回的添加子设备的响应");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/delete","边设备删除子设备");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/deleteResponse","物联网平台返回的删除子设备的响应");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/update","边设备更新子设备状态");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/updateResponse","物联网平台返回的更新子设备状态的响应");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/datas","边设备上报数据");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/command","物联网平台给设备或边设备下发命令");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/commandResponse","边设备返回给物联网平台的命令响应");
            }else if (device.getDeviceType().equals(DeviceType.COMMON.getValue())){
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/datas","边设备上报数据");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/command","物联网平台给设备或边设备下发命令");
                topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/commandResponse","边设备返回给物联网平台的命令响应");
                Boolean commonDeviceTDSubtable = this.createCommonDeviceTDSubtable(device);
                if (!commonDeviceTDSubtable) {
                    throw new Exception("创建普通设备TD子表失败");
                }
            }
            //设备基础Topic数据存储
            for(Map.Entry<String,String> entry : topicMap.entrySet()) {
                DeviceTopic deviceTopic = new DeviceTopic();
                deviceTopic.setDeviceIdentification(device.getDeviceIdentification());
                deviceTopic.setType(DeviceTopicEnum.BASIS.getKey());
                deviceTopic.setTopic(entry.getKey());
                if (entry.getKey().startsWith("/v1/devices/") && entry.getKey().endsWith("datas")){
                    deviceTopic.setPublisher("边设备");
                    deviceTopic.setSubscriber("物联网平台");
                }else if (entry.getKey().startsWith("/v1/devices/") && entry.getKey().endsWith("commandResponse")) {
                    deviceTopic.setPublisher("边设备");
                    deviceTopic.setSubscriber("物联网平台");
                }else if (entry.getKey().startsWith("/v1/devices/") && entry.getKey().endsWith("Response")) {
                    deviceTopic.setPublisher("物联网平台");
                    deviceTopic.setSubscriber("边设备");
                }else {
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
        BeanUtils.copyProperties(deviceParams,device);
        device.setUpdateBy(sysUser.getUserName());
        final int insertDeviceCount = deviceMapper.insertOrUpdateSelective(device);
        if (insertDeviceCount>0){
            //设备位置信息存储
            DeviceLocation deviceLocation = new DeviceLocation();
            BeanUtils.copyProperties(deviceParams.getDeviceLocation(),deviceLocation);
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
    public int deleteDeviceByIds(Long[] ids)
    {
        return deviceMapper.deleteDeviceByIds(ids);
    }

    /**
     * 删除设备管理信息
     *
     * @param id 设备管理主键
     * @return 结果
     */
    @Override
    public int deleteDeviceById(Long id)
    {
        return deviceMapper.deleteDeviceById(id);
    }

	@Override
	public Device findOneByClientId(String clientId){
		 return deviceMapper.findOneByClientId(clientId);
	}

	@Override
	public Device findOneByClientIdAndDeviceIdentification(String clientId,String deviceIdentification){
		 return deviceMapper.findOneByClientIdAndDeviceIdentification(clientId,deviceIdentification);
	}

	@Override
	public Device findOneByDeviceIdentification(String deviceIdentification){
		 return deviceMapper.findOneByDeviceIdentification(deviceIdentification);
	}

	@Override
	public Device findOneByClientIdOrderByDeviceIdentification(String clientId){
		 return deviceMapper.findOneByClientIdOrderByDeviceIdentification(clientId);
	}

	@Override
	public Device findOneByClientIdOrDeviceIdentification(String clientId,String deviceIdentification){
		 return deviceMapper.findOneByClientIdOrDeviceIdentification(clientId,deviceIdentification);
	}

    /**
     * 设备信息缓存失效
     * @param clientId
     * @return
     */
    @Override
    public Boolean cacheInvalidation(String clientId) {
        Device oneByClientId = deviceMapper.findOneByClientId(clientId);
        //设备信息缓存失效 删除缓存 更新数据库设备状态
        if(StringUtils.isNotNull(oneByClientId)){
            //删除缓存
            redisService.delete(Constants.DEVICE_RECORD_KEY+oneByClientId.getDeviceIdentification());
            //更新数据库设备状态
            Device device = new Device();
            device.setId(oneByClientId.getId());
            device.setConnectStatus(DeviceConnectStatus.OFFLINE.getValue());
            deviceMapper.updateByPrimaryKeySelective(device);
        }
        log.info(oneByClientId.toString());
        return null;
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
        if (StringUtils.isEmpty(deviceList)){
            return false;
        }
        final List<String> clientIdentifiers = deviceList.stream().map(Device::getClientId).collect(Collectors.toList());
        final R r = remotePublishActorService.closeConnection(clientIdentifiers);
        log.info("主动断开设备ID: {} 连接 , Broker 处理结果: {}",clientIdentifiers,r.toString());
        return r.getCode() == ResultEnum.SUCCESS.getCode();
    }

	@Override
	public Long countDistinctClientIdByConnectStatus(String connectStatus){
		 return deviceMapper.countDistinctClientIdByConnectStatus(connectStatus);
	}

    @Override
    public List<String> selectByProductIdentification(String productIdentification) {
        return deviceMapper.selectByProductIdentification(productIdentification);
    }

    /**
     * 客户端身份认证
     * @param clientIdentifier 客户端
     * @param username 用户名
     * @param password 密码
     * @param deviceStatus 设备状态
     * @param protocolType 协议类型
     * @return
     */
    @Override
    public Boolean clientAuthentication(String clientIdentifier, String username, String password, String deviceStatus, String protocolType) {
        final Device device = this.findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(clientIdentifier, username, password, deviceStatus, protocolType);
        if (Optional.ofNullable(device).isPresent()) {
            //缓存设备信息
            redisService.setCacheObject(Constants.DEVICE_RECORD_KEY+device.getDeviceIdentification(),device,60L+ Long.parseLong(DateUtils.getRandom(1)), TimeUnit.SECONDS);
            //更改设备在线状态为在线
            this.updateConnectStatusByClientId(DeviceConnectStatus.ONLINE.getValue(),clientIdentifier);
            return true;
        }
        return false;
    }

	@Override
	public List<Device> findAllByIdIn(Collection<Long> idCollection){
		 return deviceMapper.findAllByIdIn(idCollection);
	}

	@Override
	public List<Device> findAllByProductIdentification(String productIdentification){
		 return deviceMapper.findAllByProductIdentification(productIdentification);
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
     * @param ids 需要查询的普通设备id
     * @param startTime 开始时间 格式：yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间 格式：yyyy-MM-dd HH:mm:ss
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
            List<ProductServices> productServicesLis  = productServicesService.findAllByProductIdentificationIdAndStatus(product.getProductIdentification(),Constants.ENABLE);
            if (StringUtils.isNull(productServicesLis)) {
                log.error("查询普通设备影子数据失败，普通设备services不存在");
                return;
            }
            productServicesLis.forEach(productServices -> {
                String superTableName = TdUtils.getSuperTableName(product.getProductType(),product.getProductIdentification(),productServices.getServiceName());
                String shadowTableName = TdUtils.getSubTableName(superTableName,device.getClientId());
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
                    }else {
                        map.put(shadowTableName, (List<Map<String, Object>>) dataByTimestamp.getData());
                        log.info("查询普通设备影子数据成功，普通设备影子数据：{}", dataByTimestamp.getData());

                    }
                }else{
                    R<?> lastData = remoteTdEngineService.getLastData(selectDto);
                    if (StringUtils.isNull(lastData)) {
                        log.error("查询普通设备影子数据失败，普通设备影子数据不存在");
                    }else {
                        map.put(shadowTableName, (List<Map<String, Object>>) lastData.getData());
                        log.info("查询普通设备影子数据成功，普通设备影子数据：{}", lastData.getData());

                    }
                }

            });
        });
        return map;
    }

    /**
     * 创建普通设备TD子表
     * @param device
     * @return
     */
    public Boolean createCommonDeviceTDSubtable(Device device){
        final Product product = productService.findOneByProductIdentificationAndProtocolType(device.getProductIdentification(), device.getProtocolType());
        if (StringUtils.isNull(product)) {
            log.error("刷新子设备数据模型失败，子设备产品不存在");
            return false;
        }
        List<ProductServices> allByProductIdAndStatus = productServicesService.findAllByProductIdentificationIdAndStatus(product.getProductIdentification(), Constants.ENABLE);
        TableDto tableDto;
        for (ProductServices productServices : allByProductIdAndStatus) {
            tableDto = new TableDto();
            tableDto.setDataBaseName(dataBaseName);
            //超级表命名规则 : 产品类型_产品标识_服务名称
            String superTableName = TdUtils.getSuperTableName(product.getProductType(),product.getProductIdentification(),productServices.getServiceName());
            tableDto.setSuperTableName(superTableName);
            //子表命名规则 : 产品类型_产品标识_服务名称_设备标识（设备唯一标识）
            tableDto.setTableName(TdUtils.getSubTableName(superTableName,device.getDeviceIdentification()));
            //Tag的处理
            List<Fields> tagsFieldValues = new ArrayList<>();
            Fields fields = new Fields();
            fields.setFieldValue(device.getDeviceIdentification());
            tagsFieldValues.add(fields);
            tableDto.setTagsFieldValues(tagsFieldValues);
            final R<?> ctResult = remoteTdEngineService.createTable(tableDto);
            if (ctResult.getCode() == ResultEnum.SUCCESS.getCode()) {
                log.info("Create SuperTable Success: " + ctResult.getMsg());
            } else {
                log.error("Create SuperTable Exception: " + ctResult.getMsg());
            }
        }
        return true;
    }

}

