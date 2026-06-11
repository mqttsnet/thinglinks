package com.mqttsnet.thinglinks.base.vo.result.user;


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
 * 员工的角色
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
@Schema(title = "BaseEmployeeRoleRelResultVO", description = "员工的角色")
public class BaseEmployeeRoleRelResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 角色;#base_role
     */
    @Schema(description = "角色")

    private Long roleId;
    /**
     * 员工;#base_employee
     */
    @Schema(description = "员工")

    private Long employeeId;
}
