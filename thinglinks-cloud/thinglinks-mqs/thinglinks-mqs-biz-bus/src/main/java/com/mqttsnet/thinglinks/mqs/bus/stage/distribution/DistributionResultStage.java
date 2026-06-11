package com.mqttsnet.thinglinks.mqs.bus.stage.distribution;

import com.mqttsnet.thinglinks.bus.stage.AbstractDeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import com.mqttsnet.thinglinks.mqs.bus.support.BusStageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * broker 分发失败 Stage,POST 阶段,只处理 DISPATCH_ERROR(主要场景:下行命令投设备失败).
 * <p>
 * 成功回执(DISTED → mqtt.distribution.completed.topic)被复用作 PUBLISH 主流程走 DEVICE_DATA group;
 * 失败回执(DIST_ERROR → mqtt.distribution.error.topic)走本 Stage 记失败计数,
 * 用于运维监控下行命令送达率.命令状态表回写待业务明确后接入.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributionResultStage extends AbstractDeviceEventStage {

    private static final String STATS_DIM = "dispatch_error";

    private final BusStatsService statsService;

    @Override
    public StagePhaseEnum getPhase() {
        return StagePhaseEnum.POST;
    }

    @Override
    public int getOrder() {
        return 300;
    }

    @Override
    public boolean supports(DeviceProtocolEvent event) {
        return BusStageSupport.matchesAction(event, DeviceActionTypeEnum.DISPATCH_ERROR);
    }

    @Override
    protected void doExecute(DeviceProtocolEvent event, StageContext context) {
        log.info("[bus.dispatch-error] traceId={} clientId={} topic={}",
            event.getTraceId(), event.getClientId(), event.getTopic());
        statsService.increment(STATS_DIM, event.getProtocolType() + ":failure");
    }
}
