package com.mqttsnet.thinglinks.link.service.device;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceDatas;
    /**

* @Description:    java类作用描述
* @Author:         ShiHuan Sun
* @E-mail:         13733918655@163.com
* @Website:        http://thinglinks.mqttsnet.com
* @CreateDate:     2021/12/26$ 0:27$
* @UpdateUser:     ShiHuan Sun
* @UpdateDate:     2021/12/26$ 0:27$
* @UpdateRemark:   修改内容
* @Version:        1.0

*/
public interface DeviceDatasService{


    int deleteByPrimaryKey(Long id);

    int insert(DeviceDatas record);

    int insertOrUpdate(DeviceDatas record);

    int insertOrUpdateSelective(DeviceDatas record);

    int insertOrUpdateWithBLOBs(DeviceDatas record);

    int insertSelective(DeviceDatas record);

    DeviceDatas selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceDatas record);

    int updateByPrimaryKeyWithBLOBs(DeviceDatas record);

    int updateByPrimaryKey(DeviceDatas record);

    int updateBatch(List<DeviceDatas> list);

    int updateBatchSelective(List<DeviceDatas> list);

    int batchInsert(List<DeviceDatas> list);

    /**
     * thinglinks-mqtt基础数据处理
     * @param thinglinksMessage
     */
    void insertBaseDatas(String thinglinksMessage);

}
