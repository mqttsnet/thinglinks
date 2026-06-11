package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.RedisCacheStorage;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceInfoCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.VideoDeviceInfoResultDTO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Description:
 * 设备上线事件监听者
 *
 * @author Sun ShiHuan
 * @version 1.0.0
 * @since 2025/5/22
 */
@Slf4j
@Component
public class DeviceInfoOnlineEventListener {

    @Autowired
    private RedisCacheStorage redisCacheStorage;

    @Autowired
    private VideoDeviceInfoService videoDeviceInfoService;


    @EventListener
    public void handleOnlineEvent(DeviceInfoOnlineEvent event) {
        if (Objects.isNull(event.getSource())) {
            return;
        }
        VideoDeviceInfoResultDTO deviceInfoResultDTO = event.getSource();
        log.info("[设备上线事件监听] 设备ID：" + deviceInfoResultDTO.getDeviceIdentification());

        //获取最新的DB数据
        VideoDeviceInfoResultDTO videoDeviceInfoResultDTO = videoDeviceInfoService.getVideoDeviceInfoResultDTO(deviceInfoResultDTO.getDeviceIdentification());
        videoDeviceInfoResultDTO.setSipTransactionInfo(event.getSipTransactionInfo());
        // 将设备信息存入Redis缓存
        redisCacheStorage.setDeviceInfo(BeanPlusUtil.toBeanIgnoreError(videoDeviceInfoResultDTO, VideoDeviceInfoCacheVO.class));

    }
}
