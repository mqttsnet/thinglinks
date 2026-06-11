package com.mqttsnet.thinglinks.msg.vo.result;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 表单查询方法返回值VO
 * 接口执行日志
 * </p>
 *
 * @author mqttsnet
 * @date 2022-07-09 23:58:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ExtendInterfaceLogResultVO", description = "接口执行日志")
public class ExtendInterfaceLogResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "")
    private Long id;

    /**
     * 接口ID;
     * #extend_interface
     */
    @Schema(description = "接口ID")
    private Long interfaceId;
    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String name;
    /**
     * 成功次数
     */
    @Schema(description = "成功次数")
    private Integer successCount;
    /**
     * 失败次数
     */
    @Schema(description = "失败次数")
    private Integer failCount;
    /**
     * 最后执行时间
     */
    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecTime;


}
