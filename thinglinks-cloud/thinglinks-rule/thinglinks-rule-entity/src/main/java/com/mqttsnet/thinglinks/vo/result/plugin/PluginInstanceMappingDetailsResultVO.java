package com.mqttsnet.thinglinks.vo.result.plugin;

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
 * -----------------------------------------------------------------------------
 * File Name: PluginInstanceMappingDetailsResultVO
 * -----------------------------------------------------------------------------
 * Description:
 * <p>
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/11/19       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/11/19 11:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "插件实例映射及实例详细信息")
public class PluginInstanceMappingDetailsResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "插件唯一标识")
    private String pluginIdentification;

    @Schema(description = "实例唯一标识")
    private String instanceIdentification;

    @Schema(description = "插件在该实例上使用的端口号")
    private Integer port;

    @Schema(description = "端口类型或用途（如 HTTP, HTTPS, 管理端口等）")
    private String portType;

    @Schema(description = "插件实例状态：0-正常，1-异常")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
