package com.mqttsnet.thinglinks.video.vo.query.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 表单查询条件VO
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
public class VideoRecordPlanPageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
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
     * 计划状态
     */
    @Schema(description = "计划状态")
    private Integer planStatus;
    /**
     * 创建组织
     */
    @Schema(description = "创建组织")
    private Long createdOrgId;


}
