package com.mqttsnet.thinglinks.ota.service.statemachine.context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeRecordsResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeMethodEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeScopeEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeTaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * OTA升级上下文
 * 用于在状态机流转过程中传递数据
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/11/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
public class OtaUpgradeContext {

    /**
     * 租户ID
     */
    private Long tenantId;


    /**
     * 升级任务
     */
    private OtaUpgradeTasksResultDTO upgradeTask;

    /**
     * 升级记录
     */
    private OtaUpgradeRecordsResultDTO upgradeRecord;

    /**
     * 指定的设备标识列表（用于手动重试指定设备）
     */
    private List<String> specifiedDeviceIdentifications;

    /**
     * 设备错误信息映射
     * key: 设备标识
     * value: 错误信息
     */
    private Map<String, String> deviceErrors = new HashMap<>();
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;


    /**
     * 获取任务ID
     */
    public Long getTaskId() {
        return Objects.nonNull(upgradeTask) ? upgradeTask.getId() : null;
    }


    /**
     * 获取资源包ID
     */
    public Long getUpgradeTaskId() {
        return Objects.nonNull(upgradeTask) ? upgradeTask.getUpgradeId() : null;
    }

    /**
     * 获取任务名称
     */
    public String getTaskName() {
        return Objects.nonNull(upgradeTask) ? upgradeTask.getTaskName() : StrPool.EMPTY;
    }

    /**
     * 获取目标版本
     */
    public String getTargetVersion() {
        if (Objects.isNull(upgradeTask) || Objects.isNull(upgradeTask.getOtaUpgradesResult())
                || Objects.isNull(upgradeTask.getOtaUpgradesResult().getVersion())) {
            return StrPool.EMPTY;
        }
        return upgradeTask.getOtaUpgradesResult().getVersion();
    }


    /**
     * 获取升级方法
     */
    public OtaUpgradeMethodEnum getUpgradeMethod() {
        if (Objects.isNull(upgradeTask)) {
            throw new IllegalArgumentException("upgradeTask is null");
        }
        return OtaUpgradeMethodEnum.fromValue(upgradeTask.getUpgradeMethod())
                .orElse(OtaUpgradeMethodEnum.STATIC);
    }

    /**
     * 获取升级范围
     */
    public OtaUpgradeScopeEnum getUpgradeScope() {
        if (Objects.isNull(upgradeTask)) {
            throw new IllegalArgumentException("upgradeTask is null");
        }
        return OtaUpgradeScopeEnum.fromValue(upgradeTask.getUpgradeScope())
                .orElse(OtaUpgradeScopeEnum.ALL_DEVICES);
    }

    /**
     * 获取当前状态
     */
    public OtaUpgradeTaskStatusEnum getCurrentStatus() {
        if (Objects.isNull(upgradeTask) || Objects.isNull(upgradeTask.getTaskStatus())) {
            throw new IllegalArgumentException("upgradeTask or taskStatus is null");
        }
        return OtaUpgradeTaskStatusEnum.fromValue(upgradeTask.getTaskStatus()).orElse(OtaUpgradeTaskStatusEnum.PENDING);
    }

    /**
     * 设置当前状态
     */
    public void setCurrentStatus(OtaUpgradeTaskStatusEnum currentStatus) {
        if (Objects.nonNull(upgradeTask)) {
            upgradeTask.setTaskStatus(currentStatus.getValue());
        }
    }

    /**
     * 获取当前重试次数
     */
    public Integer getRetryCount() {
        return Objects.nonNull(upgradeTask) && Objects.nonNull(upgradeTask.getCurrentRetryCount())
                ? upgradeTask.getCurrentRetryCount() : 0;
    }

    /**
     * 获取最大重试次数
     */
    public Integer getMaxRetryCount() {
        return Objects.nonNull(upgradeTask) && Objects.nonNull(upgradeTask.getMaxRetryCount())
                ? upgradeTask.getMaxRetryCount() : 3;
    }

    /**
     * 获取设备升级超时时间
     */
    public Integer getDeviceUpgradeTimeout() {
        return Objects.nonNull(upgradeTask) ? upgradeTask.getDeviceUpgradeTimeout() : null;
    }

    /**
     * 获取计划结束时间
     */
    public LocalDateTime getScheduledEndTime() {
        return Objects.nonNull(upgradeTask) ? upgradeTask.getScheduledEndTime() : null;
    }

    /**
     * 检查是否达到最大重试次数
     */
    public boolean isMaxRetryReached() {
        Integer currentRetry = getRetryCount();
        Integer maxRetry = getMaxRetryCount();
        return Objects.nonNull(maxRetry) && currentRetry >= maxRetry;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        if (Objects.nonNull(upgradeTask)) {
            Integer currentRetry = getRetryCount();
            upgradeTask.setCurrentRetryCount(Objects.nonNull(currentRetry) ? currentRetry + 1 : 1);
        }
    }

    /**
     * 检查任务是否超时（仅检查任务级别的超时）
     *
     * <p>注意：此方法仅检查任务级别的超时（计划结束时间），不检查设备级别的升级超时。</p>
     * <p>设备级别的升级超时应该在处理单个升级记录时单独检查。</p>
     *
     * @return 如果超过计划结束时间，返回 true
     */
    public boolean isTimeout() {
        // 仅检查是否超过计划结束时间
        return isScheduledEndTimeReached();
    }

    /**
     * 检查是否达到计划结束时间
     *
     * @return 如果当前时间已超过计划结束时间，返回 true
     */
    public boolean isScheduledEndTimeReached() {
        LocalDateTime scheduledEndTime = getScheduledEndTime();
        if (Objects.isNull(scheduledEndTime)) {
            return false;
        }
        return LocalDateTime.now().isAfter(scheduledEndTime);
    }

    /**
     * 检查是否有错误
     */
    public boolean hasError() {
        return Objects.nonNull(deviceErrors) && !deviceErrors.isEmpty();
    }

    /**
     * 添加设备错误信息
     */
    public void addDeviceError(String deviceIdentification, String errorMessage) {
        if (Objects.isNull(deviceErrors)) {
            deviceErrors = new HashMap<>();
        }
        deviceErrors.put(deviceIdentification, errorMessage);
    }

    /**
     * 获取设备错误信息
     */
    public String getDeviceError(String deviceIdentification) {
        return Objects.nonNull(deviceErrors) ? deviceErrors.get(deviceIdentification) : null;
    }

    /**
     * 获取所有设备错误信息
     */
    public Map<String, String> getAllDeviceErrors() {
        return Objects.nonNull(deviceErrors) ? new HashMap<>(deviceErrors) : new HashMap<>();
    }

    /**
     * 获取错误设备数量
     */
    public int getErrorDeviceCount() {
        return Objects.nonNull(deviceErrors) ? deviceErrors.size() : 0;
    }


    /**
     * 清除错误信息
     */
    public void clearError() {
        if (Objects.nonNull(deviceErrors)) {
            deviceErrors.clear();
        }
    }

    /**
     * 清除特定设备的错误信息
     */
    public void clearDeviceError(String deviceIdentification) {
        if (Objects.nonNull(deviceErrors)) {
            deviceErrors.remove(deviceIdentification);
        }
    }
}
