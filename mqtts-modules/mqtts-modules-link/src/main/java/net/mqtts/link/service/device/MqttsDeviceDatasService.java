package net.mqtts.link.service.device;

import com.alibaba.fastjson.JSONObject;
import net.mqtts.link.api.domain.MqttsDeviceDatas;

import java.util.List;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/18$ 9:41$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/18$ 9:41$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public interface MqttsDeviceDatasService {


    int deleteByPrimaryKey(Long id);

    int insert(MqttsDeviceDatas record);

    int insertOrUpdate(MqttsDeviceDatas record);

    int insertOrUpdateSelective(MqttsDeviceDatas record);

    int insertOrUpdateWithBLOBs(MqttsDeviceDatas record);

    int insertSelective(MqttsDeviceDatas record);

    List<MqttsDeviceDatas> selectMqttsDeviceDatasList(MqttsDeviceDatas record);

    MqttsDeviceDatas selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(MqttsDeviceDatas record);

    int updateByPrimaryKeyWithBLOBs(MqttsDeviceDatas record);

    int updateByPrimaryKey(MqttsDeviceDatas record);

    int updateBatch(List<MqttsDeviceDatas> list);

    int updateBatchSelective(List<MqttsDeviceDatas> list);

    int batchInsert(List<MqttsDeviceDatas> list);

    int deleteMqttsDeviceDatasByIds(Long[] ids);

    /**
     * mqtt基础数据处理
     * @param mqttsMessage
     */
    void insertBaseDatas(String mqttsMessage);
}
