package com.mqttsnet.thinglinks.link.service.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import java.util.List;
    /**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:22$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:22$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
public interface DeviceTopicService{


    int deleteByPrimaryKey(Long id);

    int insert(DeviceTopic record);

    int insertOrUpdate(DeviceTopic record);

    int insertOrUpdateSelective(DeviceTopic record);

    int insertSelective(DeviceTopic record);

    DeviceTopic selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceTopic record);

    int updateByPrimaryKey(DeviceTopic record);

    int updateBatch(List<DeviceTopic> list);

    int updateBatchSelective(List<DeviceTopic> list);

    int batchInsert(List<DeviceTopic> list);

}
