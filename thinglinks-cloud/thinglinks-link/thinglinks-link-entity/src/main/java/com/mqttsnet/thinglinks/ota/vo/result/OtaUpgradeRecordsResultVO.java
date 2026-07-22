package com.mqttsnet.thinglinks.ota.vo.result;

import java.io.Serial;
import java.time.LocalDateTime;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 表单查询方法返回值VO
 * OTA升级记录表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:42:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "OtaUpgradeRecordsResultVO", description = "OTA升级记录表")
public class OtaUpgradeRecordsResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 升级包ID，关联ota_upgrades表
     */
    @Schema(description = "升级包ID，关联ota_upgrades表")
    private Long upgradeId;

    /**
     * 任务ID，关联ota_upgrade_tasks表
     */
    @Schema(description = "任务ID，关联ota_upgrade_tasks表")
    private Long taskId;
    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;
    /**
     * 升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)
     */
    @Schema(description = "升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_TASK_RECORD_STATUS)
    private Integer upgradeStatus;

    /**
     * 待升级的源版本号
     */
    @Schema(description = "待升级的源版本号")
    private String sourceVersion;


    /**
     * 目标版本号
     */
    @Schema(description = "目标版本号")
    private String targetVersion;


    /**
     * APP确认状态
     */
    @Schema(description = "APP确认状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_TASK_RECORD_APP_CONFIRM_STATUS)
    private Integer appConfirmationStatus;


    /**
     * APP确认时间
     */
    @Schema(description = "APP确认时间")
    private LocalDateTime appConfirmationTime;

    /**
     * 指令下发状态
     */
    @Schema(description = "指令下发状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_TASK_RECORD_COMMAND_SEND_STATUS)
    private Integer commandSendStatus;


    /**
     * 最新指令下发时间
     */
    @Schema(description = "最新指令下发时间")
    private LocalDateTime lastCommandSendTime;


    /**
     * OTA指令内容
     */
    @Schema(description = "OTA指令内容")
    private String commandContent;


    /**
     * 升级进度（百分比）
     */
    @Schema(description = "升级进度（百分比）")
    private Integer progress;
    /**
     * 错误代码
     */
    @Schema(description = "错误代码")
    private String errorCode;
    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;
    /**
     * 升级开始时间
     */
    @Schema(description = "升级开始时间")
    private LocalDateTime startTime;
    /**
     * 升级结束时间
     */
    @Schema(description = "升级结束时间")
    private LocalDateTime endTime;
    /**
     * 升级成功详细信息
     */
    @Schema(description = "升级成功详细信息")
    private String successDetails;
    /**
     * 升级失败详细信息
     */
    @Schema(description = "升级失败详细信息")
    private String failureDetails;
    /**
     * 升级过程日志
     */
    @Schema(description = "升级过程日志")
    private String logDetails;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;

    /**
     * 关联的升级任务信息
     */
    @Schema(description = "关联的升级任务信息")
    private OtaUpgradeTasksResultVO otaUpgradeTasksResult;

    /**
     * 关联的升级包信息
     */
    @Schema(description = "关联的升级包信息")
    private OtaUpgradesResultVO otaUpgradesResult;


}