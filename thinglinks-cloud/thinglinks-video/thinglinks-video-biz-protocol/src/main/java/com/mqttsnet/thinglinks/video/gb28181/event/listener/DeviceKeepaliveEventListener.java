package com.mqttsnet.thinglinks.video.gb28181.event.listener;

import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceKeepaliveEvent;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 设备心跳事件统一监听器。
 * <p>
 * 接收 {@link DeviceKeepaliveEvent}（来源：SIP REGISTER 重复注册、
 * Keepalive MESSAGE 等），执行：
 * <ol>
 *   <li>拉取当前设备状态</li>
 *   <li>如果当前为离线 → 写 online_status=true</li>
 *   <li>永远刷新 last_keepalive_time = 事件携带的时间戳</li>
 *   <li>同步到 Redis 缓存</li>
 * </ol>
 * <p>
 * 写最小字段（不覆盖 registerTime、host 等），避免 REGISTER/Keepalive
 * 频繁写放大。
 *
 * @author mqttsnet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceKeepaliveEventListener {

    private final VideoDeviceService videoDeviceService;
    private final VideoCacheDataHelper videoCacheDataHelper;

    @EventListener
    public void handleKeepalive(DeviceKeepaliveEvent event) {
        String deviceId = event.getDeviceIdentification();
        String keepaliveTime = event.getKeepaliveTime();
        if (!StringUtils.hasText(deviceId) || !StringUtils.hasText(keepaliveTime)) {
            log.debug("[心跳事件] 缺少必要字段，忽略。deviceId={}, time={}", deviceId, keepaliveTime);
            return;
        }

        try {
            VideoDeviceResultVO device = videoDeviceService.getByDeviceIdentification(deviceId);
            if (device == null) {
                log.warn("[心跳事件] 未找到设备：{}（来源={}），跳过更新", deviceId, event.getOrigin());
                return;
            }

            boolean wasOffline = !Boolean.TRUE.equals(device.getOnlineStatus());

            // 1. DB：仅写最少字段
            VideoDeviceUpdateVO updateVO = VideoDeviceUpdateVO.builder()
                    .deviceIdentification(deviceId)
                    .lastKeepaliveTime(keepaliveTime)
                    .onlineStatus(true)
                    .build();
            videoDeviceService.updateDeviceInfo(updateVO);

            // 2. Redis：同步
            VideoDeviceCacheVO cacheVO = videoCacheDataHelper.getDeviceInfo(deviceId);
            if (cacheVO == null) {
                cacheVO = BeanPlusUtil.toBeanIgnoreError(device, VideoDeviceCacheVO.class);
            }
            if (cacheVO != null) {
                cacheVO.setLastKeepaliveTime(keepaliveTime);
                cacheVO.setOnlineStatus(true);
                try {
                    videoCacheDataHelper.setDeviceInfo(cacheVO);
                } catch (Exception e) {
                    log.warn("[心跳事件] Redis 缓存刷新失败，设备：{}，error={}", deviceId, e.getMessage());
                }
            }

            if (wasOffline) {
                log.info("[心跳事件] 设备 {} 由离线转为在线（来源={}）", deviceId, event.getOrigin());
            } else {
                log.debug("[心跳事件] 设备 {} 心跳刷新（来源={}），last={}", deviceId, event.getOrigin(), keepaliveTime);
            }
        } catch (Exception e) {
            log.error("[心跳事件] 处理失败：{}，error={}", deviceId, e.getMessage(), e);
        }
    }
}
