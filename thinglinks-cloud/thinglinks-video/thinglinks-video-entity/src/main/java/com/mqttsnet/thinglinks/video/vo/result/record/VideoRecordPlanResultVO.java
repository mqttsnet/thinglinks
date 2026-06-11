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


/**
 * <p>
 * 表单查询方法返回值VO
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "录制计划表")
public class VideoRecordPlanResultVO extends AuditableResultVO {

    @Serial
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
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_RECORD_PLAN_TYPE)
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
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_RECORD_FILE_FORMAT)
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
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Video.VIDEO_RECORD_PLAN_STATUS)
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
     * 删除标记
     */
    @Schema(description = "删除标记")
    private Integer deleted;


}
