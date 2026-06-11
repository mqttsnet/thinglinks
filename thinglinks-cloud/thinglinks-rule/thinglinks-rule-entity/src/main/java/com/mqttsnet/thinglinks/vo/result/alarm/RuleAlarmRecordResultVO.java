package com.mqttsnet.thinglinks.vo.result.alarm;

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
 * 告警记录表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-09 21:15:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleAlarmRecordResultVO", description = "告警记录表")
public class RuleAlarmRecordResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    private String appId;
    /**
     * 告警编码
     */
    @Schema(description = "告警编码")
    private String alarmIdentification;
    /**
     * 发生时间
     */
    @Schema(description = "发生时间")
    private LocalDateTime occurredTime;
    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handledTime;
    /**
     * 处理记录
     */
    @Schema(description = "处理记录")
    private String handlingNotes;
    /**
     * 解决时间
     */
    @Schema(description = "解决时间")
    private LocalDateTime resolvedTime;
    /**
     * 解决记录
     */
    @Schema(description = "解决记录")
    private String resolutionNotes;
    /**
     * 告警具体内容信息
     */
    @Schema(description = "告警具体内容信息")
    private String contentData;
    /**
     * 处理状态
     */
    @Schema(description = "处理状态")
    private Integer handledStatus;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;


}
