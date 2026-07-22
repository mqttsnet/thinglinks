package com.mqttsnet.thinglinks.sop.vo.result;

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

/**
 * <p>
 * 表单查询方法返回值VO
 * 接口信息表
 * </p>
 *
 * @author zuihou
 * @since 2025-05-11 10:34:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(description = "接口信息表")
public class SopApiInfoResultVO extends AuditableResultVO {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称")
    private String application;
    /**
     * 接口名称
     */
    @Schema(description = "接口名称")
    private String apiName;
    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String apiVersion;
    /**
     * 接口描述
     */
    @Schema(description = "接口描述")
    private String description;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
    /**
     * 接口class
     */
    @Schema(description = "接口class")
    private String interfaceClassName;
    /**
     * 方法名称
     */
    @Schema(description = "方法名称")
    private String methodName;
    /**
     * 参数信息
     */
    @Schema(description = "参数信息")
    private String paramInfo;
    /**
     * 接口是否需要授权访问
     */
    @Schema(description = "接口是否需要授权访问")
    private Integer isPermission;
    /**
     * 是否需要appAuthToken
     */
    @Schema(description = "是否需要appAuthToken")
    private Integer isNeedToken;
    /**
     * 是否有公共响应参数
     */
    @Schema(description = "是否有公共响应参数")
    private Integer hasCommonResponse;
    /**
     * 注册来源
     * [1-系统注册 2-手动注册]
     */
    @Schema(description = "注册来源")
    private Integer regSource;
    /**
     * 接口模式
     * [1-open接口 2-Restful模式]
     */
    @Schema(description = "接口模式")
    private Integer apiMode;
    /**
     * 状态
     * [1-启用 0-禁用]
     */
    @Schema(description = "状态")
    private Integer status;


}
