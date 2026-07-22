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
 * 员工所在部门
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
@Schema(title = "BaseEmployeeOrgRelResultVO", description = "员工所在部门")
public class BaseEmployeeOrgRelResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 机构ID
     */
    @Schema(description = "机构ID")

    private Long orgId;
    /**
     * 员工ID
     */
    @Schema(description = "员工ID")

    private Long employeeId;
}
