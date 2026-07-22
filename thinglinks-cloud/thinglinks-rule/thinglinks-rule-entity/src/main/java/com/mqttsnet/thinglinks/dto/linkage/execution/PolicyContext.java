package com.mqttsnet.thinglinks.dto.linkage.execution;

import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.dto.linkage.RulePolicyDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 封装执行策略所需的上下文信息。
 *
 * @author xiaonan
 */
@Data
public class PolicyContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 2877163343519909517L;

    /**
     * 规则执行流水号 (每次执行生成全局唯一ID，可用于数据追溯)
     */
    private Long ruleExecutionId = Long.valueOf(SnowflakeIdUtil.nextId());

    // 租户ID
    private String tenantId;
    // 规则标识
    private String ruleIdentification;

    // 规则名称
    private String ruleName;

    // 规则对象DTO
    private RulePolicyDTO rulePolicyDTO;

    /**
     * 事件路径要评估的条件类型(ConditionTypeEnum.value)。
     * <p>非空时策略工厂只分发匹配类型的条件组(混搭规则的定时条件仍归定时任务触发);null=全量评估(定时/API 路径)。
     */
    private Integer triggerConditionType;

    /**
     * 触发事件上下文(消息内值)。null=定时/API 触发路径。
     */
    private TriggerEventDTO triggerEvent;
}
