package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOfflineEvent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description:
 * 设备离线事件监听者
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/22
 */
@Slf4j
@Component
public class DeviceInfoOfflineEventListener {

    @Autowired
    private VideoCacheDataHelper videoCacheDataHelper;


    @Autowired
    private VideoDeviceService videoDeviceService;


    @EventListener
    public void handleOfflineEvent(DeviceInfoOfflineEvent event) {
        if (Objects.isNull(event.getSource())) {
            return;
        }
        VideoDeviceResultVO deviceResultVO = event.getSource();
        log.info("[设备离线事件监听] 设备ID：" + deviceResultVO.getDeviceIdentification());

        //获取最新的DB数据
        VideoDeviceResultVO latestDevice = videoDeviceService.getByDeviceIdentification(deviceResultVO.getDeviceIdentification());
        // 将设备信息存入Redis缓存
        videoCacheDataHelper.setDeviceInfo(BeanPlusUtil.toBeanIgnoreError(latestDevice, VideoDeviceCacheVO.class));
    }
}
