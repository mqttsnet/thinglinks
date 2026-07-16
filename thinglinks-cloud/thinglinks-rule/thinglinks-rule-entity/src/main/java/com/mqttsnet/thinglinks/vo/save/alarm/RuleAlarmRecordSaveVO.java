package com.mqttsnet.thinglinks.vo.save.alarm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 表单保存方法VO
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
@EqualsAndHashCode
@Builder
@Schema(title = "RuleAlarmRecordSaveVO", description = "告警记录表")
public class RuleAlarmRecordSaveVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @Schema(description = "应用ID")
    @NotEmpty(message = "请填写应用ID")
    @Size(max = 64, message = "应用ID长度不能超过{max}")
    private String appId;
    /**
     * 告警编码
     */
    @Schema(description = "告警编码")
    @NotEmpty(message = "请填写告警编码")
    @Size(max = 100, message = "告警编码长度不能超过{max}")
    private String alarmIdentification;
    /**
     * 发生时间
     */
    @Schema(description = "发生时间")
    @NotNull(message = "请填写发生时间")
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
    @Size(max = 2147483647, message = "处理记录长度不能超过{max}")
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
    @Size(max = 2147483647, message = "解决记录长度不能超过{max}")
    private String resolutionNotes;
    /**
     * 告警具体内容信息
     */
    @Schema(description = "告警具体内容信息")
    @NotEmpty(message = "请填写告警具体内容信息")
    @Size(max = 2147483647, message = "告警具体内容信息长度不能超过{max}")
    private String contentData;
    /**
     * 处理状态
     */
    @Schema(description = "处理状态")
    @NotNull(message = "请填写处理状态")
    private Integer handledStatus;
    /**
     * 描述
     */
    @Schema(description = "描述")
    @Size(max = 255, message = "描述长度不能超过{max}")
    private String remark;
    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private Long createdBy;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;


}
