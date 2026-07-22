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

/**
 * <p>
 * 表单查询方法返回值VO
 * 告警规则渠道详情VO
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-09 21:14:58
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "RuleAlarmChannelDetailsResultVO", description = "告警规则渠道详情VO")
public class RuleAlarmChannelDetailsResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 渠道名称
     */
    @Schema(description = "渠道名称")
    private String channelName;
    /**
     * 渠道类型
     */
    @Schema(description = "渠道类型")
    private Integer channelType;
    /**
     * 告警配置
     */
    @Schema(description = "告警配置")
    private String channelConfig;
    /**
     * 启用状态
     */
    @Schema(description = "启用状态")
    private Integer status;
    /**
     * 描述
     */
    @Schema(description = "描述")
    private String remark;


}
