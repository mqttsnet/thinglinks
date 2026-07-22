package com.mqttsnet.thinglinks.video.service.device.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOfflineEvent;
import com.mqttsnet.thinglinks.video.service.device.DeviceKeepaliveTimeoutService;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoDeviceUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 设备心跳超时检测服务实现。
 * <p>
 * 以 DB（video_device.online_status=true）为权威候选集，对每台在线设备优先
 * 从 Redis 读取热心跳时间，缓存缺失时回退到 DB 字段。检测到超时后：
 * 1. 更新 DB 在线状态
 * 2. 更新 Redis 缓存
 * 3. 发布离线事件（触发 WebSocket 推送等）
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS(DsConstant.BASE_TENANT)
public class DeviceKeepaliveTimeoutServiceImpl implements DeviceKeepaliveTimeoutService {

    private final VideoCacheDataHelper videoCacheDataHelper;
    private final com.mqttsnet.thinglinks.video.service.device.VideoDeviceService videoDeviceService;
    private final ApplicationEventPublisher eventPublisher;

    private static final int DEFAULT_KEEPALIVE_INTERVAL = 60;
    private static final int DEFAULT_KEEPALIVE_TIMEOUT_COUNT = 3;

    @Override
    public void checkTimeoutForTenant(Long tenantId) {
        // DB 作为权威数据源：扫描所有当前标记为在线的设备，避免 Redis 缓存缺失导致的孤岛设备
        List<VideoDeviceResultVO> onlineDevices = videoDeviceService.listOnlineDevices();
        if (onlineDevices.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        int timeoutCount = 0;

        for (VideoDeviceResultVO dbDevice : onlineDevices) {
            // 优先读 Redis 热缓存的最近心跳；缓存缺失时用 DB 字段兜底
            VideoDeviceCacheVO cache = videoCacheDataHelper.getDeviceInfo(dbDevice.getDeviceIdentification());
            VideoDeviceCacheVO probe = (cache != null) ? cache : toCacheVO(dbDevice);
            if (isKeepaliveTimeout(probe, now)) {
                handleTimeout(probe);
                timeoutCount++;
            }
        }

        if (timeoutCount > 0) {
            log.info("[心跳监控] 租户={}, 本轮检测到 {} 个设备心跳超时", tenantId, timeoutCount);
        }
    }

    private VideoDeviceCacheVO toCacheVO(VideoDeviceResultVO dbDevice) {
        return BeanPlusUtil.toBeanIgnoreError(dbDevice, VideoDeviceCacheVO.class);
    }

    private boolean isKeepaliveTimeout(VideoDeviceCacheVO device, long now) {
        String lastKeepalive = device.getLastKeepaliveTime();
        if (StrUtil.isBlank(lastKeepalive)) {
            lastKeepalive = device.getRegisterTime();
        }
        if (StrUtil.isBlank(lastKeepalive)) {
            log.warn("[心跳监控] 设备: {} 无 lastKeepaliveTime/registerTime，视为超时",
                    device.getDeviceIdentification());
            return true;
        }

        try {
            Date lastTime = DateUtil.parse(lastKeepalive);
            int interval = device.getKeepaliveInterval() != null
                    ? device.getKeepaliveInterval() : DEFAULT_KEEPALIVE_INTERVAL;
            int maxTimeout = device.getKeepaliveTimeoutCount() != null
                    ? device.getKeepaliveTimeoutCount() : DEFAULT_KEEPALIVE_TIMEOUT_COUNT;
            long timeoutMillis = (long) interval * maxTimeout * 1000;

            return (now - lastTime.getTime()) > timeoutMillis;
        } catch (Exception e) {
            log.warn("[心跳监控] 解析心跳时间失败, 设备: {}, 时间: {}, 视为超时",
                    device.getDeviceIdentification(), lastKeepalive);
            return true;
        }
    }

    private void handleTimeout(VideoDeviceCacheVO device) {
        String deviceId = device.getDeviceIdentification();
        log.warn("[心跳超时] 设备: {} 心跳超时，标记离线", deviceId);

        try {
            // 1. 更新 DB
            VideoDeviceUpdateVO updateVO = VideoDeviceUpdateVO.builder()
                    .deviceIdentification(deviceId)
                    .onlineStatus(false)
                    .build();
            videoDeviceService.updateDeviceInfo(updateVO);

            // 2. 更新 Redis 缓存
            device.setOnlineStatus(false);
            videoCacheDataHelper.setDeviceInfo(device);

            // 3. 发布离线事件（触发 WebSocket 推送前端）
            VideoDeviceResultVO resultVO = new VideoDeviceResultVO();
            resultVO.setDeviceIdentification(deviceId);
            resultVO.setOnlineStatus(false);
            eventPublisher.publishEvent(new DeviceInfoOfflineEvent(resultVO));
        } catch (Exception e) {
            log.error("[心跳超时] 设备: {} 离线处理失败: {}", deviceId, e.getMessage());
        }
    }
}
