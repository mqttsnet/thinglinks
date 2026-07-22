package com.mqttsnet.thinglinks.system.vo.result.application;


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
 * 租户的资源
 * </p>
 *
 * @author mqttsnet
 * @since 2021-09-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefTenantResourceRelResultVO", description = "租户的资源")
public class DefTenantResourceRelResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")

    private Long tenantId;
    /**
     * 应用Id
     */
    @Schema(description = "应用Id")

    private Long applicationId;
    /**
     * 资源ID
     */
    @Schema(description = "资源ID")

    private Long resourceId;
}
