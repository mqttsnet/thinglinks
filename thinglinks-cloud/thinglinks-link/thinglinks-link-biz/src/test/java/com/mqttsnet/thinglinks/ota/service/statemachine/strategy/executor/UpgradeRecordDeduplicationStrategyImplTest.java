package com.mqttsnet.thinglinks.ota.service.statemachine.strategy.executor;

import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeRecordsResultDTO;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeRecordsService;
import com.mqttsnet.thinglinks.ota.service.statemachine.strategy.executor.impl.UpgradeRecordDeduplicationStrategyImpl;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeRecordsResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OTA升级记录去重策略")
class UpgradeRecordDeduplicationStrategyImplTest {

    @Mock
    private OtaUpgradeRecordsService otaUpgradeRecordsService;

    @Test
    @DisplayName("同一任务和设备已存在升级记录时判定为重复")
    void hasUpgradeRecordShouldReturnTrueWhenRecordExists() {
        when(otaUpgradeRecordsService.getByTaskIdAndDeviceIdentification(1L, "device-a"))
                .thenReturn(Optional.of(record()));
        UpgradeRecordDeduplicationStrategyImpl strategy =
                new UpgradeRecordDeduplicationStrategyImpl(otaUpgradeRecordsService);

        assertThat(strategy.hasUpgradeRecord(1L, "device-a")).isTrue();
    }

    @Test
    @DisplayName("查询升级记录异常时不过滤设备，避免误判跳过升级")
    void hasUpgradeRecordShouldReturnFalseWhenQueryFails() {
        when(otaUpgradeRecordsService.getByTaskIdAndDeviceIdentification(1L, "device-a"))
                .thenThrow(new IllegalStateException("db timeout"));
        UpgradeRecordDeduplicationStrategyImpl strategy =
                new UpgradeRecordDeduplicationStrategyImpl(otaUpgradeRecordsService);

        assertThat(strategy.hasUpgradeRecord(1L, "device-a")).isFalse();
    }

    @Test
    @DisplayName("按任务和设备查询记录时会把VO转换为状态机使用的DTO")
    void getUpgradeRecordShouldConvertVoToDto() {
        when(otaUpgradeRecordsService.getByTaskIdAndDeviceIdentification(1L, "device-a"))
                .thenReturn(Optional.of(record()));
        UpgradeRecordDeduplicationStrategyImpl strategy =
                new UpgradeRecordDeduplicationStrategyImpl(otaUpgradeRecordsService);

        Optional<OtaUpgradeRecordsResultDTO> result =
                strategy.getUpgradeRecordByTaskIdAndDeviceIdentification(1L, "device-a");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getTaskId()).isEqualTo(1L);
        assertThat(result.get().getDeviceIdentification()).isEqualTo("device-a");
        assertThat(result.get().getTargetVersion()).isEqualTo("2.0.1");
    }

    @Test
    @DisplayName("缺少任务ID或设备标识时直接返回空并不访问存储")
    void getUpgradeRecordShouldSkipInvalidArguments() {
        UpgradeRecordDeduplicationStrategyImpl strategy =
                new UpgradeRecordDeduplicationStrategyImpl(otaUpgradeRecordsService);

        assertThat(strategy.getUpgradeRecordByTaskIdAndDeviceIdentification(null, "device-a")).isEmpty();
        assertThat(strategy.getUpgradeRecordByTaskIdAndDeviceIdentification(1L, "")).isEmpty();
        verify(otaUpgradeRecordsService, never()).getByTaskIdAndDeviceIdentification(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString());
    }

    private OtaUpgradeRecordsResultVO record() {
        return new OtaUpgradeRecordsResultVO()
                .setId(10L)
                .setTaskId(1L)
                .setDeviceIdentification("device-a")
                .setTargetVersion("2.0.1");
    }
}
