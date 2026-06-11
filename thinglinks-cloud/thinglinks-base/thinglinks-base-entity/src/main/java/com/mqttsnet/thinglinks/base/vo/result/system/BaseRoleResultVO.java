package com.mqttsnet.thinglinks.base.vo.result.system;


import java.io.Serial;

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
 * 角色
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
@Schema(title = "BaseRoleResultVO", description = "角色")
public class BaseRoleResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 角色类型;10-系统角色 20-自定义角色
     */
    @Schema(description = "角色类型")

    private String type;
    /**
     * 角色类别;[10-功能角色 20-桌面角色 30-数据角色]
     */
    @Schema(description = "角色类别")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.ROLE_CATEGORY)
    private String category;
    /**
     * 名称
     */
    @Schema(description = "名称")

    private String name;
    /**
     * 编码
     */
    @Schema(description = "编码")

    private String code;
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
    /**
     * 内置角色
     */
    @Schema(description = "内置角色")

    private Boolean readonly;
}
