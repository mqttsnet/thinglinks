package com.mqttsnet.thinglinks.video.metrics;

import com.mqttsnet.thinglinks.video.service.device.VideoDeviceAlarmService;
import com.mqttsnet.thinglinks.video.service.device.VideoDeviceService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * SIP / 视频模块 Prometheus 可观测性指标收集器。
 * <p>
 * 注册 Micrometer Gauge/Counter 指标，供 Prometheus 抓取。
 * <p>
 * 指标列表:
 * - sip_devices_online (Gauge): 在线设备数
 * - sip_devices_total (Gauge): 设备总数
 * - sip_alarms_unhandled (Gauge): 未处理告警数
 * - sip_invite_total (Counter): INVITE 事务总数
 * - sip_invite_success (Counter): INVITE 成功数
 * - sip_invite_fail (Counter): INVITE 失败数
 * - sip_alarms_total (Counter): 告警总数
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SipMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final VideoDeviceService videoDeviceService;
    private final VideoDeviceAlarmService videoDeviceAlarmService;

    // Counters (need to be incremented by event listeners)
    private Counter inviteTotalCounter;
    private Counter inviteSuccessCounter;
    private Counter inviteFailCounter;
    private Counter alarmsTotalCounter;

    @PostConstruct
    public void init() {
        // Gauge: 在线设备数
        Gauge.builder("sip.devices.online", this, SipMetricsCollector::countOnlineDevices)
            .description("Number of online SIP devices")
            .register(meterRegistry);

        // Gauge: 设备总数
        Gauge.builder("sip.devices.total", this, SipMetricsCollector::countTotalDevices)
            .description("Total number of SIP devices")
            .register(meterRegistry);

        // Gauge: 未处理告警数
        Gauge.builder("sip.alarms.unhandled", this, SipMetricsCollector::countUnhandledAlarms)
            .description("Number of unhandled alarms")
            .register(meterRegistry);

        // Counter: INVITE 事务
        inviteTotalCounter = Counter.builder("sip.invite.total")
            .description("Total INVITE transactions")
            .register(meterRegistry);
        inviteSuccessCounter = Counter.builder("sip.invite.success")
            .description("Successful INVITE transactions")
            .register(meterRegistry);
        inviteFailCounter = Counter.builder("sip.invite.fail")
            .description("Failed INVITE transactions")
            .register(meterRegistry);

        // Counter: 告警总数
        alarmsTotalCounter = Counter.builder("sip.alarms.total")
            .description("Total alarms received")
            .register(meterRegistry);

        log.info("[SIP 指标] Prometheus 指标注册完成");
    }

    // ===== Gauge 数据采集方法 =====

    private double countOnlineDevices() {
        try {
            return videoDeviceService.countOnline();
        } catch (Exception e) {
            return 0;
        }
    }

    private double countTotalDevices() {
        try {
            return videoDeviceService.countTotal();
        } catch (Exception e) {
            return 0;
        }
    }

    private double countUnhandledAlarms() {
        try {
            return videoDeviceAlarmService.unhandledCount();
        } catch (Exception e) {
            return 0;
        }
    }

    // ===== Counter 增量方法 (供事件监听器调用) =====

    public void incrementInviteTotal() {
        inviteTotalCounter.increment();
    }

    public void incrementInviteSuccess() {
        inviteSuccessCounter.increment();
    }

    public void incrementInviteFail() {
        inviteFailCounter.increment();
    }

    public void incrementAlarmsTotal() {
        alarmsTotalCounter.increment();
    }
}
