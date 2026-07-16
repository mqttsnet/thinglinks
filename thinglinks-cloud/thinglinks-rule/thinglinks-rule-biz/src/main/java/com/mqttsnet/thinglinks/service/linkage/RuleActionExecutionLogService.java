package com.mqttsnet.thinglinks.service.linkage;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.linkage.RuleActionExecutionLog;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.BaseExecutionLogEvent;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleActionExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleActionExecutionLogResultVO;

import java.util.Collection;
import java.util.List;


/**
 * <p>
 * 业务接口
 * 规则动作执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:54:41
 * @create [2024-12-02 18:54:41] [mqttsnet]
 */
public interface RuleActionExecutionLogService extends SuperService<Long, RuleActionExecutionLog> {


    /**
     * 处理规则动作日志存储
     *
     * @param event
     */
    void saveActionExecutionLog(BaseExecutionLogEvent event);


    /**
     * 获取规则动作执行日志列表
     *
     * @param query 查询条件 {@link RuleActionExecutionLogPageQuery}
     * @return 规则动作执行日志列表 {@link RuleActionExecutionLogResultVO}
     */
    List<RuleActionExecutionLogResultVO> getRuleActionExecutionLogResultVOList(RuleActionExecutionLogPageQuery query);

    /**
     * 根据规则执行流水号批量删除动作执行日志。
     *
     * @param ruleExecutionIds 规则执行流水号集合
     * @return 是否执行删除
     */
    boolean removeByRuleExecutionIds(Collection<Long> ruleExecutionIds);
}

