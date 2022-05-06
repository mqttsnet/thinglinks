package com.mqttsnet.thinglinks.link.service.device;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceInfo;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/4/25$ 12:44$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/4/25$ 12:44$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
public interface DeviceInfoService {


    int deleteByPrimaryKey(Long id);

    int insert(DeviceInfo record);

    int insertOrUpdate(DeviceInfo record);

    int insertOrUpdateSelective(DeviceInfo record);

    int insertSelective(DeviceInfo record);

    DeviceInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceInfo record);

    int updateByPrimaryKey(DeviceInfo record);

    int updateBatch(List<DeviceInfo> list);

    int batchInsert(List<DeviceInfo> list);



	int deleteByDeviceId(String deviceId);



	DeviceInfo findOneByDeviceId(String deviceId);



}


