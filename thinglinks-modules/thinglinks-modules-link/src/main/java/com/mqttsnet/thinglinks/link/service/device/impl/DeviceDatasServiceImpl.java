package com.mqttsnet.thinglinks.link.service.device.impl;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.utils.SubStringUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceDatas;
import com.mqttsnet.thinglinks.link.api.domain.product.entity.Product;
import com.mqttsnet.thinglinks.link.mapper.device.DeviceDatasMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceDatasService;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.product.ProductServicesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

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
@Service
@Slf4j
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class DeviceDatasServiceImpl implements DeviceDatasService {

    @Resource
    private DeviceDatasMapper deviceDatasMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductServicesService productServicesService;
    @Autowired
    private RedisService redisService;

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
    public void insertBaseDatas(JSONObject thinglinksMessage) {
        String topic = thinglinksMessage.getString("topic");
        String msg = thinglinksMessage.getString("msg");
        if (Objects.equals(msg, "{}")) {
            log.error("Topic:{},The entry is empty and ignored", topic);
            return;
        }
        //边设备上报数据处理
        if (topic.startsWith("/v1/devices/") && topic.endsWith("/datas")) {
            log.info("Side equipment report data processing,Topic:{},Msg:{}", topic, msg);
            final String deviceIdentification = SubStringUtil.subStr(topic,12,-6);



        }
    }

    /**
     * 处理datas Topic数据上报
     *
     * @param deviceIdentification 设备标识
     * @param msg                  数据
     */
    @Override
    public void processingDatasTopic(String deviceIdentification, String msg) throws Exception{
        final Device oneByDeviceIdentification = deviceService.findOneByDeviceIdentification(deviceIdentification);
        if (Objects.isNull(oneByDeviceIdentification)) {
            log.error("The side device reports data processing, but the device does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            return;
        }
        final Product oneByManufacturerIdAndModelAndDeviceType = productService.findOneByManufacturerIdAndModelAndDeviceType(oneByDeviceIdentification.getManufacturerId(), oneByDeviceIdentification.getProductId(), oneByDeviceIdentification.getProtocolType());
        if (Objects.isNull(oneByManufacturerIdAndModelAndDeviceType)) {
            log.error("The side device reports data processing, but the product does not exist,DeviceIdentification:{},Msg:{}", deviceIdentification, msg);
            return;
        }

    }

}


