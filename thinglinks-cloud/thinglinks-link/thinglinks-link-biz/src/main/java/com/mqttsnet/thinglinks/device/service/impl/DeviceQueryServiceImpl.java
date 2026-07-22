package com.mqttsnet.thinglinks.device.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.device.manager.DeviceManager;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备基础信息只读查询 Service 实现,仅持有 {@link DeviceManager}、零下游 Service 依赖、类图为 DAG。
 * 产品信息不在 DeviceCacheVO 内嵌,消费方按需走 LinkCacheDataHelper(缓存聚合层统一出口)自行解析。
 *
 * @author mqttsnet
 * @since 2026-05-18
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceQueryServiceImpl implements DeviceQueryService {

    private final DeviceManager deviceManager;

    @Override
    public Optional<DeviceCacheVO> findDeviceCacheVO(String deviceIdOrClientId) {
        return deviceManager.findDeviceCacheVO(deviceIdOrClientId);
    }

    @Override
    public Long findDeviceTotal() {
        return deviceManager.findDeviceTotal();
    }

    @Override
    public IPage<DeviceResultVO> getPage(PageParams<DevicePageQuery> params) {
        IPage<Device> page = deviceManager.getPage(params);
        return BeanPlusUtil.toBeanPage(page, DeviceResultVO.class);
    }

    @Override
    public Long countByCertSerialNumber(String certSerialNumber) {
        return deviceManager.count(Wraps.<Device>lbQ()
            .eq(Device::getCertSerialNumber, certSerialNumber));
    }

    @Override
    public Long countOnlineByCertSerialNumber(String certSerialNumber) {
        return deviceManager.count(Wraps.<Device>lbQ()
            .eq(Device::getCertSerialNumber, certSerialNumber)
            .eq(Device::getConnectStatus, DeviceConnectStatusEnum.ONLINE.getValue()));
    }

    @Override
    public List<Device> listTopBoundDevicesByCertSerialNumber(String certSerialNumber, int limit) {
        return deviceManager.list(Wraps.<Device>lbQ()
            .eq(Device::getCertSerialNumber, certSerialNumber)
            .orderByDesc(Device::getLastHeartbeatTime)
            .last("LIMIT " + limit));
    }

    @Override
    public List<Device> listByCertSerialNumber(String certSerialNumber) {
        if (StrUtil.isBlank(certSerialNumber)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(deviceManager.list(Wraps.<Device>lbQ()
                .eq(Device::getCertSerialNumber, certSerialNumber)))
            .orElseGet(Collections::emptyList);
    }

    @Override
    public List<String> listDeviceIdentificationsByProduct(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return Collections.emptyList();
        }
        DevicePageQuery devicePageQuery = new DevicePageQuery();
        devicePageQuery.setProductIdentification(productIdentification);
        return deviceManager.getDevicList(devicePageQuery).stream().map(Device::getDeviceIdentification).toList();
    }

    @Override
    public List<Device> listRebindCursorPageByProduct(String productIdentification, Long afterId, int pageSize) {
        return deviceManager.listRebindCursorPageByProduct(productIdentification, afterId, pageSize);
    }
}
