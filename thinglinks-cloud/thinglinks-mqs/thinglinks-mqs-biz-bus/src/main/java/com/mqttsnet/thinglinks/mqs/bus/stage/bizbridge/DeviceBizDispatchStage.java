package com.mqttsnet.thinglinks.mqs.bus.stage.bizbridge;

import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.bus.stage.AbstractDeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.device.CommonDeviceEvent;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.mqs.bus.support.BusStageSupport;
import com.mqttsnet.thinglinks.mqs.event.assembler.CommonDeviceEventAssembler;
import com.mqttsnet.thinglinks.mqs.event.counter.LinkDataReportCounter;
import com.mqttsnet.thinglinks.mqs.event.dispatcher.DeviceEventDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * bus↔biz 唯一桥 Stage(CORE)── 装配 {@link CommonDeviceEvent} 委派 biz {@link DeviceEventDispatcher},
 * 与 POST 桥接/告警/统计链路平行解耦。是 bus 唯一接缝,biz 领域逻辑演进对 bus 无感。
 *
 * @author mqttsnet
 * @since 2026-06-02
 */
@Component
@RequiredArgsConstructor
public class DeviceBizDispatchStage extends AbstractDeviceEventStage {

    private final CommonDeviceEventAssembler assembler;
    private final DeviceEventDispatcher bizDeviceEventDispatcher;
    private final LinkDataReportCounter linkDataReportCounter;

    /**
     * 固定 CORE 阶段:业务委派是核心处理。
     *
     * @return {@link StagePhaseEnum#CORE}
     */
    @Override
    public StagePhaseEnum getPhase() {
        return StagePhaseEnum.CORE;
    }

    /**
     * CORE 唯一业务 stage,取居中序。
     *
     * @return 执行序 200
     */
    @Override
    public int getOrder() {
        return 200;
    }

    /**
     * 凡有 eventType 的事件均委派(全量业务事件)。
     *
     * @param event 协议事件
     * @return 有 eventType 返 {@code true}
     */
    @Override
    public boolean supports(DeviceProtocolEvent event) {
        return event != null && StrUtil.isNotBlank(event.getEventType());
    }

    /**
     * 装配领域事件委派 biz;PUBLISH 成功后计上行(桥接入站不走 bus,天然不计)。
     * 装配失败仅跳过不抛;biz 处理异常上抛 → CORE FAILED → DLT。
     *
     * @param event   协议事件(身份字段已由 enrich 富化)
     * @param context 流水线上下文
     */
    @Override
    protected void doExecute(DeviceProtocolEvent event, StageContext context) {
        Optional<CommonDeviceEvent> domainEvent = assembler.assemble(event);
        if (domainEvent.isEmpty()) {
            return;
        }
        bizDeviceEventDispatcher.dispatch(domainEvent.get());
        if (BusStageSupport.matchesAction(event, DeviceActionTypeEnum.PUBLISH)) {
            linkDataReportCounter.incrementUpLink();
        }
    }
}
