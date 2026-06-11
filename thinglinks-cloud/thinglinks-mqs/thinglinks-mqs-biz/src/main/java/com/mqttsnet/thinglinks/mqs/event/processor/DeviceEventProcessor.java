package com.mqttsnet.thinglinks.mqs.event.processor;

import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;

/**
 * 设备事件 Processor 抽象 ── 一个 actionType 一个实现,Dispatcher 按 {@link #supports} 路由.
 * <p>
 * 跨协议生效:实现只关心动作语义(PUBLISH / CONNECT / etc.),不需要按 protocolType 分支.
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
public interface DeviceEventProcessor {

    /**
     * 本 Processor 接收的动作类型 ── 一个 Processor 精确匹配一种 {@link DeviceActionTypeEnum}.
     * <p>
     * {@code DeviceEventDispatcher.routeToProcessor} 按本方法路由,findFirst 命中即调用 {@link #process}.
     *
     * @param type 当前事件的动作类型(非 null)
     * @return 是否处理此 actionType
     */
    boolean supports(DeviceActionTypeEnum type);

    /**
     * 处理 {@link CommonDeviceEvent} 主路径.
     * <p>
     * 实现侧业务逻辑由各 actionType 决定:DeviceAction 持久化 / 物模型解析 / 桥接旁路 等.
     * 建议保持幂等 ── 上层 {@code DeviceEventDispatcher} 有 {@code @Retryable} 兜底,可能重投.
     *
     * @param event 协议中立事件,protocolType / actionType / clientId / tenantId 等已由 assembler 填好
     */
    void process(CommonDeviceEvent event);
}
