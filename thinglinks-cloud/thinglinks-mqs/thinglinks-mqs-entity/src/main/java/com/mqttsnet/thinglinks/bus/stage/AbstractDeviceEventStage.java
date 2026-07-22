package com.mqttsnet.thinglinks.bus.stage;

import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Stage 基类(模板方法),getName 默认取 SimpleName。子类实现 {@link #doExecute};
 * 需要细控异常 / 指标的直接实现 {@link DeviceEventStage} 接口。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Slf4j
public abstract class AbstractDeviceEventStage implements DeviceEventStage {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public final void execute(DeviceProtocolEvent event, StageContext context) {
        doExecute(event, context);
    }

    /**
     * 子类实现真正业务逻辑。
     */
    protected abstract void doExecute(DeviceProtocolEvent event, StageContext context);
}
