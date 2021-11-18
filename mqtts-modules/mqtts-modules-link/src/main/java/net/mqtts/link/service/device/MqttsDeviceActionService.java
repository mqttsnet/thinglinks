package net.mqtts.link.service.device;

import java.util.List;
import net.mqtts.link.domain.device.MqttsDeviceAction;
    /**

* @Description:    java类作用描述

* @Author:         ShiHuan Sun

* @E-mail:          13733918655@163.com

* @Website:         http://mqtts.net

* @CreateDate:     2021/11/18$ 9:41$

* @UpdateUser:     ShiHuan Sun

* @UpdateDate:     2021/11/18$ 9:41$

* @UpdateRemark:   修改内容

* @Version:        1.0

*/
public interface MqttsDeviceActionService{


    int deleteByPrimaryKey(Long id);

    int insert(MqttsDeviceAction record);

    int insertOrUpdate(MqttsDeviceAction record);

    int insertOrUpdateSelective(MqttsDeviceAction record);

    int insertOrUpdateWithBLOBs(MqttsDeviceAction record);

    int insertSelective(MqttsDeviceAction record);

    MqttsDeviceAction selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MqttsDeviceAction record);

    int updateByPrimaryKeyWithBLOBs(MqttsDeviceAction record);

    int updateByPrimaryKey(MqttsDeviceAction record);

    int updateBatch(List<MqttsDeviceAction> list);

    int updateBatchSelective(List<MqttsDeviceAction> list);

    int batchInsert(List<MqttsDeviceAction> list);

}
