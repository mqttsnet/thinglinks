package com.mqttsnet.thinglinks.device.vo.update;

import com.mqttsnet.basic.base.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 表单修改方法VO
 * 设备访问控制(ACL)规则表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-06-11 19:57:46
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode
@Builder
@Schema(title = "DeviceAclRuleUpdateVO", description = "设备访问控制(ACL)规则")
public class DeviceAclRuleUpdateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @NotNull(message = "请填写id", groups = SuperEntity.Update.class)
    private Long id;


    /**
     * 规则名称
     */
    @NotEmpty(message = "请填写规则名称")
    @Size(max = 100, message = "规则名称长度不能超过{max}")
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    @Size(max = 255, message = "设备标识长度不能超过{max}")
    private String deviceIdentification;
    /**
     * 动作类型((0:全部、1:发布、2:订阅、3:取消订阅))
     */
    @Schema(description = "动作类型((0:全部、1:发布、2:订阅、3:取消订阅))")
    @NotNull(message = "请填写动作类型((0:全部、1:发布、2:订阅、3:取消订阅))")
    @Min(value = 0, message = "动作类型必须在 0-3 范围内")
    @Max(value = 3, message = "动作类型必须在 0-3 范围内")
    private Integer actionType;
    /**
     * 规则优先级(0-1000,值越小优先级越高)
     */
    @Schema(description = "规则优先级(0-1000,值越小优先级越高)")
    @NotNull(message = "请填写规则优先级(0-1000,值越小优先级越高)")
    @Min(value = 0, message = "优先级必须在 0-1000 范围内")
    @Max(value = 1000, message = "优先级必须在 0-1000 范围内")
    private Integer priority;
    /**
     * MQTT主题模式(支持通配符)
     */
    @Schema(description = "MQTT主题模式(支持通配符)")
    @Size(max = 255, message = "MQTT主题模式(支持通配符)长度不能超过{max}")
    private String topicPattern;
    /**
     * IP白名单地址(多个用逗号分隔)
     */
    @Schema(description = "IP白名单地址(多个用逗号分隔)")
    @Size(max = 255, message = "IP白名单地址(多个用逗号分隔)长度不能超过{max}")
    private String ipWhitelist;
    /**
     * 决策(0:拒绝、1:允许)
     */
    @Schema(description = "决策(0:拒绝、1:允许)")
    @NotNull(message = "请填写决策(0:拒绝、1:允许)")
    private Boolean decision;
    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    @NotNull(message = "请填写是否启用")
    private Boolean enabled;
    /**
     * 备注
     */
    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过{max}")
    private String remark;
    /**
     * 创建人组织
     */
    @Schema(description = "创建人组织")
    private Long createdOrgId;
    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    @NotEmpty(message = "请填写产品标识")
    @Size(max = 100, message = "产品标识长度不能超过{max}")
    private String productIdentification;
    /**
     * 规则级别(0:产品级、1:设备级)
     */
    @Schema(description = "规则级别(0:产品级、1:设备级)")
    @NotNull(message = "请填写规则级别(0:产品级、1:设备级)")
    @Min(value = 0, message = "规则级别必须为 0(产品级)或 1(设备级)")
    @Max(value = 1, message = "规则级别必须为 0(产品级)或 1(设备级)")
    private Integer ruleLevel;


}
