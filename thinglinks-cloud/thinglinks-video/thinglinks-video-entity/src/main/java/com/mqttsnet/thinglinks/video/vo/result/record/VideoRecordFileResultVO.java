package com.mqttsnet.thinglinks.video.vo.result.record;

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

import java.io.Serial;
import java.time.LocalDateTime;


/**
 * <p>
 * 表单查询方法返回值VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "录制文件表")
public class VideoRecordFileResultVO extends AuditableResultVO {

    @Serial
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
     * 文件ID
     */
    @Schema(description = "文件ID")
    private Long fileId;
    /**
     * 文件大小(字节)
     */
    @Schema(description = "文件大小(字节)")
    private Long fileSize;
    /**
     * 文件格式
     */
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_RECORD_FILE_FORMAT)
    @Schema(description = "文件格式")
    private String fileFormat;
    /**
     * 时长(秒)
     */
    @Schema(description = "时长(秒)")
    private Integer duration;
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
     * 缩略图文件ID
     */
    @Schema(description = "缩略图文件ID")
    private Long thumbnailFileId;
    /**
     * 文件状态
     */
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_RECORD_FILE_STATUS)
    @Schema(description = "文件状态")
    private Integer fileStatus;
    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private String extendParams;
    /**
     * 删除标记
     */
    @Schema(description = "删除标记")
    private Integer deleted;


}
