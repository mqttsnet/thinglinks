package com.mqttsnet.thinglinks.video.ws;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.event.DeviceAlarmEvent;
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
 * 告警 WebSocket 推送器。
 * <p>
 * 监听 DeviceAlarmEvent → 推送到前端告警中心实时弹窗。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@Component
public class AlarmWebSocketPusher {

    @EventListener
    public void onDeviceAlarm(DeviceAlarmEvent event) {
        Long tid = ObjectUtil.defaultIfNull(event.getTenantId(), ContextUtil.getTenantId());
        if (ObjectUtil.isNull(tid)) {
            return;
        }
        String tenantId = String.valueOf(tid);

        AlarmMessage msg = AlarmMessage.builder()
                .type("NEW_ALARM")
                .alarmId(event.getAlarmId())
                .deviceIdentification(event.getDeviceIdentification())
                .deviceName(event.getDeviceName())
                .alarmPriority(event.getAlarmPriority())
                .alarmDescription(event.getAlarmDescription())
                .alarmTime(Optional.ofNullable(event.getAlarmTime()).map(DateUtil::formatLocalDateTime).orElse(""))
                .build();

        VideoWebSocketSessionHolder.pushAlarm(tenantId, msg);
        log.debug("[WS-告警推送] 推送: tenantId={}, alarmId={}, device={}",
                tenantId, event.getAlarmId(), event.getDeviceIdentification());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmMessage {
        private String type;
        private Long alarmId;
        private String deviceIdentification;
        private String deviceName;
        private String alarmPriority;
        private String alarmDescription;
        private String alarmTime;
    }
}
