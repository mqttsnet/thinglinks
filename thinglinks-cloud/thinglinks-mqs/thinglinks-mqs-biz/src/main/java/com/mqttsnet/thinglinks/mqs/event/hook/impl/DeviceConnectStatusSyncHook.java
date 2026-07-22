package com.mqttsnet.thinglinks.mqs.event.hook.impl;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventContext;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventHook;
import com.mqttsnet.thinglinks.mqs.event.hook.DeviceEventHookOrder;
import com.mqttsnet.thinglinks.mqs.session.DeviceConnectStatusSyncer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备连接状态同步 hook ── 以事件 HLC 单调写更新 {@code device.connect_status}.
 * 协议中立,所有连接生命周期事件统一在此集中处理,各 Processor 不直接写状态字段.
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceConnectStatusSyncHook implements DeviceEventHook {

    private final DeviceConnectStatusSyncer syncer;

    /**
     * {@inheritDoc}
     * <p>固定返 {@link DeviceEventHookOrder#CONNECT_STATUS_SYNC}(100) ──
     * 排在 METRICS_ENTRY(50) 之后、业务联动 hook 之前.
     */
    @Override
    public int getOrder() {
        return DeviceEventHookOrder.CONNECT_STATUS_SYNC.getOrder();
    }

    /**
     * {@inheritDoc}
     * <p>仅对影响连接状态的动作生效:CONNECT / DISCONNECT / CLOSE / KICKED / HEART_TIMEOUT / ERROR
     * (参见 {@link DeviceActionTypeEnum#affectsConnectionStatus}).
     * 其它 actionType(PUBLISH / PING / SUBSCRIBE 等)跳过,避免无谓 broker 探活开销.
     */
    @Override
    public boolean supports(DeviceActionTypeEnum type) {
        return type != null && type.affectsConnectionStatus();
    }

    /**
     * {@inheritDoc}
     * <p>
     * 调 {@link DeviceConnectStatusSyncer#sync} 以事件时间戳做单调写更新 {@code device.connect_status}.
     * 目标状态由本 hook 显式映射:
     * CONNECT → ONLINE,其余(DISCONNECT/CLOSE/KICKED/HEART_TIMEOUT/ERROR)→ OFFLINE.
     * <p>
     * 单调写键的优先级:
     * <ol>
     *   <li>{@code event.getEventHlc()} ── 上游因果时钟 HLC,plugin 改造后最高精度</li>
     *   <li>{@code event.getTs()} ── 物理 ms 时间戳,plugin 改造前 fallback(精度递减但仍能 CAS 单调写)</li>
     * </ol>
     * 两者皆 null 时 syncer 内部判 null 跳过.失败抛异常由 dispatcher fireHooks 强隔离,不影响主链路.
     */
    @Override
    public void beforeDispatch(DeviceEventContext ctx) {
        CommonDeviceEvent event = ctx.getEvent();
        Long syncKey = event.getEventHlc() != null ? event.getEventHlc() : event.getTs();
        syncer.sync(ctx.getClientId(), fallbackConnectStatus(ctx.getType()), syncKey);
    }

    private DeviceConnectStatusEnum fallbackConnectStatus(DeviceActionTypeEnum type) {
        return DeviceActionTypeEnum.CONNECT.equals(type)
                ? DeviceConnectStatusEnum.ONLINE
                : DeviceConnectStatusEnum.OFFLINE;
    }
}
