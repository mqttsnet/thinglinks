package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigHolder;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description:
 * 设备上线事件监听者
 * <p>
 * 1. 缓存设备信息到 Redis
 * 2. 异步发送 DeviceInfo + Catalog 查询，实现零配置自动接入
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/5/22
 */
@Slf4j
@Component
public class DeviceInfoOnlineEventListener {

    @Autowired
    private VideoCacheDataHelper videoCacheDataHelper;

    @Autowired
    private VideoDeviceService videoDeviceService;

    @Autowired
    private DeviceAutoQueryService deviceAutoQueryService;

    @EventListener
    public void handleOnlineEvent(DeviceInfoOnlineEvent event) {
        if (Objects.isNull(event.getSource())) {
            return;
        }
        VideoDeviceResultVO deviceResultVO = event.getSource();
        log.info("[设备上线事件监听] 设备ID：{}", deviceResultVO.getDeviceIdentification());

        // 1. 获取最新的DB数据并缓存到Redis
        VideoDeviceResultVO latestDevice = videoDeviceService.getByDeviceIdentification(deviceResultVO.getDeviceIdentification());
        VideoDeviceCacheVO cacheVO = BeanPlusUtil.toBeanIgnoreError(latestDevice, VideoDeviceCacheVO.class);
        cacheVO.setSipTransactionInfo(event.getSipTransactionInfo());
        videoCacheDataHelper.setDeviceInfo(cacheVO);

        // 2. 异步查询设备信息和通道目录
        // autoQueryDevice 内部用 ContextUtil.getLocalMap/setLocalMap 传递上下文
        deviceAutoQueryService.autoQueryDevice(
                latestDevice != null ? latestDevice : deviceResultVO,
                TenantSipConfigHolder.get());
    }
}
