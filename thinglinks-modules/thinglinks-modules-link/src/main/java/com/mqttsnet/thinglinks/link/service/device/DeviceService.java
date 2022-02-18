package com.mqttsnet.thinglinks.link.service.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;

import java.util.List;

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
public interface DeviceService {


    int deleteByPrimaryKey(Long id);

    int insert(Device record);

    int insertOrUpdate(Device record);

    int insertOrUpdateSelective(Device record);

    int insertSelective(Device record);

    Device selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(Device record);

    int updateByPrimaryKey(Device record);

    int updateBatch(List<Device> list);

    int updateBatchSelective(List<Device> list);

    int batchInsert(List<Device> list);

	int updateConnectStatusByClientId(String updatedConnectStatus,String clientId);

	Device findOneByClientIdAndUserNameAndPasswordAndDeviceStatusAndProtocolType(String clientId,String userName,String password,String deviceStatus,String protocolType);

	List<Device> findByAll(Device device);

	Device findOneById(Long id);


    /**
     * 查询设备管理
     *
     * @param id 设备管理主键
     * @return 设备管理
     */
    public Device selectDeviceById(Long id);

    /**
     * 查询设备管理列表
     *
     * @param device 设备管理
     * @return 设备管理集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 新增设备管理
     *
     * @param device 设备管理
     * @return 结果
     */
    public int insertDevice(Device device);

    /**
     * 修改设备管理
     *
     * @param device 设备管理
     * @return 结果
     */
    public int updateDevice(Device device);

    /**
     * 批量删除设备管理
     *
     * @param ids 需要删除的设备管理主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

    /**
     * 删除设备管理信息
     *
     * @param id 设备管理主键
     * @return 结果
     */
    public int deleteDeviceById(Long id);



	Device findOneByClientId(String clientId);



	Device findOneByClientIdAndDeviceIdentification(String clientId,String deviceIdentification);



	Device findOneByDeviceIdentification(String deviceIdentification);



	Device findOneByClientIdOrderByDeviceIdentification(String clientId);



	Device findOneByClientIdOrDeviceIdentification(String clientId,String deviceIdentification);






}

