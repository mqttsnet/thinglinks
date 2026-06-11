package com.mqttsnet.thinglinks.ota.vo.result;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

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
 * OTA升级任务表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:40:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "OtaUpgradeTasksResultVO", description = "OTA升级任务表")
public class OtaUpgradeTasksResultVO extends AuditableResultVO {

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
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String taskName;

    /**
     * 升级模式
     */
    @Schema(description = "升级模式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_UPGRADE_METHOD)
    private Integer upgradeMethod;
    /**
     * 升级范围
     */
    @Schema(description = "升级范围")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_UPGRADE_SCOPE)
    private Integer upgradeScope;

    /**
     * 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
     */
    @Schema(description = "任务状态(0:待发布、1:进行中、2:已完成、3:已取消)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_OTA_TASK_STATUS)
    private Integer taskStatus;
    /**
     * 计划执行开始时间
     */
    @Schema(description = "计划执行开始时间")
    private LocalDateTime scheduledStartTime;
    /**
     * 计划执行结束时间
     */
    @Schema(description = "计划执行结束时间")
    private LocalDateTime scheduledEndTime;

    /**
     * 最大重试次数
     */
    @Schema(description = "最大重试次数")
    private Integer maxRetryCount;
    /**
     * 当前重试次数
     */
    @Schema(description = "当前重试次数")
    private Integer currentRetryCount;

    /**
     * 待升级的源版本号
     */
    @Schema(description = "待升级的源版本号")
    private String sourceVersions;


    /**
     * APP确认升级
     */
    @Schema(description = "APP确认升级")
    private Boolean appConfirmationRequired;


     /**
     * 升级速率(恒定速率升级，10-1000)
     */
    @Schema(description = "升级速率(恒定速率升级，10-1000)")
    private Integer upgradeRate;

    /**
     * 重试间隔分钟数(默认为10分钟)
     */
    @Schema(description = "重试间隔分钟数(默认为10分钟)")
    private Integer retryIntervalMinutes;

    /**
     * 设备升级超时时间(分钟)
     */
    @Schema(description = "设备升级超时时间(分钟)")
    private Integer deviceUpgradeTimeout;


    /**
     * 最新重试时间
     */
    @Schema(description = "最新重试时间")
    private LocalDateTime lastRetryTime;


    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String description;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;


    /**
     * 升级包信息
     */
    @Schema(description = "升级包信息")
    private OtaUpgradesResultVO otaUpgradesResult;

    /**
     * 升级目标值列表
     */
    @Schema(description = "升级目标值列表")
    private List<String> targetValueList;

}