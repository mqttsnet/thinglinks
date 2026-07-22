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
 * 数据源
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
@Schema(title = "DefDatasourceConfigResultVO", description = "数据源")
public class DefDatasourceConfigResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 名称
     */
    @Schema(description = "名称")

    private String name;
    @Schema(description = "数据源前缀")
    private String dbPrefix;
    /**
     * 用户名
     */
    @Schema(description = "用户名")

    private String username;
    /**
     * 链接
     */
    @Schema(description = "链接")

    private String url;
    /**
     * 驱动
     */
    @Schema(description = "驱动")

    private String driverClassName;

    private Long tenantId;
}
