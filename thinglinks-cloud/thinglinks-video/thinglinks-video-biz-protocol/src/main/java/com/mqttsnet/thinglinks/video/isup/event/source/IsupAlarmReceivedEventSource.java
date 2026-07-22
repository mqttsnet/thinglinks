package com.mqttsnet.thinglinks.video.isup.event.source;

import com.mqttsnet.basic.base.entity.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Description:
 * ISUP 告警接收事件源。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsupAlarmReceivedEventSource extends Entity<Long> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备序列号
     */
    private String deviceSerial;

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 告警类型
     */
    private String alarmType;

    /**
     * 告警数据（JSON 格式）
     */
    private String alarmData;
}
