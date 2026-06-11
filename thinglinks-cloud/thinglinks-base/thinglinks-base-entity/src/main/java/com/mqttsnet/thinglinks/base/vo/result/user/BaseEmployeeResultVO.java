package com.mqttsnet.thinglinks.base.vo.result.user;


import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.entity.system.SysUser;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.util.List;

/**
 * <p>
 * 实体类
 * 员工
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
@Schema(title = "BaseEmployeeResultVO", description = "员工")
public class BaseEmployeeResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 是否默认员工;[0-否 1-是]
     */
    @Schema(description = "是否默认员工")

    private Boolean isDefault;
    /**
     * 用户id
     */
    @Schema(description = "用户id")

    private Long userId;
    /**
     * 岗位Id
     */
    @Schema(description = "岗位Id")

    @Echo(api = EchoApi.POSITION_ID_CLASS)
    private Long positionId;
    /**
     * 组织Id
     */
    @Schema(description = "组织Id")

    @Echo(api = EchoApi.ORG_ID_CLASS)
    private List<Long> orgIdList;

    @Schema(description = "最后一次登录单位ID")
    @Echo(api = EchoApi.ORG_ID_CLASS)
    private Long lastCompanyId;

    @Schema(description = "最后一次登录部门ID")
    private Long lastDeptId;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")

    private String realName;

    /**
     * 职位状态;[10-在职 20-离职]@Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.POSITION_STATUS)
     */
    @Schema(description = "职位状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.POSITION_STATUS)

    private String positionStatus;
    /**
     * 状态;[0-禁用 1-启用]
     */
    @Schema(description = "状态")

    private Boolean state;

    /**
     * 激活状态;[10-未激活 20-已激活]
     */
    @Schema(description = "激活状态")
    private String activeStatus;

    @Schema(description = "用户信息")
    private SysUser defUser;
}
