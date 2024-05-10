package com.mqttsnet.thinglinks.link.api.domain.ota.vo.result;

import cn.hutool.core.map.MapUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

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
@Builder
@ApiModel(value = "OtaUpgradeTasksResultVO", description = "OTA升级任务表")
public class OtaUpgradeTasksResultVO  implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> echoMap = MapUtil.newHashMap();

    @ApiModelProperty(value = "主键")
    private Long id;

    /**
    * 升级包ID，关联ota_upgrades表
    */
    @ApiModelProperty(value = "升级包ID，关联ota_upgrades表")
    private Long upgradeId;
    /**
    * 任务名称
    */
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    /**
    * 任务状态(0:待发布、1:进行中、2:已完成、3:已取消)
    */
    @ApiModelProperty(value = "任务状态(0:待发布、1:进行中、2:已完成、3:已取消)")
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
    private String description;
    /**
    * 描述
    */
    @ApiModelProperty(value = "描述")
    private String remark;
    /**
    * 创建人组织
    */
    @ApiModelProperty(value = "创建人组织")
    private Long createdOrgId;


    /**
     * 升级包信息
     */
    @ApiModelProperty(value = "升级包信息")
    private OtaUpgradesResultVO otaUpgradesResultVO;

}
