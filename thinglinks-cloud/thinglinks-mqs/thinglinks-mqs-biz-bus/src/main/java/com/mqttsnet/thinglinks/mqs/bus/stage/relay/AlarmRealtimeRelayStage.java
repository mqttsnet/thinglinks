package com.mqttsnet.thinglinks.mqs.bus.stage.relay;

import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.rocketmq.producer.RocketmqTemplate;
import com.mqttsnet.thinglinks.bus.stage.AbstractDeviceEventStage;
import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.mq.BizMqRouteConstant;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.enumeration.bus.StagePhaseEnum;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import com.mqttsnet.thinglinks.mqs.bus.support.BusStageSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 实时告警旁路 Stage,POST 阶段,PUBLISH / ERROR / DISPATCH_ERROR asyncSend
 * 到 {@link BizMqRouteConstant.Alarm#REALTIME}。tag = productIdentification。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmRealtimeRelayStage extends AbstractDeviceEventStage {

    private static final String MQ_ROCKETMQ = "rocketmq";
    private static final String STATUS_OK = "ok";
    private static final String STATUS_FAIL = "fail";
    private static final String STATUS_FAIL_SYNC = "fail-sync";
    private static final String STATUS_SKIP_NO_TEMPLATE = "skip-no-template";
    private static final String TAG_FALLBACK = "_";

    private final ObjectProvider<RocketmqTemplate> rocketmqTemplateProvider;
    private final BusStatsService statsService;

    @Override
    public StagePhaseEnum getPhase() {
        return StagePhaseEnum.POST;
    }

    @Override
    public int getOrder() {
        return 200;
    }

    @Override
    public boolean supports(DeviceProtocolEvent event) {
        return BusStageSupport.actionTypeIn(event,
            DeviceActionTypeEnum.PUBLISH,
            DeviceActionTypeEnum.ERROR,
            DeviceActionTypeEnum.DISPATCH_ERROR);
    }

    @Override
    protected void doExecute(DeviceProtocolEvent event, StageContext context) {
        Optional<RocketmqTemplate> template = Optional.ofNullable(rocketmqTemplateProvider.getIfAvailable());
        if (template.isEmpty()) {
            statsService.incrementRelay(getName(), MQ_ROCKETMQ,
                BizMqRouteConstant.Alarm.REALTIME, STATUS_SKIP_NO_TEMPLATE);
            log.debug("[bus.alarm.relay] template missing, skip traceId={}", event.getTraceId());
            return;
        }
        String destination = BizMqRouteConstant.Alarm.REALTIME + ":"
            + StrUtil.nullToDefault(event.getProductIdentification(), TAG_FALLBACK);
        try {
            template.get().asyncSend(destination, event, callback(event));
        } catch (Throwable e) {
            statsService.incrementRelay(getName(), MQ_ROCKETMQ,
                BizMqRouteConstant.Alarm.REALTIME, STATUS_FAIL_SYNC);
            log.warn("[bus.alarm.relay] asyncSend invocation failed traceId={}", event.getTraceId(), e);
        }
    }

    private SendCallback callback(DeviceProtocolEvent event) {
        return new SendCallback() {
            @Override
            public void onSuccess(SendResult r) {
                statsService.incrementRelay(getName(), MQ_ROCKETMQ,
                    BizMqRouteConstant.Alarm.REALTIME, STATUS_OK);
                log.debug("[bus.alarm.relay] ✓ traceId={} msgId={} status={}",
                    event.getTraceId(), r.getMsgId(), r.getSendStatus());
            }

            @Override
            public void onException(Throwable e) {
                statsService.incrementRelay(getName(), MQ_ROCKETMQ,
                    BizMqRouteConstant.Alarm.REALTIME, STATUS_FAIL);
                log.warn("[bus.alarm.relay] async send failed traceId={} err={}",
                    event.getTraceId(), e.getMessage());
            }
        };
    }
}
