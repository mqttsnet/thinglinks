package com.mqttsnet.thinglinks.link.api.domain.ota.vo.save;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单保存方法VO
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
@EqualsAndHashCode
@Builder
@ApiModel(value = "OtaUpgradeTasksSaveVO", description = "OTA升级任务表")
public class OtaUpgradeTasksSaveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 升级包ID，关联ota_upgrades表
     */
    @ApiModelProperty(value = "升级包ID，关联ota_upgrades表")
    @NotNull(message = "请填写升级包ID，关联ota_upgrades表")
    private Long upgradeId;
    /**
     * 任务名称
     */
    @ApiModelProperty(value = "任务名称")
    @NotEmpty(message = "请填写任务名称")
    @Size(max = 100, message = "任务名称长度不能超过{max}")
    private String taskName;
    /**
     * 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
     */
    @ApiModelProperty(value = "任务状态(0:待发布、1:进行中、2:已完成、3:已取消)")
    @NotNull(message = "请填写任务状态(0:待发布、1:进行中、2:已完成、3:已取消)")
    private Integer taskStatus;
    /**
     * 计划执行时间
     */
    @ApiModelProperty(value = "计划执行时间")
    private LocalDateTime scheduledTime;
    /**
     * 任务描述
     */
    @ApiModelProperty(value = "任务描述")
    @Size(max = 255, message = "任务描述长度不能超过{max}")
    private String description;
    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    @Size(max = 255, message = "描述长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;



}
