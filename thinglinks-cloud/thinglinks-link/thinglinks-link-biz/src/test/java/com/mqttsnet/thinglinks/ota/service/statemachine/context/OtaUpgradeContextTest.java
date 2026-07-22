package com.mqttsnet.thinglinks.ota.service.statemachine.context;

import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradesResultDTO;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeTaskStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OTA升级上下文")
class OtaUpgradeContextTest {

    @Test
    @DisplayName("升级任务缺少升级包信息时目标版本返回空字符串而不是抛异常")
    void getTargetVersionShouldReturnEmptyWhenPackageMissing() {
        OtaUpgradeContext context = OtaUpgradeContext.builder()
                .upgradeTask(new OtaUpgradeTasksResultDTO().setId(1L))
                .build();

        assertThat(context.getTargetVersion()).isEmpty();
    }

    @Test
    @DisplayName("升级任务存在升级包时返回升级包目标版本")
    void getTargetVersionShouldReturnPackageVersion() {
        OtaUpgradeContext context = OtaUpgradeContext.builder()
                .upgradeTask(new OtaUpgradeTasksResultDTO()
                        .setOtaUpgradesResult(new OtaUpgradesResultDTO().setVersion("2.0.1")))
                .build();

        assertThat(context.getTargetVersion()).isEqualTo("2.0.1");
    }

    @Test
    @DisplayName("清理单设备错误不依赖升级任务对象，避免重试前错误残留")
    void clearDeviceErrorShouldRemoveErrorWithoutUpgradeTask() {
        OtaUpgradeContext context = new OtaUpgradeContext();
        context.addDeviceError("device-a", "send timeout");

        context.clearDeviceError("device-a");

        assertThat(context.hasError()).isFalse();
        assertThat(context.getDeviceError("device-a")).isNull();
    }

    @Test
    @DisplayName("设置当前任务状态时同步写回升级任务状态值")
    void setCurrentStatusShouldWriteBackTaskStatus() {
        OtaUpgradeTasksResultDTO task = new OtaUpgradeTasksResultDTO()
                .setTaskStatus(OtaUpgradeTaskStatusEnum.PENDING.getValue());
        OtaUpgradeContext context = OtaUpgradeContext.builder().upgradeTask(task).build();

        context.setCurrentStatus(OtaUpgradeTaskStatusEnum.IN_PROGRESS);

        assertThat(task.getTaskStatus()).isEqualTo(OtaUpgradeTaskStatusEnum.IN_PROGRESS.getValue());
        assertThat(context.getCurrentStatus()).isEqualTo(OtaUpgradeTaskStatusEnum.IN_PROGRESS);
    }
}
