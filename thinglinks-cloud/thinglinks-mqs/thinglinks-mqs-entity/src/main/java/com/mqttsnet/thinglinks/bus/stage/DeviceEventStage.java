package com.mqttsnet.thinglinks.bus.stage;

import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;

/**
 * 单一阶段执行单元,Pipeline 模式核心。同 phase 内按 {@link #getOrder} 升序;
 * PRE/CORE 同步,POST 异步;实现须无状态线程安全 + 幂等。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface DeviceEventStage {

    /**
     * 阶段名,用于日志 / 指标 / 失败定位(PascalCase,如 BridgeRelay)。
     */
    String getName();

    StagePhaseEnum getPhase();

    /**
     * 同 phase 排序,数字越小越先执行。
     */
    int getOrder();

    /**
     * 是否处理该事件,默认全部处理。
     */
    default boolean supports(DeviceProtocolEvent event) {
        return true;
    }

    /**
     * 执行业务逻辑;失败按 phase 语义传播。
     */
    void execute(DeviceProtocolEvent event, StageContext context);
}
