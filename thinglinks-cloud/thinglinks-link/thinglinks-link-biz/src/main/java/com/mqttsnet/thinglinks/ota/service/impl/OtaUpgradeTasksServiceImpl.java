package com.mqttsnet.thinglinks.ota.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.converter.Builder;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.update.DeviceUpdateVO;
import com.mqttsnet.thinglinks.ota.converter.OtaUpgradeCommandConverter;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeFileResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTargetsResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradeTasksResultDTO;
import com.mqttsnet.thinglinks.ota.dto.OtaUpgradesResultDTO;
import com.mqttsnet.thinglinks.ota.entity.OtaUpgradeRecords;
import com.mqttsnet.thinglinks.ota.entity.OtaUpgradeTasks;
import com.mqttsnet.thinglinks.ota.entity.OtaUpgrades;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageSignMethodEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageStatusEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaPackageTypeEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaTaskRecordAppConfirmStatusEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeMethodEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeScopeEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeTargetStatusEnum;
import com.mqttsnet.thinglinks.ota.enumeration.OtaUpgradeTaskStatusEnum;
import com.mqttsnet.thinglinks.ota.manager.OtaUpgradeTasksManager;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeRecordsService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTargetsService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradeTasksService;
import com.mqttsnet.thinglinks.ota.service.OtaUpgradesService;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradeRecordsPageQuery;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradeTargetsPageQuery;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradeTasksPageQuery;
import com.mqttsnet.thinglinks.ota.vo.query.OtaUpgradesPageQuery;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeRecordsResultVO;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradeTasksResultVO;
import com.mqttsnet.thinglinks.ota.vo.result.OtaUpgradesResultVO;
import com.mqttsnet.thinglinks.ota.vo.save.OtaUpgradeTargetsSaveVO;
import com.mqttsnet.thinglinks.ota.vo.save.OtaUpgradeTasksSaveVO;
import com.mqttsnet.thinglinks.ota.vo.update.OtaUpgradeTasksUpdateVO;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaCommandResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaListUpgradeableVersionsResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaPullResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReadResponseParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoOtaReportResponseParam;
import com.mqttsnet.thinglinks.utils.ota.OtaUpgradeFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 业务实现类
 * OTA升级任务表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:40:04
 * @create [2024-01-12 22:40:04] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class OtaUpgradeTasksServiceImpl extends SuperServiceImpl<OtaUpgradeTasksManager, Long, OtaUpgradeTasks> implements OtaUpgradeTasksService {

    private final OtaUpgradesService otaUpgradesService;

    private final DeviceService deviceService;

    private final OtaUpgradeRecordsService otaUpgradeRecordsService;

    private final OtaUpgradeTargetsService otaUpgradeTargetsService;

    private final OtaUpgradeFileUtils otaUpgradeFileUtils;


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

        OtaUpgrades otaUpgrade = otaUpgradesService.getById(saveVO.getUpgradeId());
        if (Objects.isNull(otaUpgrade)) {
            throw BizException.wrap("OTA upgrade package does not exist");
        }

        // Map the saveVO to your OtaUpgradeTask entity
        OtaUpgradeTasks otaUpgradeTask = buildOtaUpgradeTaskFromSaveVO(saveVO);

        // Persist the OtaUpgradeTask entity using your manager or repository
        boolean save = superManager.save(otaUpgradeTask);

        // Return the saved entity or a custom response
        if (!save) {
            throw BizException.wrap("Failed to save OTA upgrade task");
        }

        if (!OtaUpgradeScopeEnum.ALL_DEVICES.getValue().equals(saveVO.getUpgradeScope())) {
            ArgumentAssert.notEmpty(saveVO.getTargetValueList(), "Target values cannot be empty");
            List<OtaUpgradeTargetsSaveVO> otaUpgradeTargetsSaveVOList = saveVO.getTargetValueList().stream().map(targetValue -> new OtaUpgradeTargetsSaveVO()
                    .setTaskId(otaUpgradeTask.getId())
                    .setTargetValue(targetValue)
                    .setTargetStatus(OtaUpgradeTargetStatusEnum.PENDING.getValue())
                    .setCreatedOrgId(ContextUtil.getCurrentDeptId())).collect(Collectors.toList());
            otaUpgradeTargetsService.saveBatchForOtaUpgradeTargets(otaUpgradeTargetsSaveVOList);
        }

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

        // Fetch the existing task and update with new details
        OtaUpgradeTasks otaUpgradeTask = superManager.getById(updateVO.getId());

        if (Objects.isNull(otaUpgradeTask)) {
            throw BizException.wrap("OTA upgrade task not found");
        }
        Builder<OtaUpgradeTasks> otaUpgradeTasksBuilder = builderOtaUpgradeTasksUpdateVO(updateVO);
        otaUpgradeTask = otaUpgradeTasksBuilder.with(OtaUpgradeTasks::setId, updateVO.getId()).build();

        // Save the updated entity
        superManager.updateById(otaUpgradeTask);

        // Map the updated entity back to OtaUpgradeTasksUpdateVO if needed
        return BeanPlusUtil.toBeanIgnoreError(otaUpgradeTask, OtaUpgradeTasksUpdateVO.class);
    }

    /**
     * Update OTA Upgrade Task Status
     *
     * @param id     主键
     * @param status 状态
     * @return {@link Boolean} 返回结果
     */
    @Override
    public Boolean changeTaskStatus(Long id, Integer status) {
        ArgumentAssert.notNull(id, "Task ID cannot be null");
        ArgumentAssert.notNull(status, "Status cannot be null");

        OtaUpgradeTasks otaUpgradeTask = superManager.getById(id);
        if (Objects.isNull(otaUpgradeTask)) {
            throw BizException.wrap("OTA upgrade task does not exist");
        }
        OtaUpgradeTaskStatusEnum.fromValue(status)
                .orElseThrow(() -> BizException.wrap("Invalid task status"));

        otaUpgradeTask.setTaskStatus(status);
        return superManager.updateById(otaUpgradeTask);
    }

    /**
     * Delete OTA Upgrade Task
     *
     * @param id 主键
     * @return {@link Boolean} 返回结果
     */
    @Override
    public Boolean deleteOtaUpgradeTask(Long id) {
        ArgumentAssert.notNull(id, "Task ID cannot be null");

        OtaUpgradeTasks task = superManager.getById(id);
        if (Objects.isNull(task)) {
            throw new BizException("OTA upgrade task does not exist");
        }
        // 不允许删除PENDING、IN_PROGRESS、COMPLETED状态的任务
        ArgumentAssert.isTrue(OtaUpgradeTaskStatusEnum.PENDING.getValue().equals(task.getTaskStatus()), "OTA upgrade task is pending, cannot be deleted");
        ArgumentAssert.isTrue(OtaUpgradeTaskStatusEnum.IN_PROGRESS.getValue().equals(task.getTaskStatus()), "OTA upgrade task is in progress, cannot be deleted");
        ArgumentAssert.isTrue(OtaUpgradeTaskStatusEnum.COMPLETED.getValue().equals(task.getTaskStatus()), "OTA upgrade task is completed, cannot be deleted");
        // Delete associated upgrade targets
        otaUpgradeTargetsService.deleteByTaskId(id);
        return superManager.removeById(id);
    }

    /**
     * Retrieves the details of an OTA upgrade task including the associated upgrade package information.
     * The method uses Optional to handle potential null values and ensure the task and its related upgrade
     * package information are correctly retrieved and set in the result object.
     *
     * @param taskId The unique identifier of the OTA upgrade task.
     * @return {@link OtaUpgradeTasksResultVO} The detailed information about the OTA upgrade task.
     */
    @Override
    public OtaUpgradeTasksResultVO getUpgradeTaskDetails(Long taskId) {
        ArgumentAssert.notNull(taskId, "Task ID cannot be null");

        OtaUpgradeTasks otaUpgradeTask = superManager.getById(taskId);
        ArgumentAssert.notNull(otaUpgradeTask, "OTA upgrade task does not exist");

        OtaUpgradeTasksResultVO resultVO = BeanPlusUtil.toBeanIgnoreError(otaUpgradeTask, OtaUpgradeTasksResultVO.class);

        OtaUpgrades otaUpgrade = otaUpgradesService.getById(otaUpgradeTask.getUpgradeId());
        ArgumentAssert.notNull(otaUpgrade, "Associated OTA upgrade package does not exist");
        OtaUpgradesResultVO otaUpgradesResultVO = BeanPlusUtil.toBeanIgnoreError(otaUpgrade, OtaUpgradesResultVO.class);
        resultVO.setOtaUpgradesResult(otaUpgradesResultVO);

        OtaUpgradeTargetsPageQuery otaUpgradeTargetsPageQuery = new OtaUpgradeTargetsPageQuery().setTaskId(taskId);
        List<OtaUpgradeTargetsResultDTO> otaUpgradeTargetsResultDTOList = otaUpgradeTargetsService.getOtaUpgradeTargetsResultDTOList(otaUpgradeTargetsPageQuery);
        resultVO.setTargetValueList(otaUpgradeTargetsResultDTOList.stream().map(OtaUpgradeTargetsResultDTO::getTargetValue).collect(Collectors.toList()));
        return resultVO;
    }

    @Override
    public List<OtaUpgradeTasksResultDTO> getUpgradeTaskDetailsList(OtaUpgradeTasksPageQuery query) {
        List<OtaUpgradeTasks> otaUpgradeTasksList = superManager.getOtaUpgradeTasksList(query);
        List<OtaUpgradeTasksResultDTO> otaUpgradeTasksResultDTOList = BeanPlusUtil.toBeanList(otaUpgradeTasksList, OtaUpgradeTasksResultDTO.class);
        List<OtaUpgradesResultVO> otaUpgradesResultVOList = otaUpgradesService.selectListByIds(otaUpgradeTasksList.stream().map(OtaUpgradeTasks::getUpgradeId).distinct().collect(Collectors.toList()));
        Map<Long, OtaUpgradesResultVO> otaUpgradesResultVOMap = CollectionUtil.isNotEmpty(otaUpgradesResultVOList) ?
                otaUpgradesResultVOList.stream().collect(Collectors.toMap(OtaUpgradesResultVO::getId, Function.identity(), (existing, replacement) -> existing)) : Collections.emptyMap();
        otaUpgradeTasksResultDTOList.forEach(otaUpgradeTasksResultDTO -> {
            if (otaUpgradesResultVOMap.containsKey(otaUpgradeTasksResultDTO.getUpgradeId())) {
                otaUpgradeTasksResultDTO.setOtaUpgradesResult(BeanPlusUtil.toBeanIgnoreError(otaUpgradesResultVOMap.get(otaUpgradeTasksResultDTO.getUpgradeId()), OtaUpgradesResultDTO.class));
            }
        });
        return otaUpgradeTasksResultDTOList;
    }


    /**
     * Save an OTA upgrade record from MQTT events.
     *
     * @param topoOtaCommandResponseParam The message body containing the OTA command response.
     * @return {@link TopoOtaCommandResponseParam} The saved OTA upgrade record.
     */
    @Override
    public TopoOtaCommandResponseParam saveOtaUpgradeRecordByMqtt(TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        return handleAndPersistOtaUpgradeRecord(topoOtaCommandResponseParam);
    }

    /**
     * Save an OTA upgrade record from HTTP events.
     *
     * @param topoOtaCommandResponseParam The message body containing the OTA command response.
     * @return {@link TopoOtaCommandResponseParam} The saved OTA upgrade record.
     */
    @Override
    public TopoOtaCommandResponseParam saveUpgradeRecordByNorthbound(TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        return handleAndPersistOtaUpgradeRecord(topoOtaCommandResponseParam);
    }

    /**
     * 通过MQTT事件拉取OTA信息
     *
     * @param topoOtaPullParam 拉取OTA参数
     * @return {@link TopoOtaPullResponseParam} OTA信息记录
     */
    @Override
    public TopoOtaPullResponseParam otaPullByMqtt(TopoOtaPullParam topoOtaPullParam) {
        return handleOtaPull(topoOtaPullParam);
    }

    /**
     * 通过HTTP事件拉取OTA信息
     *
     * @param topoOtaPullParam 拉取OTA参数
     * @return {@link TopoOtaPullResponseParam} OTA信息记录
     */
    @Override
    public TopoOtaPullResponseParam otaPullByNorthbound(TopoOtaPullParam topoOtaPullParam) {
        return handleOtaPull(topoOtaPullParam);
    }

    @Override
    public TopoOtaReportResponseParam otaReportByMqtt(TopoOtaReportParam topoOtaReportParam) {
        return handleOtaReport(topoOtaReportParam);
    }

    @Override
    public TopoOtaReportResponseParam otaReportByNorthbound(TopoOtaReportParam topoOtaReportParam) {
        return handleOtaReport(topoOtaReportParam);
    }

    private TopoOtaReportResponseParam handleOtaReport(TopoOtaReportParam topoOtaReportParam) {
        OtaPackageTypeEnum.fromValue(topoOtaReportParam.getPackageType()).orElseThrow(() -> BizException.wrap("Invalid package type"));
        // Check if the device exists
        DeviceDetailsResultVO deviceDetailsResultVO = deviceService.findOneByDeviceIdentification(topoOtaReportParam.getDeviceIdentification());
        ArgumentAssert.notNull(deviceDetailsResultVO, "Device not found");

        // Update the device with the new version
        DeviceUpdateVO deviceUpdateVO = new DeviceUpdateVO();
        deviceUpdateVO.setId(deviceDetailsResultVO.getId());
        if (OtaPackageTypeEnum.FIRMWARE.getValue().equals(topoOtaReportParam.getPackageType())) {
            deviceUpdateVO.setFwVersion(topoOtaReportParam.getCurrentVersion());
        } else if (OtaPackageTypeEnum.SOFTWARE.getValue().equals(topoOtaReportParam.getPackageType())) {
            deviceUpdateVO.setSwVersion(topoOtaReportParam.getCurrentVersion());
        }
        deviceService.updateById(deviceUpdateVO);

        // Build OTA report response parameters
        return new TopoOtaReportResponseParam()
                .setDeviceIdentification(deviceDetailsResultVO.getDeviceIdentification())
                .setPackageType(topoOtaReportParam.getPackageType())
                .setCurrentVersion(topoOtaReportParam.getCurrentVersion());
    }

    @Override
    public void otaReadResponseByMqtt(TopoOtaReadResponseParam topoOtaReadResponseParam) {
        handleOtaResponse(topoOtaReadResponseParam);
    }

    @Override
    public void otaReadResponseByNorthbound(TopoOtaReadResponseParam topoOtaReadResponseParam) {
        handleOtaResponse(topoOtaReadResponseParam);
    }

    private void handleOtaResponse(TopoOtaReadResponseParam topoOtaReadResponseParam) {
        log.info("handle Ota  Response...request:{}", JSON.toJSONString(topoOtaReadResponseParam));
        OtaPackageTypeEnum.fromValue(topoOtaReadResponseParam.getPackageType()).orElseThrow(() -> BizException.wrap("Invalid package type"));
        // Check if the device exists
        DeviceDetailsResultVO deviceDetailsResultVO = deviceService.findOneByDeviceIdentification(topoOtaReadResponseParam.getDeviceIdentification());
        ArgumentAssert.notNull(deviceDetailsResultVO, "Device not found");

        // Update the device with the new version
        DeviceUpdateVO deviceUpdateVO = new DeviceUpdateVO();
        deviceUpdateVO.setId(deviceDetailsResultVO.getId());
        if (OtaPackageTypeEnum.FIRMWARE.getValue().equals(topoOtaReadResponseParam.getPackageType())) {
            deviceUpdateVO.setFwVersion(topoOtaReadResponseParam.getCurrentVersion());
        } else if (OtaPackageTypeEnum.SOFTWARE.getValue().equals(topoOtaReadResponseParam.getPackageType())) {
            deviceUpdateVO.setSwVersion(topoOtaReadResponseParam.getCurrentVersion());
        }
        deviceService.updateById(deviceUpdateVO);
    }


    /**
     * pull OTA upgrade task
     * 从升级记录中查找匹配的升级任务
     *
     * @param topoOtaPullParam pull OTA upgrade task param
     * @return {@link TopoOtaPullResponseParam} OTA upgrade task
     */
    private TopoOtaPullResponseParam handleOtaPull(TopoOtaPullParam topoOtaPullParam) {
        log.info("OTA pull request: {}", JSON.toJSONString(topoOtaPullParam));
        OtaPackageTypeEnum.fromValue(topoOtaPullParam.getPackageType()).orElseThrow(() -> BizException.wrap("Invalid package type"));
        // Check if the device exists
        DeviceDetailsResultVO deviceDetailsResultVO = deviceService.findOneByDeviceIdentification(topoOtaPullParam.getDeviceIdentification());

        ArgumentAssert.notNull(deviceDetailsResultVO, "Device not found");

        // 查询该设备的升级记录，根据 sourceVersion 匹配 currentVersion
        OtaUpgradeRecordsPageQuery recordsQuery = new OtaUpgradeRecordsPageQuery()
                .setDeviceIdentification(topoOtaPullParam.getDeviceIdentification())
                .setSourceVersion(topoOtaPullParam.getCurrentVersion())
                .setTargetVersion(topoOtaPullParam.getRequestVersion())
                .setAppConfirmationStatusList(Arrays.asList(
                        OtaTaskRecordAppConfirmStatusEnum.NOT_REQUIRED.getValue(),
                        OtaTaskRecordAppConfirmStatusEnum.CONFIRMED.getValue()
                ));
        List<OtaUpgradeRecordsResultVO> upgradeRecords = otaUpgradeRecordsService.getOtaUpgradeRecordsResultVOList(recordsQuery);

        if (upgradeRecords.isEmpty()) {
            throw BizException.wrap("No OTA upgrade package found for the device");
        }

        // 获取最新的升级记录（按创建时间排序）
        OtaUpgradeRecordsResultVO record = upgradeRecords.stream()
                .max(Comparator.comparing(OtaUpgradeRecordsResultVO::getCreatedTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> BizException.wrap("No OTA upgrade package found for the device"));

        // 根据升级记录中的升级包ID查询升级包信息
        OtaUpgrades otaUpgrade = otaUpgradesService.getById(record.getUpgradeId());
        if (Objects.isNull(otaUpgrade)) {
            throw BizException.wrap("OTA upgrade package not found");
        }
        OtaUpgradesResultDTO otaUpgradesResultDTO = BeanPlusUtil.toBeanIgnoreError(otaUpgrade, OtaUpgradesResultDTO.class);

        // 验证升级包的包类型是否匹配
        if (!topoOtaPullParam.getPackageType().equals(otaUpgradesResultDTO.getPackageType())) {
            throw BizException.wrap("OTA upgrade package type mismatch");
        }

        List<Long> fileIds = otaUpgradesResultDTO.getFileIds();

        Map<Long, OtaUpgradeFileResultDTO> fileInfoMap = getOtaUpgradeFileInfoMap(fileIds);

        // 获取任务详情
        OtaUpgradeTasksResultVO taskDetails = this.getUpgradeTaskDetails(record.getTaskId());
        OtaUpgradeTasksResultDTO otaUpgradeTasksResultDTO = BeanPlusUtil.toBeanIgnoreError(taskDetails, OtaUpgradeTasksResultDTO.class);

        return OtaUpgradeCommandConverter.buildOtaPullResponseParam(
                deviceDetailsResultVO.getDeviceIdentification(),
                otaUpgradeTasksResultDTO,
                otaUpgradesResultDTO,
                fileInfoMap);
    }

    /**
     * 获取OTA升级文件信息集合
     *
     * @param fileIds 文件ID列表
     * @return {@link Map<Long,OtaUpgradeFileResultDTO>} OTA升级文件信息集合
     */
    private Map<Long, OtaUpgradeFileResultDTO> getOtaUpgradeFileInfoMap(List<Long> fileIds) {
        return otaUpgradeFileUtils.getOtaUpgradeFileInfoMap(fileIds);
    }


    /**
     * Handles and persists the OTA upgrade record to the database. This method abstracts the common logic for
     * processing and saving OTA upgrade command responses, regardless of the original communication protocol (MQTT, HTTP, etc.).
     *
     * @param topoOtaCommandResponseParam The response parameters from an OTA command.
     * @return {@link TopoOtaCommandResponseParam} The persisted OTA upgrade record with any updates made during processing.
     */
    private TopoOtaCommandResponseParam handleAndPersistOtaUpgradeRecord(TopoOtaCommandResponseParam topoOtaCommandResponseParam) {

        // Check if the device exists
        DeviceDetailsResultVO deviceDetailsResultVO = deviceService.findOneByDeviceIdentification(topoOtaCommandResponseParam.getDeviceIdentification());

        ArgumentAssert.notNull(deviceDetailsResultVO, "Device not found");

        // Check if the OTA task exists
        OtaUpgradeTasksResultVO otaTask = this.getUpgradeTaskDetails(topoOtaCommandResponseParam.getOtaTaskId());

        ArgumentAssert.notNull(otaTask, "OTA upgrade task not found");

        // Update device information if necessary
        updateDeviceInfo(deviceDetailsResultVO, otaTask, topoOtaCommandResponseParam);


        LocalDateTime startTime = Optional.ofNullable(topoOtaCommandResponseParam.getStartTime())
                .map(time -> Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);

        LocalDateTime endTime = Optional.ofNullable(topoOtaCommandResponseParam.getEndTime())
                .map(time -> Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(null);

        OtaUpgradeRecordsPageQuery query = new OtaUpgradeRecordsPageQuery()
                .setTaskId(topoOtaCommandResponseParam.getOtaTaskId())
                .setDeviceIdentification(topoOtaCommandResponseParam.getDeviceIdentification());
        PageParams<OtaUpgradeRecordsPageQuery> params = new PageParams<>();
        params.setModel(query);

        Optional<OtaUpgradeRecords> existingRecordOpt = otaUpgradeRecordsService.getOtaUpgradeRecordsResultVOPage(params).getRecords().stream()
                .findFirst()
                .map(this::convertToOtaUpgradeRecordsDO);

        OtaUpgradeRecords record = existingRecordOpt.map(rec -> {
            updateOtaUpgradeRecordsDO(rec, topoOtaCommandResponseParam, startTime, endTime);

            return rec;
        }).orElseGet(() -> createNewOtaUpgradeRecordsDO(topoOtaCommandResponseParam, startTime, endTime));

        otaUpgradeRecordsService.getSuperManager().saveOrUpdate(record);

        return topoOtaCommandResponseParam;
    }


    private OtaUpgradeRecords convertToOtaUpgradeRecordsDO(OtaUpgradeRecordsResultVO otaUpgradeRecordsResultVO) {
        return BeanPlusUtil.toBeanIgnoreError(otaUpgradeRecordsResultVO, OtaUpgradeRecords.class);
    }

    // Method to update an existing DO with response params
    private void updateOtaUpgradeRecordsDO(OtaUpgradeRecords rec, TopoOtaCommandResponseParam responseParam, LocalDateTime startTime, LocalDateTime endTime) {
        rec.setUpgradeStatus(responseParam.getUpgradeStatus());
        rec.setProgress(responseParam.getProgress());
        rec.setErrorCode(responseParam.getErrorCode());
        rec.setErrorMessage(responseParam.getErrorMessage());
        rec.setStartTime(startTime);
        rec.setEndTime(endTime);
        rec.setSuccessDetails(responseParam.getSuccessDetails());
        rec.setFailureDetails(responseParam.getFailureDetails());
        rec.setLogDetails(responseParam.getLogDetails());
    }

    // Method to create a new DO from response params
    private OtaUpgradeRecords createNewOtaUpgradeRecordsDO(TopoOtaCommandResponseParam responseParam, LocalDateTime startTime, LocalDateTime endTime) {
        return OtaUpgradeRecords.builder()
                .taskId(responseParam.getOtaTaskId())
                .deviceIdentification(responseParam.getDeviceIdentification())
                .upgradeStatus(responseParam.getUpgradeStatus())
                .progress(responseParam.getProgress())
                .errorCode(responseParam.getErrorCode())
                .errorMessage(responseParam.getErrorMessage())
                .startTime(startTime)
                .endTime(endTime)
                .successDetails(responseParam.getSuccessDetails())
                .failureDetails(responseParam.getFailureDetails())
                .logDetails(responseParam.getLogDetails())
                .build();
    }


    private void updateDeviceInfo(DeviceDetailsResultVO deviceDetailsResultVO, OtaUpgradeTasksResultVO otaTask, TopoOtaCommandResponseParam topoOtaCommandResponseParam) {
        DeviceUpdateVO deviceUpdateVO = new DeviceUpdateVO();
        deviceUpdateVO.setId(deviceDetailsResultVO.getId());
        OtaUpgradesResultVO otaUpgradesResultVO = otaTask.getOtaUpgradesResult();
        if (OtaPackageTypeEnum.FIRMWARE.getValue().equals(otaUpgradesResultVO.getPackageType())) {
            deviceUpdateVO.setFwVersion(otaUpgradesResultVO.getVersion());
        } else if (OtaPackageTypeEnum.SOFTWARE.getValue().equals(otaUpgradesResultVO.getPackageType())) {
            deviceUpdateVO.setSwVersion(otaUpgradesResultVO.getVersion());
        }
        deviceService.updateById(deviceUpdateVO);
    }

    private void validateOtaUpgradeTasksSaveVO(OtaUpgradeTasksSaveVO saveVO) {
        OtaUpgradeMethodEnum.fromValue(saveVO.getUpgradeMethod())
                .orElseThrow(() -> BizException.wrap("Invalid upgrade method"));

        OtaUpgradeScopeEnum.fromValue(saveVO.getUpgradeScope())
                .orElseThrow(() -> BizException.wrap("Invalid upgrade scope"));
    }

    private OtaUpgradeTasks buildOtaUpgradeTaskFromSaveVO(OtaUpgradeTasksSaveVO saveVO) {
        saveVO.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        return BeanPlusUtil.toBeanIgnoreError(saveVO, OtaUpgradeTasks.class);
    }

    private void validateOtaUpgradeTasksUpdateVO(OtaUpgradeTasksUpdateVO updateVO) {
        OtaUpgrades otaUpgrade = otaUpgradesService.getById(updateVO.getUpgradeId());
        ArgumentAssert.notNull(otaUpgrade, "OTA upgrade can not be null");
    }

    private Builder<OtaUpgradeTasks> builderOtaUpgradeTasksUpdateVO(OtaUpgradeTasksUpdateVO updateVO) {
        return Builder.of(OtaUpgradeTasks::new)
                .with(OtaUpgradeTasks::setUpgradeId, updateVO.getUpgradeId())
                .with(OtaUpgradeTasks::setTaskName, updateVO.getTaskName())
                .with(OtaUpgradeTasks::setScheduledStartTime, updateVO.getScheduledStartTime())
                .with(OtaUpgradeTasks::setScheduledEndTime, updateVO.getScheduledEndTime())
                .with(OtaUpgradeTasks::setMaxRetryCount, updateVO.getMaxRetryCount())
                .with(OtaUpgradeTasks::setUpgradeRate, updateVO.getUpgradeRate())
                .with(OtaUpgradeTasks::setRetryIntervalMinutes, updateVO.getRetryIntervalMinutes())
                .with(OtaUpgradeTasks::setDeviceUpgradeTimeout, updateVO.getDeviceUpgradeTimeout())
                .with(OtaUpgradeTasks::setDescription, updateVO.getDescription())
                .with(OtaUpgradeTasks::setRemark, updateVO.getRemark())
                .with(OtaUpgradeTasks::setCreatedOrgId, updateVO.getCreatedOrgId());
    }

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 状态枚举
     * @return 是否更新成功
     */
    @Override
    public boolean updateTaskStatus(Long taskId, OtaUpgradeTaskStatusEnum status) {
        ArgumentAssert.notNull(taskId, "Task ID cannot be null");
        ArgumentAssert.notNull(status, "Status cannot be null");

        OtaUpgradeTasks otaUpgradeTask = superManager.getById(taskId);
        if (Objects.isNull(otaUpgradeTask)) {
            log.warn("OTA upgrade task not found - taskId: {}", taskId);
            return false;
        }

        otaUpgradeTask.setTaskStatus(status.getValue());
        boolean result = superManager.updateById(otaUpgradeTask);

        if (result) {
            log.info("Successfully updated task status - taskId: {}, status: {}", taskId, status);
        } else {
            log.error("Failed to update task status - taskId: {}, status: {}", taskId, status);
        }

        return result;
    }

    /**
     * 更新任务重试次数
     *
     * @param taskId     任务ID
     * @param retryCount 重试次数
     * @return 是否更新成功
     */
    @Override
    public boolean updateRetryCount(Long taskId, int retryCount) {
        ArgumentAssert.notNull(taskId, "Task ID cannot be null");
        ArgumentAssert.isTrue(retryCount >= 0, "Retry count cannot be negative");

        OtaUpgradeTasks otaUpgradeTask = superManager.getById(taskId);
        if (Objects.isNull(otaUpgradeTask)) {
            log.warn("OTA upgrade task not found - taskId: {}", taskId);
            return false;
        }

        otaUpgradeTask.setCurrentRetryCount(retryCount);
        boolean result = superManager.updateById(otaUpgradeTask);

        if (result) {
            log.info("Successfully updated task retry count - taskId: {}, retryCount: {}", taskId, retryCount);
        } else {
            log.error("Failed to update task retry count - taskId: {}, retryCount: {}", taskId, retryCount);
        }

        return result;
    }

    /**
     * 根据ID集合查询任务信息
     *
     * @param ids 任务ID集合
     * @return {@link List<OtaUpgradeTasksResultVO>} 任务信息列表
     */
    @Override
    public List<OtaUpgradeTasksResultVO> selectListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<OtaUpgradeTasks> tasks = superManager.listByIds(ids);
        return Optional.ofNullable(tasks)
                .map(taskList -> BeanPlusUtil.toBeanList(taskList, OtaUpgradeTasksResultVO.class))
                .orElse(Collections.emptyList());
    }

    /**
     * 北向API获取可升级版本列表
     *
     * @param deviceIdentification 设备标识
     * @param packageType          包类型
     * @return {@link TopoOtaListUpgradeableVersionsResponseParam} 可升级版本列表响应
     */
    @Override
    public TopoOtaListUpgradeableVersionsResponseParam getAvailableUpgradeVersionsByNorthbound(String deviceIdentification, Integer packageType) {
        log.info("getAvailableUpgradeVersionsByNorthbound - deviceIdentification: {}, packageType: {}", deviceIdentification, packageType);
        OtaPackageTypeEnum.fromValue(packageType).orElseThrow(() -> BizException.wrap("Invalid package type"));

        DeviceDetailsResultVO deviceDetailsResultVO = deviceService.findOneByDeviceIdentification(deviceIdentification);
        ArgumentAssert.notNull(deviceDetailsResultVO, "Device not found");

        String currentVersion = (packageType.equals(OtaPackageTypeEnum.FIRMWARE.getValue()))
                ? deviceDetailsResultVO.getFwVersion()
                : deviceDetailsResultVO.getSwVersion();

        OtaUpgradeRecordsPageQuery recordsQuery = new OtaUpgradeRecordsPageQuery()
                .setDeviceIdentification(deviceIdentification)
                .setAppConfirmationStatusList(Arrays.asList(
                        OtaTaskRecordAppConfirmStatusEnum.NOT_REQUIRED.getValue(),
                        OtaTaskRecordAppConfirmStatusEnum.CONFIRMED.getValue()
                ));
        List<OtaUpgradeRecordsResultVO> upgradeRecords = otaUpgradeRecordsService.getOtaUpgradeRecordsResultVOList(recordsQuery);

        List<Long> upgradeIds = upgradeRecords.stream()
                .map(OtaUpgradeRecordsResultVO::getUpgradeId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> taskIds = upgradeRecords.stream()
                .map(OtaUpgradeRecordsResultVO::getTaskId)
                .distinct()
                .collect(Collectors.toList());

        OtaUpgradesPageQuery upgradesQuery = new OtaUpgradesPageQuery()
                .setIds(upgradeIds)
                .setStatus(OtaPackageStatusEnum.ENABLE.getValue());
        List<OtaUpgradesResultDTO> otaUpgradesResultDTOList = otaUpgradesService.getOtaUpgradesResultDTOList(upgradesQuery);

        List<OtaUpgradeTasksResultVO> tasksResultVOList = selectListByIds(taskIds);

        Map<Long, OtaUpgradesResultDTO> otaUpgradesResultDTOMap = otaUpgradesResultDTOList.stream()
                .collect(Collectors.toMap(OtaUpgradesResultDTO::getId, v -> v));

        Map<Long, OtaUpgradeTasksResultVO> otaUpgradeTasksResultVOMap = tasksResultVOList.stream()
                .collect(Collectors.toMap(OtaUpgradeTasksResultVO::getId, v -> v));

        List<TopoOtaListUpgradeableVersionsResponseParam.UpgradeVersionInfo> upgradeVersionsList = upgradeRecords.stream()
                .sorted(Comparator.comparing(OtaUpgradeRecordsResultVO::getCreatedTime, Comparator.nullsFirst(Comparator.reverseOrder())))
                .map(record -> buildUpgradeVersionInfo(record, packageType, otaUpgradesResultDTOMap, otaUpgradeTasksResultVOMap))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return TopoOtaListUpgradeableVersionsResponseParam.builder()
                .deviceIdentification(deviceDetailsResultVO.getDeviceIdentification())
                .productIdentification(deviceDetailsResultVO.getProductIdentification())
                .packageType(packageType)
                .currentVersion(currentVersion)
                .upgradeVersions(upgradeVersionsList)
                .build();
    }

    /**
     * 构建可升级版本信息
     *
     * @param record                     升级记录
     * @param packageType                包类型
     * @param otaUpgradeMap              升级包Map
     * @param otaUpgradeTasksResultVOMap 任务 Map
     * @return 版本信息的Optional，如果构建失败则返回Optional.empty()
     */
    private Optional<TopoOtaListUpgradeableVersionsResponseParam.UpgradeVersionInfo> buildUpgradeVersionInfo(
            OtaUpgradeRecordsResultVO record,
            Integer packageType,
            Map<Long, OtaUpgradesResultDTO> otaUpgradeMap,
            Map<Long, OtaUpgradeTasksResultVO> otaUpgradeTasksResultVOMap) {
        try {
            OtaUpgradesResultDTO otaUpgradesResultDTO = otaUpgradeMap.get(record.getUpgradeId());
            if (Objects.isNull(otaUpgradesResultDTO)) {
                log.warn("OTA upgrade package not found for record: {}", record.getId());
                return Optional.empty();
            }

            if (!packageType.equals(otaUpgradesResultDTO.getPackageType())) {
                return Optional.empty();
            }
            OtaUpgradeTasksResultVO otaUpgradeTasksResultVO = otaUpgradeTasksResultVOMap.get(record.getTaskId());
            if (otaUpgradeTasksResultVO == null) {
                log.warn("OTA upgrade task not found for record: {}", record.getId());
                return Optional.empty();
            }

            String sign = extractFileSign(otaUpgradesResultDTO);

            return Optional.of(TopoOtaListUpgradeableVersionsResponseParam.UpgradeVersionInfo.builder()
                    .otaTaskId(otaUpgradeTasksResultVO.getId())
                    .otaTaskName(otaUpgradeTasksResultVO.getTaskName())
                    .packageName(otaUpgradesResultDTO.getPackageName())
                    .version(otaUpgradesResultDTO.getVersion())
                    .fileLocation(otaUpgradesResultDTO.getFileLocation())
                    .description(otaUpgradesResultDTO.getDescription())
                    .customInfo(otaUpgradesResultDTO.getCustomInfo())
                    .signMethod(otaUpgradesResultDTO.getSignMethod())
                    .sign(sign)
                    .build());
        } catch (Exception e) {
            log.error("Failed to build upgrade version info for record: {}", record.getId(), e);
            return Optional.empty();
        }
    }

    /**
     * 提取文件签名信息
     *
     * @param otaUpgradesResultDTO OTA升级包DTO
     * @return 签名字符串，如果无法获取则返回空字符串
     */
    private String extractFileSign(OtaUpgradesResultDTO otaUpgradesResultDTO) {
        if (otaUpgradesResultDTO.getSignMethod() == null) {
            return StrPool.EMPTY;
        }

        List<Long> fileIds = otaUpgradesResultDTO.getFileIds();

        Map<Long, OtaUpgradeFileResultDTO> fileInfoMap = getOtaUpgradeFileInfoMap(fileIds);
        if (fileInfoMap.isEmpty()) {
            return StrPool.EMPTY;
        }

        OtaUpgradeFileResultDTO fileInfo = fileInfoMap.values().iterator().next();
        return OtaPackageSignMethodEnum.fromValue(otaUpgradesResultDTO.getSignMethod())
                .flatMap(fileInfo::getFileSign)
                .orElse(StrPool.EMPTY);
    }
}