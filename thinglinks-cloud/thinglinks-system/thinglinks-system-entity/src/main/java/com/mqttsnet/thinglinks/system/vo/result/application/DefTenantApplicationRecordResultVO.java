package com.mqttsnet.thinglinks.system.vo.result.application;


import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
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
 * 租户应用授权记录
 * </p>
 *
 * @author mqttsnet
 * @since 2021-09-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefTenantApplicationRecordResultVO", description = "租户应用授权记录")
public class DefTenantApplicationRecordResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 授权ID
     */
    @Schema(description = "授权ID")

    private Long tenantApplicationRelId;
    /**
     * 企业ID
     */
    @Schema(description = "企业ID")

    private Long tenantId;
    /**
     * 应用ID
     */
    @Schema(description = "应用ID")

    private Long applicationId;
    /**
     * 应用名称
     */
    @Schema(description = "应用名称")

    private String applicationName;
    /**
     * 企业名称
     */
    @Schema(description = "企业名称")

    private String tenantName;
    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名")

    private String operateByName;
    /**
     * 授权类型;[10-应用授权 20-应用续期 30-取消授权]
     *
     * @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.APPLICATION_GRANT_TYPE)
     */
    @Schema(description = "授权类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.APPLICATION_GRANT_TYPE)

    private String grantType;
}
