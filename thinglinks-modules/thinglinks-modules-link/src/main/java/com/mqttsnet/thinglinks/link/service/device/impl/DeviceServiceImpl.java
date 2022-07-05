package com.mqttsnet.thinglinks.link.service.device.impl;

import com.mqttsnet.thinglinks.broker.api.RemotePublishActorService;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.device.DeviceTopicService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setCreateTime(DateUtils.getNowDate());
        return deviceMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        if (StringUtils.isEmpty(String.valueOf(record.getId()))){
            record.setCreateBy(sysUser.getUserName());
            record.setCreateTime(DateUtils.getNowDate());
        }else {
            record.setUpdateTime(DateUtils.getNowDate());
            record.setUpdateBy(sysUser.getUserName());
        }
        return deviceMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        if (StringUtils.isEmpty(String.valueOf(record.getId()))){
            record.setCreateBy(sysUser.getUserName());
            record.setCreateTime(DateUtils.getNowDate());
        }else {
            record.setUpdateTime(DateUtils.getNowDate());
            record.setUpdateBy(sysUser.getUserName());
        }
        return deviceMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setCreateBy(sysUser.getUserName());
        record.setCreateTime(DateUtils.getNowDate());
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
        record.setUpdateTime(DateUtils.getNowDate());
        record.setUpdateBy(sysUser.getUserName());
        return deviceMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Device record) {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        record.setUpdateTime(DateUtils.getNowDate());
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
     * @param device 设备管理
     * @return 结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int insertDevice(Device device)throws Exception {
        Device oneByClientIdAndDeviceIdentification = deviceMapper.findOneByClientIdOrDeviceIdentification(device.getClientId(), device.getDeviceIdentification());
        if(StringUtils.isNotNull(oneByClientIdAndDeviceIdentification)){
            return 0;
        }
        device.setConnectStatus(DeviceConnectStatus.INIT.getValue());
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        device.setCreateBy(sysUser.getUserName());
        device.setCreateTime(DateUtils.getNowDate());
        final int insertDeviceCount = deviceMapper.insertDevice(device);
        //基础TOPIC处理
        Map<String, String> topicMap = new HashMap<>();
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/add","边设备添加子设备");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/addResponse","物联网平台返回的添加子设备的响应");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/delete","边设备删除子设备");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/deleteResponse","物联网平台返回的删除子设备的响应");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/update","边设备更新子设备状态");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/topo/updateResponse","物联网平台返回的更新子设备状态的响应");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/datas","边设备上报数据");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/command","物联网平台给设备或边设备下发命令");
        topicMap.put("/v1/devices/"+device.getDeviceIdentification()+"/commandResponse","边设备返回给物联网平台的命令响应");
        if (insertDeviceCount>0){
            for(Map.Entry<String,String> entry : topicMap.entrySet()) {
                DeviceTopic deviceTopic = new DeviceTopic();
                deviceTopic.setDeviceIdentification(device.getDeviceIdentification());
                deviceTopic.setType("0");
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
     * @param device 设备管理
     * @return 结果
     */
    @Override
    public int updateDevice(Device device)
    {
        LoginUser loginUser = tokenService.getLoginUser();
        SysUser sysUser = loginUser.getSysUser();
        device.setUpdateTime(DateUtils.getNowDate());
        device.setUpdateBy(sysUser.getUserName());
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
            redisService.delete(Constants.DEVICE_RECORD_KEY+clientId);
            //更新数据库设备状态
            Device device = new Device();
            device.setId(oneByClientId.getId());
            device.setConnectStatus(DeviceConnectStatus.OFFLINE.getValue());
            device.setUpdateTime(DateUtils.getNowDate());
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
        return r.getCode() == 200;
    }

	@Override
	public Long countDistinctClientIdByConnectStatus(String connectStatus){
		 return deviceMapper.countDistinctClientIdByConnectStatus(connectStatus);
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
            redisService.setCacheObject(Constants.DEVICE_RECORD_KEY+device.getClientId(),device,60L+ Long.parseLong(DateUtils.getRandom(1)), TimeUnit.SECONDS);
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








}

