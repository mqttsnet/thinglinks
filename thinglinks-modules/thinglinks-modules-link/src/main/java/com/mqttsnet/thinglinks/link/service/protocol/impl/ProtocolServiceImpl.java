package com.mqttsnet.thinglinks.link.service.protocol.impl;

import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.exception.ServiceException;
import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.Device;
import com.mqttsnet.thinglinks.link.api.domain.protocol.Protocol;
import com.mqttsnet.thinglinks.link.mapper.protocol.ProtocolMapper;
import com.mqttsnet.thinglinks.link.service.device.DeviceService;
import com.mqttsnet.thinglinks.link.service.product.ProductService;
import com.mqttsnet.thinglinks.link.service.protocol.ProtocolService;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @program: thinglinks
 * @description: ${description}
 * @packagename: com.mqttsnet.thinglinks.link.service.protocol.impl
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-01 17:56
 **/
@Service
public class ProtocolServiceImpl implements ProtocolService {

    @Resource
    private ProtocolMapper protocolMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private ProductService productService;
    @Autowired
    private RedisService redisService;

    @Override
    public int deleteByPrimaryKey(Long id) {
        return protocolMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Protocol record) {
        return protocolMapper.insert(record);
    }

    @Override
    public int insertOrUpdate(Protocol record) {
        return protocolMapper.insertOrUpdate(record);
    }

    @Override
    public int insertOrUpdateSelective(Protocol record) {
        return protocolMapper.insertOrUpdateSelective(record);
    }

    @Override
    public int insertSelective(Protocol record) {
        return protocolMapper.insertSelective(record);
    }

    @Override
    public Protocol selectByPrimaryKey(Long id) {
        return protocolMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(Protocol record) {
        return protocolMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(Protocol record) {
        return protocolMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateBatch(List<Protocol> list) {
        return protocolMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<Protocol> list) {
        return protocolMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<Protocol> list) {
        return protocolMapper.batchInsert(list);
    }

    /**
     * ??????????????????
     *
     * @param id ??????????????????
     * @return ????????????
     */
    @Override
    public Protocol selectProtocolById(Long id) {
        return protocolMapper.selectProtocolById(id);
    }

    /**
     * ????????????????????????
     *
     * @param protocol ????????????
     * @return ????????????
     */
    @Override
    public List<Protocol> selectProtocolList(Protocol protocol) {
        return protocolMapper.selectProtocolList(protocol);
    }

    /**
     * ??????????????????
     *
     * @param protocol ????????????
     * @return ??????
     */
    @Override
    public int insertProtocol(Protocol protocol) {
        protocol.setCreateTime(DateUtils.dateToLocalDateTime(DateUtils.getNowDate()));
        int iexe = protocolMapper.insertProtocol(protocol);
        if (iexe != 1) {
            throw new ServiceException("??????????????????",1000);
        }
        return Integer.parseInt(protocol.getId().toString());
    }

    /**
     * ??????????????????
     *
     * @param protocol ????????????
     * @return ??????
     */
    @Override
    public int updateProtocol(Protocol protocol) {
        protocol.setUpdateTime(DateUtils.dateToLocalDateTime(DateUtils.getNowDate()));
        return protocolMapper.updateProtocol(protocol);
    }

    /**
     * ????????????????????????
     *
     * @param ids ?????????????????????????????????
     * @return ??????
     */
    @Override
    public int deleteProtocolByIds(Long[] ids) {
        return protocolMapper.deleteProtocolByIds(ids);
    }

    @Override
    public Protocol findOneByProductIdentificationAndProtocolTypeAndStatus(String productIdentification, String protocolType, String status) {
        return protocolMapper.findOneByProductIdentificationAndProtocolTypeAndStatus(productIdentification, protocolType, status);
    }

    /**
     * ????????????????????????
     *
     * @param ids
     * @return
     */
    @Override
    public int enable(Long[] ids) {
        List<Protocol> protocolList = protocolMapper.findAllByIdIn(Arrays.asList(ids));
        for (Protocol protocol : protocolList) {
            List<Device> deviceList = deviceService.findAllByProductIdentification(protocol.getProductIdentification());
            String content = StringEscapeUtils.unescapeHtml4(protocol.getContent());
            for (Device device : deviceList) {
                redisService.set(Constants.DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT + device.getDeviceIdentification(), content);
            }
            protocolMapper.updateStatusById(Constants.ENABLE, protocol.getId());
        }
        return protocolList.size();
    }

    /**
     * ????????????????????????
     *
     * @param ids
     * @return
     */
    @Override
    public int disable(Long[] ids) {
        List<Protocol> protocolList = protocolMapper.findAllByIdIn(Arrays.asList(ids));
        for (Protocol protocol : protocolList) {
            List<Device> deviceList = deviceService.findAllByProductIdentification(protocol.getProductIdentification());
            for (Device device : deviceList) {
                redisService.delete(Constants.DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT + protocol.getProtocolType() + device.getDeviceIdentification());
            }
            protocolMapper.updateStatusById(Constants.DISABLE, protocol.getId());
        }
        return protocolList.size();
    }

    @Override
    public List<Protocol> findAllByIdIn(Collection<Long> idCollection) {
        return protocolMapper.findAllByIdIn(idCollection);
    }

    @Override
    public int updateStatusById(String updatedStatus, Long id) {
        return protocolMapper.updateStatusById(updatedStatus, id);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    @Override
    public int protocolScriptCacheRefresh() {
        List<Protocol> protocolList = protocolMapper.findAllByStatus(Constants.ENABLE);
        for (Protocol protocol : protocolList) {
            List<Device> deviceList = deviceService.findAllByProductIdentification(protocol.getProductIdentification());
            for (Device device : deviceList) {
                redisService.delete(Constants.DEVICE_DATA_REPORTED_AGREEMENT_SCRIPT + protocol.getProtocolType() + device.getDeviceIdentification());
            }
            protocolMapper.updateStatusById(Constants.DISABLE, protocol.getId());
        }
        return protocolList.size();
    }

    @Override
    public List<Protocol> findAllByStatus(String status) {
        return protocolMapper.findAllByStatus(status);
    }


}
