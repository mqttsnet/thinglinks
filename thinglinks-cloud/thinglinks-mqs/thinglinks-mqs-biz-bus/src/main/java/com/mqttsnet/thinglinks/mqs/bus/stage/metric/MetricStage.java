package com.mqttsnet.thinglinks.mqs.bus.stage.metric;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.bus.stage.AbstractDeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.constants.bus.BusConstants;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.DispatchGroupEnum;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 指标 Stage,POST 阶段,Redis 计数"事件成功穿过 PRE+CORE 落到 POST"维度。
 * dispatch_total 由 dispatcher 单点收口,本 stage 不重复写。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricStage extends AbstractDeviceEventStage {

    private static final String LABEL_PREFIX = "MetricStage";
    private static final String UNKNOWN = "UNKNOWN";
    /**
     * 穿透标识,区分于 stage_executions 维度内其它 stage 的 SUCCESS/FAILED label。
     */
    private static final String STATUS_THROUGH = "through";

    private final BusStatsService statsService;

    @Override
    public StagePhaseEnum getPhase() {
        return StagePhaseEnum.POST;
    }

    @Override
    public int getOrder() {
        return 900;
    }

    @Override
    protected void doExecute(DeviceProtocolEvent event, StageContext context) {
        String label = String.join(":",
            LABEL_PREFIX,
            StrUtil.nullToDefault(event.getProtocolType(), UNKNOWN),
            StrUtil.nullToDefault(event.getEventType(), UNKNOWN),
            context.get(BusConstants.Ctx.DISPATCH_GROUP, DispatchGroupEnum.class)
                .map(DispatchGroupEnum::name)
                .orElse(DispatchGroupEnum.DEVICE_DATA.name()));
        statsService.incrementStage(label, StagePhaseEnum.POST.getValue(), STATUS_THROUGH);
    }
}
