package com.mqttsnet.thinglinks.video.notify;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.video.event.DeviceAlarmEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Description:
 * 设备告警通知监听器。
 * <p>
 * 监听 DeviceAlarmEvent → 构建 NotifyRequest → 调用 VideoNotifyDispatcher 分发。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAlarmNotificationListener {

    private final VideoNotifyDispatcher dispatcher;

    @EventListener
    public void onDeviceAlarm(DeviceAlarmEvent event) {
        Map<String, String> variables = new HashMap<>();
        variables.put("deviceIdentification", StrUtil.nullToDefault(event.getDeviceIdentification(), ""));
        variables.put("deviceName", StrUtil.nullToDefault(event.getDeviceName(), ""));
        variables.put("channelIdentification", StrUtil.nullToDefault(event.getChannelIdentification(), ""));
        variables.put("alarmPriority", StrUtil.nullToDefault(event.getAlarmPriority(), ""));
        variables.put("alarmType", StrUtil.nullToDefault(event.getAlarmType(), ""));
        variables.put("alarmDescription", StrUtil.nullToDefault(event.getAlarmDescription(), ""));
        variables.put("eventTime", Optional.ofNullable(event.getAlarmTime()).map(DateUtil::formatLocalDateTime).orElse(""));
        variables.put("bizId", String.valueOf(event.getAlarmId()));
        variables.put("bizType", "VIDEO_ALARM");

        String title = String.format("[告警] %s",
                StrUtil.isNotBlank(event.getDeviceName()) ? event.getDeviceName() : event.getDeviceIdentification());

        NotifyRequest request = NotifyRequest.builder()
                .eventType("ALARM")
                .title(title)
                .priority(event.getAlarmPriority())
                .variables(variables)
                .sourceCreatedBy(event.getDeviceCreatedBy())
                .sourceCreatedOrgId(event.getDeviceCreatedOrgId())
                .bizType("VIDEO_ALARM")
                .bizId(String.valueOf(event.getAlarmId()))
                .contextLocalMap(ContextUtil.getLocalMap())
                .build();

        dispatcher.dispatch(request);
    }
}
