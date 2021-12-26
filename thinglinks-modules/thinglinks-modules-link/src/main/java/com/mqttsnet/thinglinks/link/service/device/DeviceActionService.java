package com.mqttsnet.thinglinks.link.service.device;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceAction;

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
public interface DeviceActionService {


    int deleteByPrimaryKey(Long id);

    int insert(DeviceAction record);

    int insertOrUpdate(DeviceAction record);

    int insertOrUpdateSelective(DeviceAction record);

    int insertOrUpdateWithBLOBs(DeviceAction record);

    int insertSelective(DeviceAction record);

    DeviceAction selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceAction record);

    int updateByPrimaryKeyWithBLOBs(DeviceAction record);

    int updateByPrimaryKey(DeviceAction record);

    int updateBatch(List<DeviceAction> list);

    int updateBatchSelective(List<DeviceAction> list);

    int batchInsert(List<DeviceAction> list);

    /**
     * 设备连接事件
     *
     * @param thinglinksMessage
     */
    void connectEvent(String thinglinksMessage);

    /**
     * 设备断开事件
     *
     * @param thinglinksMessage
     */
    void closeEvent(String thinglinksMessage);

}


