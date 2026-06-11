package com.mqttsnet.thinglinks.base.vo.result.system;


import java.io.Serial;

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
 * 角色的资源
 * </p>
 *
 * @author mqttsnet
 * @since 2021-10-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "BaseRoleResourceRelResultVO", description = "角色的资源")
public class BaseRoleResourceRelResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 资源id;#def_resource
     */
    @Schema(description = "资源id")

    private Long resourceId;
    /**
     * 角色id;#base_role
     */
    @Schema(description = "角色id")

    private Long roleId;
    /**
     * 组织ID
     */
    @Schema(description = "组织ID")

    private Long orgId;
}
