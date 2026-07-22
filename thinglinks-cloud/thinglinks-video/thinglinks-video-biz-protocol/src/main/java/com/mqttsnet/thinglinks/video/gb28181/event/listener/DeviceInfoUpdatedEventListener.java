package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoUpdatedEvent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 设备信息更新事件监听者。
 * <p>设备信息一旦写库（UI 维护 / SIP 注册回写 / Keepalive 翻转），由本监听者统一回查 DB
 * 把缓存刷成最新值；任何下游链路都不要再凭旧缓存回写 DB，避免覆盖用户最新配置。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class DeviceInfoUpdatedEventListener {

    private final VideoDeviceService videoDeviceService;
    private final VideoCacheDataHelper videoCacheDataHelper;

    @EventListener
    public void handle(DeviceInfoUpdatedEvent event) {
        String deviceIdentification = event.getSource();
        if (deviceIdentification == null || deviceIdentification.isBlank()) {
            return;
        }
        try {
            VideoDeviceResultVO latest = videoDeviceService.getByDeviceIdentification(deviceIdentification);
            if (latest == null) {
                videoCacheDataHelper.removeDeviceInfo(deviceIdentification);
                log.debug("[设备缓存] 更新事件命中但 DB 已无该设备，仅清缓存: deviceIdentification={}", deviceIdentification);
                return;
            }
            VideoDeviceCacheVO cacheVO = BeanPlusUtil.toBeanIgnoreError(latest, VideoDeviceCacheVO.class);
            videoCacheDataHelper.setDeviceInfo(cacheVO);
            log.debug("[设备缓存] 已按 DB 最新值刷新: deviceIdentification={}", deviceIdentification);
        } catch (Exception e) {
            log.warn("[设备缓存] 更新事件处理失败，回退为清缓存（下次读自动溯源 DB）: deviceIdentification={}",
                    deviceIdentification, e);
            try {
                videoCacheDataHelper.removeDeviceInfo(deviceIdentification);
            } catch (Exception ignore) {
                // ignore
            }
        }
    }
}
