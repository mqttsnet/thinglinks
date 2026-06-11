package com.mqttsnet.thinglinks.video.vo.update.record;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;


/**
 * <p>
 * 表单修改方法VO
 * 录制计划表
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-01 00:00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(description = "录制计划表")
public class VideoRecordPlanUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "请填写id", groups = SuperEntity.Update.class)
    private Long id;

    /**
     * 计划名称
     */
    @Schema(description = "计划名称")
    private String planName;
    /**
     * 计划类型
     */
    @Schema(description = "计划类型")
    private Integer planType;
    /**
     * 媒体服务标识
     */
    @Schema(description = "媒体服务标识")
    private String mediaIdentification;
    /**
     * 录制格式
     */
    @Schema(description = "录制格式")
    private String recordFormat;
    /**
     * 分段时长(秒)
     */
    @Schema(description = "分段时长(秒)")
    private Integer segmentDuration;
    /**
     * 保留天数
     */
    @Schema(description = "保留天数")
    private Integer retentionDays;
    /**
     * 存储路径
     */
    @Schema(description = "存储路径")
    private String storagePath;
    /**
     * 计划状态
     */
    @Schema(description = "计划状态")
    private Integer planStatus;
    /**
     * 调度规则(JSON)
     */
    @Schema(description = "调度规则(JSON)")
    private String scheduleRule;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 创建组织
     */
    @Schema(description = "创建组织")
    private Long createdOrgId;


}
