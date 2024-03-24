package com.mqttsnet.thinglinks.link.service.ota;

import com.mqttsnet.thinglinks.common.core.exception.ArgumentException;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.result.OtaUpgradeTasksResultVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradeTasksSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradeTasksUpdateVO;

import java.time.LocalDateTime;
import java.util.List;

public interface OtaUpgradeTasksService {

    /**
     * Save OTA Upgrade Task
     *
     * @param saveVO 保存参数
     * @return {@link OtaUpgradeTasksSaveVO} 返回结果
     */
    OtaUpgradeTasksSaveVO saveUpgradeTask(OtaUpgradeTasksSaveVO saveVO);

    /**
     * Update OTA Upgrade Task
     *
     * @param updateVO 更新参数
     * @return {@link OtaUpgradeTasksUpdateVO} 返回结果
     */
    OtaUpgradeTasksUpdateVO updateUpgradeTask(OtaUpgradeTasksUpdateVO updateVO);

    /**
     * Update OTA Upgrade Task Status
     *
     * @param id     主键
     * @param status 状态
     * @return {@link Boolean} 返回结果
     */
    boolean changeTaskStatus(Long id, Integer status) throws ArgumentException;

    /**
     * Delete OTA Upgrade Task
     *
     * @param id 主键
     * @return {@link Boolean} 返回结果
     */
    boolean deleteOtaUpgradeTask(Long id) throws ArgumentException;

    /**
     * Get OTA Upgrade Task Details
     *
     * @param id 主键
     * @return {@link OtaUpgradeTasksResultVO} 返回结果
     */
    OtaUpgradeTasksResultVO getUpgradeTaskDetails(Long id) throws ArgumentException;

    /**
     * Perform ota upgrade tasks based on the start time and end time, including upgrade package information.
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return {@link OtaUpgradeTasksResultVO} OTA升级任务的详细信息。
     */
    List<OtaUpgradeTasksResultVO> otaUpgradeTasksExecute(LocalDateTime startTime, LocalDateTime endTime);
}
