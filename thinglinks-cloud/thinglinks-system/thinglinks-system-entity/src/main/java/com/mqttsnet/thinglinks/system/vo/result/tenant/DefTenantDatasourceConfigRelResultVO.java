package com.mqttsnet.thinglinks.system.vo.result.tenant;


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
 * 租户的数据源
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
@Schema(title = "DefTenantDatasourceConfigRelResultVO", description = "租户的数据源")
public class DefTenantDatasourceConfigRelResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 租户id
     */
    @Schema(description = "租户id")

    private Long tenantId;
    /**
     * 数据源id
     */
    @Schema(description = "数据源id")

    private Long datasourceConfigId;
    /**
     * 服务
     */
    @Schema(description = "服务")

    private String application;
}
