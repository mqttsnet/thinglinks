package com.mqttsnet.thinglinks.device.vo.result;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
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
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DeviceAclRuleResultVO", description = "设备访问控制(ACL)规则")
public class DeviceAclRuleResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;

    /**
     * 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 设备标识
     */
    @Schema(description = "设备标识")
    private String deviceIdentification;
    /**
     * 动作类型((0:全部、1:发布、2:订阅、3:取消订阅))
     */
    @Schema(description = "动作类型((0:全部、1:发布、2:订阅、3:取消订阅))")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_ACL_RULE_ACTION_TYPE)
    private Integer actionType;
    /**
     * 规则优先级(0-1000,值越小优先级越高)
     */
    @Schema(description = "规则优先级(0-1000,值越小优先级越高)")
    private Integer priority;
    /**
     * MQTT主题模式(支持通配符)
     */
    @Schema(description = "MQTT主题模式(支持通配符)")
    private String topicPattern;
    /**
     * IP白名单地址(多个用逗号分隔)
     */
    @Schema(description = "IP白名单地址(多个用逗号分隔)")
    private String ipWhitelist;
    /**
     * 决策(0:拒绝、1:允许)
     */
    @Schema(description = "决策(0:拒绝、1:允许)")

    private Boolean decision;
    /**
     * 是否启用
     */
    @Schema(description = "是否启用")
    private Boolean enabled;
    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 产品标识
     */
    @Schema(description = "产品标识")
    private String productIdentification;
    /**
     * 规则级别(0:产品级、1:设备级)
     */
    @Schema(description = "规则级别(0:产品级、1:设备级)")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Link.LINK_ACL_RULE_LEVEL)
    private Integer ruleLevel;


    @Schema(description = "产品信息")
    private ProductResultVO productResultVO;

    @Schema(description = "设备信息")
    private DeviceResultVO deviceResultVO;


}
