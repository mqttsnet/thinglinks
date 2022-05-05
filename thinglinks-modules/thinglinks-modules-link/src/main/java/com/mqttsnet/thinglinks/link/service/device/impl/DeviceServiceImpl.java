package com.mqttsnet.thinglinks.link.service.device.impl;

import com.alibaba.fastjson.JSON;
import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.enums.DeviceConnectStatus;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.log.annotation.Log;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductServices;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import com.mqttsnet.thinglinks.system.api.model.LoginUser;
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
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RedisService redisService;

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
    @Transactional(rollbackFor = Exception.class)
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
            device.setConnectStatus(DeviceConnectStatus.INIT.getValue());
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
    public int disconnect(Long[] ids) {
        log.info("主动断开设备连接ID{}",ids);
        //TODO 调用broker api实现mqtt设备断开连接

        return 0;
    }

	@Override
	public Long countDistinctClientIdByConnectStatus(String connectStatus){
		 return deviceMapper.countDistinctClientIdByConnectStatus(connectStatus);
	}




}

