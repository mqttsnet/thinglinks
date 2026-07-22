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
import java.time.LocalDateTime;


/**
 * <p>
 * 表单查询条件VO
 * 录制文件表
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
@Schema(description = "录制文件表")
public class VideoRecordFilePageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 计划ID
     */
    @Schema(description = "计划ID")
    private Long planId;
    /**
     * 设备唯一标识
     */
    @Schema(description = "设备唯一标识")
    private String deviceIdentification;
    /**
     * 通道唯一标识
     */
    @Schema(description = "通道唯一标识")
    private String channelIdentification;
    /**
     * 流唯一标识
     */
    @Schema(description = "流唯一标识")
    private String streamIdentification;
    /**
     * 应用名
     */
    @Schema(description = "应用名")
    private String app;
    /**
     * 媒体服务标识
     */
    @Schema(description = "媒体服务标识")
    private String mediaIdentification;
    /**
     * 文件名
     */
    @Schema(description = "文件名")
    private String fileName;
    /**
     * 文件格式
     */
    @Schema(description = "文件格式")
    private String fileFormat;
    /**
     * 文件状态
     */
    @Schema(description = "文件状态")
    private Integer fileStatus;
    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    /**
     * 创建组织
     */
    @Schema(description = "创建组织")
    private Long createdOrgId;


}
