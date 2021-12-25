package com.mqttsnet.thinglinks.link.service.device.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceDatasMapper;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceDatas;
import com.mqttsnet.thinglinks.link.service.device.DeviceDatasService;
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
@Service
@Slf4j
public class DeviceDatasServiceImpl implements DeviceDatasService{

    @Resource
    private DeviceDatasMapper deviceDatasMapper;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return deviceDatasMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(DeviceDatas record) {
        return deviceDatasMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(DeviceDatas record) {
        return deviceDatasMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(DeviceDatas record) {
        return deviceDatasMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertOrUpdateWithBLOBs(DeviceDatas record) {
        return deviceDatasMapper.insertOrUpdateWithBLOBs(record);
    }

    @Override
    public int insertSelective(DeviceDatas record) {
        return deviceDatasMapper.insertSelective(record);
    }

    @Override
    public DeviceDatas selectByPrimaryKey(Long id) {
        return deviceDatasMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(DeviceDatas record) {
        return deviceDatasMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKeyWithBLOBs(DeviceDatas record) {
        return deviceDatasMapper.updateByPrimaryKeyWithBLOBs(record);
    }

    @Override
    public int updateByPrimaryKey(DeviceDatas record) {
        return deviceDatasMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<DeviceDatas> list) {
        return deviceDatasMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<DeviceDatas> list) {
        return deviceDatasMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<DeviceDatas> list) {
        return deviceDatasMapper.batchInsert(list);
    }

    /**
     * thinglinks-mqtt基础数据处理
     *
     * @param thinglinksMessage
     */
    @Override
    public void insertBaseDatas(String thinglinksMessage) {
        log.info(thinglinksMessage);
    }

}
