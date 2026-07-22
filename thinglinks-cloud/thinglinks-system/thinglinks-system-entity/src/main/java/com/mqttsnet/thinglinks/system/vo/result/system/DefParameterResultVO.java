package com.mqttsnet.thinglinks.system.vo.result.system;


import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 实体类
 * 参数配置
 * </p>
 *
 * @author mqttsnet
 * @since 2021-10-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefParameterResultVO", description = "参数配置")
public class DefParameterResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 参数键
     */
    @Schema(description = "参数键")

    private String key;
    /**
     * 参数值
     */
    @Schema(description = "参数值")

    private String value;
    /**
     * 参数名称
     */
    @Schema(description = "参数名称")

    private String name;
    /**
     * 备注
     */
    @Schema(description = "备注")

    private String remarks;
    /**
     * 状态
     */
    @Schema(description = "状态")

    private Boolean state;
    /**
     * 类型;[10-系统参数 20-业务参数]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Tenant.PARAMETER_TYPE)
     */
    @Schema(description = "类型")

    private String paramType;
}
