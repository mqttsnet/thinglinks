package com.mqttsnet.thinglinks.system.vo.result.system;


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
 * 客户端
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
@Schema(title = "DefClientResultVO", description = "客户端")
public class DefClientResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 客户端ID
     */
    @Schema(description = "客户端ID")

    private String clientId;
    /**
     * 客户端密码
     */
    @Schema(description = "客户端密码")

    private String clientSecret;
    /**
     * 客户端名称
     */
    @Schema(description = "客户端名称")

    private String name;
    /**
     * 类型;[10-WEB网站;15-移动端应用;20-手机H5网页;25-内部服务; 30-第三方应用]	@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.CLIENT_TYPE)
     */
    @Schema(description = "类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.CLIENT_TYPE)

    private String type;
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
}
