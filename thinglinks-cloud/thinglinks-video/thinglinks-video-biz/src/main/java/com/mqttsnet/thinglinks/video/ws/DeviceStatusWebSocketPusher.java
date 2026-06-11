package com.mqttsnet.thinglinks.video.ws;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOfflineEvent;
import com.mqttsnet.thinglinks.video.dto.device.event.DeviceInfoOnlineEvent;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoDeviceResultVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Description:
 * 设备状态 WebSocket 推送器。
 * <p>
 * 监听 DeviceInfoOnlineEvent / DeviceInfoOfflineEvent → 推送到前端。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@Component
public class DeviceStatusWebSocketPusher {

    @EventListener
    public void onDeviceOnline(DeviceInfoOnlineEvent event) {
        Optional.ofNullable(event.getSource()).ifPresent(device ->
                pushDeviceStatus(device, "DEVICE_ONLINE", true));
    }

    @EventListener
    public void onDeviceOffline(DeviceInfoOfflineEvent event) {
        Optional.ofNullable(event.getSource()).ifPresent(device ->
                pushDeviceStatus(device, "DEVICE_OFFLINE", false));
    }

    private void pushDeviceStatus(VideoDeviceResultVO device, String type, boolean online) {
        Long tid = ContextUtil.getTenantId();
        if (ObjectUtil.isNull(tid)) {
            return;
        }

        DeviceStatusMessage msg = DeviceStatusMessage.builder()
                .type(type)
                .deviceIdentification(device.getDeviceIdentification())
                .deviceName(device.getDeviceName())
                .onlineStatus(online)
                .timestamp(DateUtil.now())
                .build();

        String tenantId = String.valueOf(tid);
        VideoWebSocketSessionHolder.pushDeviceStatus(tenantId, msg);
        log.debug("[WS-设备状态] 推送: tenantId={}, type={}, device={}", tenantId, type, device.getDeviceIdentification());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceStatusMessage {
        private String type;
        private String deviceIdentification;
        private String deviceName;
        private boolean onlineStatus;
        private String timestamp;
    }
}
