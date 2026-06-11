package com.mqttsnet.thinglinks.mqs.event.processor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.thinglinks.common.cache.mqs.heartbeat.DeviceHeartbeatReconcileCacheKeyBuilder;
import com.mqttsnet.thinglinks.constants.bus.BusKafkaJsonField;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备 PING 处理器 ── 续命设备心跳 + 按节流走 HLC CAS 校准在线(单调写,自愈生命周期漏写)。
 * 心跳时间与在线状态统一经 {@code reportDeviceHeartbeat} 一次上报;落库由 dispatcher 统一收口、
 * 桥接由 bus 传输层负责,本处理器不再涉及。
 *
 * @author mqttsnet
 * @since 2026-06-02
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DevicePingProcessor implements DeviceEventProcessor {

    private final DeviceOpenAnyUserFacade deviceOpenAnyUserApi;
    private final CachePlusOps cachePlusOps;

    /**
     * 是否处理该动作类型。
     *
     * @param type 设备动作类型
     * @return 仅 {@link DeviceActionTypeEnum#PING} 返 {@code true}
     */
    @Override
    public boolean supports(DeviceActionTypeEnum type) {
        return DeviceActionTypeEnum.PING == type;
    }

    /**
     * 续命心跳 + 在线状态校准,统一经 {@code reportDeviceHeartbeat} 一次上报:
     * <ul>
     *   <li><b>心跳时间</b>:每条 PING 无条件续(facade 内更新 {@code last_heartbeat_time})。</li>
     *   <li><b>在线状态</b>:eventHlc 有效且过 60s 节流窗时,才带 hlc 触发 facade 内 <b>HLC CAS 单调写</b> ONLINE
     *       (防迟到/乱序事件把已离线翻回在线);窗内只续心跳不写状态。</li>
     * </ul>
     * 失败抛 → DLT 重试,PING 重放幂等。
     *
     * @param event 设备通用事件
     */
    @Override
    public void process(CommonDeviceEvent event) {
        Long heartbeatTime = extractHeartbeatTime(event);
        Long eventHlc = event.getEventHlc();
        // 在线状态:eventHlc 有效 + 过节流窗 才带上,触发 reportDeviceHeartbeat 内的 CAS 置在线;否则传 null 只续心跳
        Long statusHlc = (eventHlc != null && eventHlc > 0 && onlineReconcileDue(event.getClientId()))
                ? eventHlc : null;
        if (heartbeatTime == null && statusHlc == null) {
            return;
        }
        R<Boolean> r = deviceOpenAnyUserApi.reportDeviceHeartbeat(event.getClientId(), heartbeatTime, statusHlc);
        if (!Boolean.TRUE.equals(r.getIsSuccess())) {
            throw new IllegalStateException("reportDeviceHeartbeat failed clientId=" + event.getClientId()
                    + " msg=" + r.getMsg());
        }
    }

    /**
     * 在线状态 CAS 单调写 60s 节流:窗内返 {@code false}(仅续心跳不写状态),
     * 窗外占位并返 {@code true} 触发本次 CAS。节流键查询/占位异常一律保守返 {@code false}(只续心跳)。
     *
     * @param clientId 设备 clientId
     * @return 本次 PING 是否应触发在线状态 CAS
     */
    private boolean onlineReconcileDue(String clientId) {
        try {
            CacheKey throttleKey = DeviceHeartbeatReconcileCacheKeyBuilder.build(clientId);
            Long ttl = cachePlusOps.ttl(throttleKey);
            if (ttl != null && ttl > 0) {
                return false;
            }
            cachePlusOps.set(throttleKey, "1");
            return true;
        } catch (Exception e) {
            log.warn("[DevicePing] reconcile throttle check failed (skip status) clientId={}", clientId, e);
            return false;
        }
    }

    /**
     * 心跳时间提取:rawMessage.heartbeatTime → {@code event.ts}(eventUtc)兜底;均无返 {@code null}。
     *
     * @param event 设备通用事件
     * @return 心跳时间 epoch ms,无返 {@code null}
     */
    private Long extractHeartbeatTime(CommonDeviceEvent event) {
        if (StrUtil.isNotBlank(event.getRawMessage())) {
            try {
                String hb = JSON.parseObject(event.getRawMessage()).getString(BusKafkaJsonField.HEARTBEAT_TIME);
                if (StrUtil.isNotBlank(hb)) {
                    return Long.parseLong(hb);
                }
            } catch (Exception ignored) {
                // 解析失败走 ts 兜底
            }
        }
        return event.getTs();
    }
}
