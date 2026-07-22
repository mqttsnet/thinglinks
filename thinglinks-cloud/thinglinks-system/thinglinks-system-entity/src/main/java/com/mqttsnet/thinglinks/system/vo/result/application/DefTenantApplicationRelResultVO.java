package com.mqttsnet.thinglinks.system.vo.result.application;


import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
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
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 实体类
 * 租户的应用
 * </p>
 *
 * @author mqttsnet
 * @since 2021-09-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefTenantApplicationRelResultVO", description = "租户的应用")
public class DefTenantApplicationRelResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")

    @Echo(api = EchoApi.DEF_TENANT_SERVICE_IMPL_CLASS)
    private Long tenantId;
    /**
     * 应用ID
     */
    @Schema(description = "应用ID")

    @Echo(api = EchoApi.DEF_APPLICATION_SERVICE_IMPL_CLASS)
    private Long applicationId;
    /**
     * 过期时间
     */
    @Schema(description = "过期时间")

    private LocalDateTime expirationTime;

    @Schema(description = "是否过期")
    private Boolean expired;

    @Schema(description = "应用下的资源")
    private Collection<DefResourceResultVO> resourceList;
    @Schema(description = "选中的资源id")
    private List<Long> checkedList;
}
