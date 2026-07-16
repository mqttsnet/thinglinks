package com.mqttsnet.thinglinks.mqs.bus.stage.distribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.mqttsnet.thinglinks.bus.stage.StageContext;
import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import com.mqttsnet.thinglinks.mqs.bus.stats.BusStatsService;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS 分发结果统计阶段")
class DistributionResultStageTest {

    private final BusStatsService statsService = org.mockito.Mockito.mock(BusStatsService.class);
    private final DistributionResultStage stage = new DistributionResultStage(statsService);

    @Test
    @DisplayName("验证仅 DISPATCH_ERROR 事件进入分发失败统计阶段")
    void supportsDispatchErrorCaseInsensitively() {
        assertThat(stage.supports(event("dispatch_error"))).isTrue();
        assertThat(stage.supports(event(DeviceActionTypeEnum.PUBLISH.getValue()))).isFalse();
    }

    @Test
    @DisplayName("验证分发失败事件按协议维度记录失败计数")
    void executeRecordsDispatchFailureCounter() {
        DeviceProtocolEvent event = event(DeviceActionTypeEnum.DISPATCH_ERROR.getValue());

        stage.execute(event, StageContext.create(event));

        verify(statsService).increment("dispatch_error", ProtocolTypeEnum.MQTT.getValue() + ":failure");
    }

    private static DeviceProtocolEvent event(String actionType) {
        return DeviceProtocolEvent.builder()
            .traceId("trace-1")
            .eventType(actionType)
            .protocolType(ProtocolTypeEnum.MQTT.getValue())
            .clientId("client-1")
            .topic("/downlink")
            .build();
    }
}
