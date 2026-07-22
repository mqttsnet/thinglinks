package com.mqttsnet.thinglinks.video.isup.cmd;

import com.mqttsnet.thinglinks.video.isup.sdk.IsupSdkAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Description:
 * ISUP 告警命令执行器。
 * 封装告警订阅和取消订阅的 SDK 调用逻辑。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IsupAlarmCommander {

    private final IsupSdkAdapter isupSdkAdapter;

    /**
     * 订阅设备告警。
     *
     * @param userId 登录句柄
     * @return 告警句柄，失败返回 {@code null}
     */
    public Integer subscribeAlarm(Integer userId) {
        log.info("ISUP订阅告警: userId={}", userId);
        Integer alarmHandle = isupSdkAdapter.setupAlarmChan(userId);
        if (alarmHandle == null) {
            log.error("ISUP告警订阅失败: userId={}", userId);
        }
        return alarmHandle;
    }

    /**
     * 取消告警订阅。
     *
     * @param alarmHandle 告警句柄
     * @return 是否取消成功
     */
    public boolean unsubscribeAlarm(Integer alarmHandle) {
        log.info("ISUP取消告警订阅: alarmHandle={}", alarmHandle);
        return isupSdkAdapter.closeAlarmChan(alarmHandle);
    }
}
