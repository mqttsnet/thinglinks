package com.mqttsnet.thinglinks.service.linkage;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.linkage.RuleConditionExecutionLog;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.BaseExecutionLogEvent;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionExecutionLogResultVO;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 业务接口
 * 规则条件执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:53:47
 * @create [2024-12-02 18:53:47] [mqttsnet]
 */
public interface RuleConditionExecutionLogService extends SuperService<Long, RuleConditionExecutionLog> {

    /**
     * 处理规则条件日志存储
     *
     * @param event
     */
    void saveConditionExecutionLog(BaseExecutionLogEvent event);


    /**
     * 获取规则条件执行日志列表
     *
     * @param query 查询条件 {@link RuleConditionExecutionLogPageQuery}
     * @return 规则条件执行日志列表 {@link RuleConditionExecutionLogResultVO}
     */
    List<RuleConditionExecutionLogResultVO> getRuleConditionExecutionLogResultVOList(RuleConditionExecutionLogPageQuery query);

    /**
     * 根据规则执行流水号批量删除条件执行日志。
     *
     * @param ruleExecutionIds 规则执行流水号集合
     * @return 是否执行删除
     */
    boolean removeByRuleExecutionIds(Collection<Long> ruleExecutionIds);
}

