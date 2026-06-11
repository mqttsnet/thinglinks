package com.mqttsnet.thinglinks.video.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Description:
 * 设备告警事件（协议无关，定义在 entity 层供 biz 和 protocol 共用）。
 * <p>
 * 事件携带完整上下文，监听器无需二次查库。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Getter
public class DeviceAlarmEvent extends ApplicationEvent {

    private final Long alarmId;
    private final String deviceIdentification;
    private final String deviceName;
    private final String channelIdentification;
    private final String alarmPriority;
    private final String alarmMethod;
    private final String alarmType;
    private final String alarmDescription;
    private final LocalDateTime alarmTime;
    private final Long tenantId;
    private final Long deviceCreatedBy;
    private final Long deviceCreatedOrgId;

    public DeviceAlarmEvent(Object source, Long alarmId, String deviceIdentification, String deviceName,
                            String channelIdentification, String alarmPriority, String alarmMethod,
                            String alarmType, String alarmDescription, LocalDateTime alarmTime,
                            Long tenantId, Long deviceCreatedBy, Long deviceCreatedOrgId) {
        super(source);
        this.alarmId = alarmId;
        this.deviceIdentification = deviceIdentification;
        this.deviceName = deviceName;
        this.channelIdentification = channelIdentification;
        this.alarmPriority = alarmPriority;
        this.alarmMethod = alarmMethod;
        this.alarmType = alarmType;
        this.alarmDescription = alarmDescription;
        this.alarmTime = alarmTime;
        this.tenantId = tenantId;
        this.deviceCreatedBy = deviceCreatedBy;
        this.deviceCreatedOrgId = deviceCreatedOrgId;
    }
}
