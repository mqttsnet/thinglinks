package com.mqttsnet.thinglinks.system.vo.result.tenant;


import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.enumeration.system.TenantConnectTypeEnum;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.system.enumeration.tenant.DefTenantRegisterTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * 实体类
 * 企业
 * </p>
 *
 * @author mqttsnet
 * @since 2021-10-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefTenantResultVO", description = "企业")
public class DefTenantResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    /**
     * 企业编码
     */
    @Schema(description = "企业编码")

    private String code;
    /**
     * 企业名称
     */
    @Schema(description = "企业名称")

    private String name;
    /**
     * 企业简称
     */
    @Schema(description = "企业简称")

    private String abbreviation;
    /**
     * 统一社会信用代码
     */
    @Schema(description = "统一社会信用代码")

    private String creditCode;
    /**
     * 联系人
     */
    @Schema(description = "联系人")

    private String contactPerson;
    /**
     * 联系方式
     */
    @Schema(description = "联系方式")

    private String contactPhone;
    /** 类别 */
    @Schema(description = "类别")

    private String classify;
    /**
     * 联系邮箱
     */
    @Schema(description = "联系邮箱")

    private String contactEmail;
    /**
     * 省
     */
    @Schema(description = "省")

    private Long provinceId;
    /**
     * 省
     */
    @Schema(description = "省")

    private String provinceName;
    /**
     * 市
     */
    @Schema(description = "市")

    private Long cityId;
    /**
     * 市
     */
    @Schema(description = "市")

    private String cityName;
    /**
     * 区
     */
    @Schema(description = "区")

    private Long districtId;
    /**
     * 区
     */
    @Schema(description = "区")

    private String districtName;
    /**
     * 详细地址
     */
    @Schema(description = "详细地址")

    private String address;
    /**
     * 类型;#{CREATE:创建;REGISTER:注册}
     */
    @Schema(description = "类型")

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.DefTenantRegisterTypeEnum)
    private DefTenantRegisterTypeEnum registerType;
    /**
     * 数据源链接类型;#TenantConnectTypeEnum{LOCAL:本地;REMOTE:远程}
     */
    @Schema(description = "数据源链接类型")

    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.TenantConnectTypeEnum)
    private TenantConnectTypeEnum connectType;
    /**
     * 状态;0-禁用 1-启用
     */
    @Schema(description = "状态")

    private Boolean state;
    /**
     * 审核状态;[05-正常 10-待初始化 15-已撤回 20-待审核 25-已拒绝 30-已同意]
     *
     * @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Tenant.TENANT_STATUS)
     */
    @Schema(description = "审核状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.TENANT_STATUS)

    private String status;
    /**
     * 内置
     */
    @Schema(description = "内置")

    private Boolean readonly;
    /**
     * 创建人
     */
    @Schema(description = "创建人")

    private String createdName;
    /**
     * 有效期;
     * 为空表示永久
     */
    @Schema(description = "有效期")

    private LocalDateTime expirationTime;
    /**
     * 企业简介
     */
    @Schema(description = "企业简介")

    private String describe;
    /**
     * 审核意见
     */
    @Schema(description = "审核意见")
    private String reviewComments;


    @Schema(description = "员工状态")
    private Boolean employeeState;
    @Schema(description = "员工id")
    private Long employeeId;
    @Schema(description = "是否默认")
    private Boolean isDefault;
}
