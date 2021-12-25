package com.mqttsnet.thinglinks.link.service.device;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;

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
     * 批量删除设备管理
     *
     * @param ids 需要删除的设备管理主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

}

