package com.mqttsnet.thinglinks.link.api.domain.ota.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OTA升级记录模型
 */
@ApiModel(value = "OTA升级记录模型")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
public class OtaUpgradeRecords implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "任务ID，关联ota_upgrade_tasks表")
    private Long taskId;

    @ApiModelProperty(value = "设备标识")
    private String deviceIdentification;

    @ApiModelProperty(value = "升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)")
    private Short upgradeStatus;

    @ApiModelProperty(value = "升级进度（百分比）")
    private Short progress;

    @ApiModelProperty(value = "错误代码")
    private String errorCode;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "升级开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "升级结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "升级成功详细信息")
    private String successDetails;

    @ApiModelProperty(value = "升级失败详细信息")
    private String failureDetails;

    @ApiModelProperty(value = "升级过程日志")
    private String logDetails;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty(value = "记录创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @ApiModelProperty(value = "更新人")
    private String updatedBy;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedTime;
}
