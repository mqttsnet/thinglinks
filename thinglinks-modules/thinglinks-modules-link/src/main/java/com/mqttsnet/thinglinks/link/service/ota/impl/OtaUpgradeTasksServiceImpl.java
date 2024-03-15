package com.mqttsnet.thinglinks.link.service.ota.impl;

import com.mqttsnet.thinglinks.common.core.exception.ServiceException;
import com.mqttsnet.thinglinks.common.core.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.common.core.utils.bean.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.security.service.TokenService;
import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgradeTasks;
import com.mqttsnet.thinglinks.link.api.domain.ota.entity.OtaUpgrades;
import com.mqttsnet.thinglinks.link.api.domain.ota.enumeration.OtaPackageStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.link.api.domain.ota.enumeration.OtaUpgradeTaskStatusEnum;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.result.OtaUpgradeTasksResultVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.result.OtaUpgradesResultVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradeTasksSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradeTasksUpdateVO;
import com.mqttsnet.thinglinks.link.mapper.ota.OtaUpgradeTasksMapper;
import com.mqttsnet.thinglinks.link.mapper.ota.OtaUpgradesMapper;
import com.mqttsnet.thinglinks.link.service.ota.OtaUpgradeTasksService;
import com.mqttsnet.thinglinks.system.api.domain.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OtaUpgradeTasksServiceImpl implements OtaUpgradeTasksService {

    @Resource
    private OtaUpgradeTasksMapper otaUpgradeTasksMapper;

    @Resource
    private OtaUpgradesMapper otaUpgradesMapper;

    @Resource
    private TokenService tokenService;

    /**
     * Save OTA Upgrade Task
     *
     * @param saveVO 保存参数
     * @return {@link OtaUpgradeTasksSaveVO} 返回结果
     */
    @Override
    public OtaUpgradeTasksSaveVO saveUpgradeTask(OtaUpgradeTasksSaveVO saveVO) {
        log.info("saveUpgradeTask saveVO: {}", saveVO);
        validateOtaUpgradeTasksSaveVO(saveVO);
        // Map the saveVO to your OtaUpgradeTask entity
        OtaUpgradeTasks otaUpgradeTask = buildOtaUpgradeTaskFromSaveVO(saveVO);

        // Persist the OtaUpgradeTask entity using your manager or repository
        otaUpgradeTasksMapper.insertOtaUpgradeTask(otaUpgradeTask);

        // Map the saved entity back to OtaUpgradeTasksSaveVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgradeTask, OtaUpgradeTasksSaveVO.class);
    }

    /**
     * Update OTA Upgrade Task
     *
     * @param updateVO 更新参数
     * @return {@link OtaUpgradeTasksUpdateVO} 返回结果
     */
    @Override
    public OtaUpgradeTasksUpdateVO updateUpgradeTask(OtaUpgradeTasksUpdateVO updateVO) {
        log.info("Updating OTA upgrade task: {}", updateVO);

        // Validate the updateVO object
        validateOtaUpgradeTasksUpdateVO(updateVO);

        // Map the saveVO to your OtaUpgrade entity
        OtaUpgradeTasks otaUpgradeTasks = buildOtaUpgradeTaskFromSaveVO(updateVO);

        // Save the updated entity
        otaUpgradeTasksMapper.updateOtaUpgradeTaskById(otaUpgradeTasks);


        // Map the updated entity back to OtaUpgradeTasksUpdateVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgradeTasks, OtaUpgradeTasksUpdateVO.class);
    }

    @Override
    public boolean changeTaskStatus(Long id, Integer status) {
        ArgumentAssert.notNull(id, "Package ID cannot be null");
        ArgumentAssert.notNull(status, "Status cannot be null");

        OtaUpgradeTasks otaUpgradeTasks = otaUpgradeTasksMapper.selectOtaUpgradeTaskById(id);
        if (otaUpgradeTasks == null) {
            throw new ServiceException("OTA upgrade package does not exist");
        }
        if (status == otaUpgradeTasks.getTaskStatus().intValue()) {
            throw new ServiceException("The OTA upgrade package status is the same as the current status");
        }
        OtaUpgradeTaskStatusEnum.fromValue(status)
                .orElseThrow(() -> new ServiceException("Invalid task status"));

        return otaUpgradeTasksMapper.updateOtaUpgradeTasksByStatus(id, status) > 0;
    }

    @Override
    public boolean deleteOtaUpgradeTask(Long id) {
        ArgumentAssert.notNull(id, "Task ID cannot be null");

        OtaUpgradeTasks task = otaUpgradeTasksMapper.selectOtaUpgradeTaskById(id);
        if (task == null) {
            throw new ServiceException("OTA upgrade task does not exist");
        }

        // Additional checks can be added here, like if the task is in progress or has dependencies

        return otaUpgradeTasksMapper.deleteOtaUpgradeTaskById(id) > 0;
    }

    @Override
    public OtaUpgradeTasksResultVO getUpgradeTaskDetails(Long id) {
        ArgumentAssert.notNull(id, "Task ID cannot be null");

        OtaUpgradeTasks otaUpgradeTask = otaUpgradeTasksMapper.selectOtaUpgradeTaskById(id);
        if (otaUpgradeTask == null) {
            throw new ServiceException("OTA upgrade task not found");
        }

        OtaUpgradeTasksResultVO resultVO = BeanPlusUtil.toBeanIgnoreError(otaUpgradeTask, OtaUpgradeTasksResultVO.class);

        OtaUpgrades otaUpgrade = otaUpgradesMapper.selectOtaUpgradeById(otaUpgradeTask.getUpgradeId());
        if (otaUpgrade == null) {
            throw new ServiceException("Associated OTA upgrade package not found");
        }

        OtaUpgradesResultVO otaUpgradesResultVO = BeanPlusUtil.toBeanIgnoreError(otaUpgrade, OtaUpgradesResultVO.class);
        resultVO.setOtaUpgradesResultVO(otaUpgradesResultVO);
        return resultVO;
    }

    @Override
    public List<OtaUpgradeTasksResultVO> otaUpgradeTasksExecute(LocalDateTime startTime, LocalDateTime endTime) {
        //TODO 此方法 待实现
        return null;
    }


    private void validateOtaUpgradeTasksUpdateVO(OtaUpgradeTasksUpdateVO updateVO) {
        Optional.ofNullable(otaUpgradeTasksMapper.selectOtaUpgradeTaskById(updateVO.getId())).orElseThrow(() -> new ServiceException("OTA upgrade task not found"));
        //TODO 如需其他限制 在此添加
    }


    private void validateOtaUpgradeTasksSaveVO(OtaUpgradeTasksSaveVO saveVO) {
        OtaUpgradeTaskStatusEnum.fromValue(saveVO.getTaskStatus())
                .orElseThrow(() -> new ServiceException("Invalid task status"));

        OtaUpgrades otaUpgrades = otaUpgradesMapper.selectOtaUpgradeById(saveVO.getUpgradeId());
        if (otaUpgrades == null) {
            throw new ServiceException("OTA upgrade package does not exist");
        }

    }

    private <T> OtaUpgradeTasks buildOtaUpgradeTaskFromSaveVO(T vo) {
        SysUser sysUser = tokenService.getLoginUser().getSysUser();
        OtaUpgradeTasks otaUpgradeTasks = BeanPlusUtil.toBeanIgnoreError(vo, OtaUpgradeTasks.class);
        otaUpgradeTasks.setCreatedBy(sysUser.getUserName());
        otaUpgradeTasks.setUpdatedBy(sysUser.getUserName());
        return otaUpgradeTasks;
    }
}
