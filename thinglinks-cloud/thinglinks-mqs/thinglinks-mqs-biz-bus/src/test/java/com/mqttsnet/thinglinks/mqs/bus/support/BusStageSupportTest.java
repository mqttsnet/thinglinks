package com.mqttsnet.thinglinks.mqs.bus.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.mqttsnet.thinglinks.common.enums.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQS Bus 阶段动作匹配工具")
class BusStageSupportTest {

    @Test
    @DisplayName("验证动作字符串会裁剪空格并按枚举值大小写不敏感匹配")
    void actionTypeInMatchesCanonicalValuesCaseInsensitively() {
        assertThat(BusStageSupport.actionTypeIn("publish",
            DeviceActionTypeEnum.PUBLISH, DeviceActionTypeEnum.ERROR)).isTrue();
        assertThat(BusStageSupport.actionTypeIn(" PUBLISH ",
            DeviceActionTypeEnum.PUBLISH)).isTrue();
        assertThat(BusStageSupport.actionTypeIn("unknown-action",
            DeviceActionTypeEnum.PUBLISH)).isFalse();
    }

    @Test
    @DisplayName("验证事件动作匹配统一走共享枚举查找逻辑")
    void matchesActionUsesSharedEnumLookup() {
        DeviceProtocolEvent event = DeviceProtocolEvent.builder()
            .eventType("dispatch_error")
            .build();

        assertThat(BusStageSupport.matchesAction(event, DeviceActionTypeEnum.DISPATCH_ERROR)).isTrue();
        assertThat(BusStageSupport.matchesAction(event, DeviceActionTypeEnum.PUBLISH)).isFalse();
        assertThat(BusStageSupport.matchesAction(null, DeviceActionTypeEnum.PUBLISH)).isFalse();
    }
}
