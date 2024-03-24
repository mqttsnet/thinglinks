package com.mqttsnet.thinglinks.link.api.domain.ota.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OTA升级任务模型
 */
@ApiModel(value = "OTA升级任务模型")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Builder
public class OtaUpgradeTasks implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "升级包ID，关联ota_upgrades表")
    private Long upgradeId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务状态(0:待发布、1:进行中、2:已完成、3:已取消)")
    private Short taskStatus;

    @ApiModelProperty(value = "计划执行时间")
    private LocalDateTime scheduledTime;

    @ApiModelProperty(value = "任务描述")
    private String description;

    @ApiModelProperty(value = "描述")
    private String remark;

    @ApiModelProperty(value = "创建人")
    private String createdBy;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "更新人")
    private String updatedBy;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updatedTime;
}
