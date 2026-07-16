package com.mqttsnet.thinglinks.mqs.event.hook;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * 设备事件 hook 上下文 ── {@link DeviceEventHook} 各阶段入参载体.
 * 不可变 + 包装一个 {@link CommonDeviceEvent} 直引用,各 hook 共享同一份解析结果不重复解析.
 *
 * @author mqttsnet
 * @since 2026-05-14
 */
@Getter
@ToString
@RequiredArgsConstructor
public class DeviceEventContext {

    /**
     * 原始事件 ── 字段全部从此处取
     */
    private final CommonDeviceEvent event;

    /**
     * dispatcher 进入时间(epoch ms) ── 用于 hook 计算耗时
     */
    private final long enteredAtMillis;

    /**
     * 直通 {@link CommonDeviceEvent#getActionType()}.
     * <p>提供为顶层快捷访问,避免 hook 内 ctx.getEvent().getActionType() 二跳.
     *
     * @return 当前事件动作类型
     */
    public DeviceActionTypeEnum getType() {
        return event.getActionType();
    }

    /**
     * 直通 {@link CommonDeviceEvent#getClientId()}.
     *
     * @return 客户端 ID
     */
    public String getClientId() {
        return event.getClientId();
    }

    /**
     * 直通 {@link CommonDeviceEvent#getTenantId()}.
     *
     * @return 租户 ID(String 形态,与 envelope / ContextUtil 全链路对齐)
     */
    public String getTenantId() {
        return event.getTenantId();
    }

    /**
     * 直通 {@link CommonDeviceEvent#getRawMessage()}.
     *
     * @return 上游原始 JSON 报文
     */
    public String getRawMessage() {
        return event.getRawMessage();
    }
}
