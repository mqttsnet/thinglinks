package com.mqttsnet.thinglinks.link.service.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceLocation;
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
public interface DeviceLocationService {


    int deleteByPrimaryKey(Long id);

    int insert(DeviceLocation record);

    int insertOrUpdate(DeviceLocation record);

    int insertOrUpdateSelective(DeviceLocation record);

    int insertSelective(DeviceLocation record);

    DeviceLocation selectByPrimaryKey(Long id);

    /**
     * 查询设备位置列表
     *
     * @param deviceLocation 设备位置
     * @return 设备位置集合
     */
    public List<DeviceLocation> selectDeviceLocationList(DeviceLocation deviceLocation);

    int updateByPrimaryKeySelective(DeviceLocation record);

    int updateByPrimaryKey(DeviceLocation record);

    int updateBatch(List<DeviceLocation> list);

    int updateBatchSelective(List<DeviceLocation> list);

    int batchInsert(List<DeviceLocation> list);

    /**
     * 批量删除设备位置
     *
     * @param ids 需要删除的设备位置主键集合
     * @return 结果
     */
    int deleteDeviceLocationByIds(Long[] ids);
}


