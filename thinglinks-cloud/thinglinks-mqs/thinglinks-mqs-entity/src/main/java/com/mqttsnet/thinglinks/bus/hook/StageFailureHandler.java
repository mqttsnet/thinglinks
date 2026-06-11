package com.mqttsnet.thinglinks.bus.hook;

import com.mqttsnet.thinglinks.bus.stage.DeviceEventStage;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;

/**
 * Stage 失败处理器 SPI,stage 抛异常时调用(降级 / 报警 / DLT 转储)。
 * 实现内部应 try/catch 兜底,不应抛异常污染主流程。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface StageFailureHandler {

    /**
     * 是否处理本次失败,多实例时 supports 返 true 的都依次执行。
     */
    default boolean supports(DeviceEventStage stage, Throwable error) {
        return true;
    }

    void onFailure(DeviceEventStage stage, DeviceProtocolEvent event, Throwable error);
}
