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
@EqualsAndHashCode
@Builder
@ApiModel(value = "OtaUpgradeRecordsSaveVO", description = "OTA升级记录表")
public class OtaUpgradeRecordsSaveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID，关联ota_upgrade_tasks表
     */
    @ApiModelProperty(value = "任务ID，关联ota_upgrade_tasks表")
    @NotNull(message = "请填写任务ID，关联ota_upgrade_tasks表")
    private Long taskId;
    /**
     * 设备标识
     */
    @ApiModelProperty(value = "设备标识")
    @NotEmpty(message = "请填写设备标识")
    @Size(max = 100, message = "设备标识长度不能超过{max}")
    private String deviceIdentification;
    /**
     * 升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)
     */
    @ApiModelProperty(value = "升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)")
    @NotNull(message = "请填写升级状态(0:待升级、1:升级中、2:升级成功、3:升级失败)")
    private Integer upgradeStatus;
    /**
     * 升级进度（百分比）
     */
    @ApiModelProperty(value = "升级进度（百分比）")
    @NotNull(message = "请填写升级进度（百分比）")
    private Integer progress;
    /**
     * 错误代码
     */
    @ApiModelProperty(value = "错误代码")
    @Size(max = 100, message = "错误代码长度不能超过{max}")
    private String errorCode;
    /**
     * 错误信息
     */
    @ApiModelProperty(value = "错误信息")
    @Size(max = 255, message = "错误信息长度不能超过{max}")
    private String errorMessage;
    /**
     * 升级开始时间
     */
    @ApiModelProperty(value = "升级开始时间")
    private LocalDateTime startTime;
    /**
     * 升级结束时间
     */
    @ApiModelProperty(value = "升级结束时间")
    private LocalDateTime endTime;
    /**
     * 升级成功详细信息
     */
    @ApiModelProperty(value = "升级成功详细信息")
    @Size(max = 2147483647, message = "升级成功详细信息长度不能超过{max}")
    private String successDetails;
    /**
     * 升级失败详细信息
     */
    @ApiModelProperty(value = "升级失败详细信息")
    @Size(max = 2147483647, message = "升级失败详细信息长度不能超过{max}")
    private String failureDetails;
    /**
     * 升级过程日志
     */
    @ApiModelProperty(value = "升级过程日志")
    @Size(max = 2147483647, message = "升级过程日志长度不能超过{max}")
    private String logDetails;
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
