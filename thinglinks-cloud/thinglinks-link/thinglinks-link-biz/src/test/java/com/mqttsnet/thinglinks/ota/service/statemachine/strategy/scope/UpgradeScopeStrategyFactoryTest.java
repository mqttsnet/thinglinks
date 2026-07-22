package com.mqttsnet.thinglinks.ota.service.statemachine.strategy.scope;

import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeScopeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OTA升级范围策略工厂")
class UpgradeScopeStrategyFactoryTest {

    @Test
    @DisplayName("注册策略时忽略空策略和空范围策略，并支持按枚举值获取")
    void factoryShouldRegisterOnlyValidStrategies() {
        UpgradeScopeStrategy allDevicesStrategy = strategy(OtaUpgradeScopeEnum.ALL_DEVICES);
        UpgradeScopeStrategy nullScopeStrategy = strategy(null);
        UpgradeScopeStrategyFactory factory = new UpgradeScopeStrategyFactory(
                List.of(allDevicesStrategy, nullScopeStrategy));

        assertThat(factory.getStrategy(OtaUpgradeScopeEnum.ALL_DEVICES)).containsSame(allDevicesStrategy);
        assertThat(factory.getStrategy(OtaUpgradeScopeEnum.TARGETED)).isEmpty();
        assertThat(factory.supports(OtaUpgradeScopeEnum.ALL_DEVICES)).isTrue();
        assertThat(factory.supports(OtaUpgradeScopeEnum.TARGETED)).isFalse();
    }

    @Test
    @DisplayName("重复注册同一升级范围时保留第一个策略，避免后续Bean覆盖既有行为")
    void factoryShouldKeepFirstStrategyWhenDuplicateScopeRegistered() {
        UpgradeScopeStrategy first = strategy(OtaUpgradeScopeEnum.ALL_DEVICES);
        UpgradeScopeStrategy second = strategy(OtaUpgradeScopeEnum.ALL_DEVICES);
        UpgradeScopeStrategyFactory factory = new UpgradeScopeStrategyFactory(List.of(first, second));

        assertThat(factory.getStrategyRequired(OtaUpgradeScopeEnum.ALL_DEVICES)).isSameAs(first);
    }

    @Test
    @DisplayName("未知升级范围使用强制获取时抛业务异常，调用方可以明确失败原因")
    void getStrategyRequiredShouldThrowWhenUnsupported() {
        UpgradeScopeStrategyFactory factory = new UpgradeScopeStrategyFactory(List.of());

        assertThrows(BizException.class, () -> factory.getStrategyRequired(999));
    }

    private UpgradeScopeStrategy strategy(OtaUpgradeScopeEnum supportedScope) {
        return new UpgradeScopeStrategy() {
            @Override
            public Optional<List<DeviceResultVO>> getScopeDevices(OtaUpgradeTasksResultDTO upgradeTask) {
                return Optional.empty();
            }

            @Override
            public boolean supports(Integer upgradeScope) {
                return supportedScope != null && supportedScope.getValue().equals(upgradeScope);
            }

            @Override
            public OtaUpgradeScopeEnum getSupportedScope() {
                return supportedScope;
            }
        };
    }
}
