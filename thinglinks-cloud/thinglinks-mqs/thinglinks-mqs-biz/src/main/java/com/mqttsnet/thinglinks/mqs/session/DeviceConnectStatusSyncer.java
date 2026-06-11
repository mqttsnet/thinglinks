package com.mqttsnet.thinglinks.mqs.session;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 以事件 HLC 单调写更新 {@code device.connect_status}(event-time LWW CAS).
 * <p>仅当 DB 内 {@code last_status_event_hlc < eventHlc} 时才覆盖;CAS 拒绝 = 老事件迟到,保持现状.
 * <p>失败统一 warn 不抛,不阻塞主链路.
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceConnectStatusSyncer {

    private final DeviceOpenAnyUserFacade deviceApi;

    /**
     * 提交一次基于事件的连接状态同步.
     *
     * @param clientId MQTT clientId,空白跳过
     * @param status   本事件对应状态(CONNECT → ONLINE,其余 → OFFLINE)
     * @param eventHlc 上游因果时钟 HLC;null 或 &lt;=0 跳过,不用本机时钟兜底
     */
    public void sync(String clientId, DeviceConnectStatusEnum status, Long eventHlc) {
        if (StrUtil.isBlank(clientId) || status == null) {
            return;
        }
        if (eventHlc == null || eventHlc <= 0) {
            log.warn("[StatusSyncer] eventHlc missing/invalid, skip clientId={} status={}", clientId, status);
            return;
        }
        try {
            R<Boolean> r = deviceApi.updateDeviceConnectionStatusByEvent(clientId, status.getValue(), eventHlc);
            if (r == null || !Boolean.TRUE.equals(r.getIsSuccess())) {
                log.warn("[StatusSyncer] facade returned non-success clientId={} hlc={} status={} r={}", clientId, eventHlc, status, r);
                return;
            }
            if (Boolean.FALSE.equals(r.getData())) {
                // CAS 拒绝:老事件迟到,DB 已有更新 hlc;info 级便于排查抖动 / 时钟漂移
                log.info("[StatusSyncer] CAS rejected (stale event) clientId={} hlc={} status={}", clientId, eventHlc, status);
            } else {
                log.info("[StatusSyncer] applied clientId={} status={} hlc={}", clientId, status, eventHlc);
            }
        } catch (Exception e) {
            log.warn("[StatusSyncer] facade call failed (non-blocking) clientId={} hlc={} status={}", clientId, eventHlc, status, e);
        }
    }
}
