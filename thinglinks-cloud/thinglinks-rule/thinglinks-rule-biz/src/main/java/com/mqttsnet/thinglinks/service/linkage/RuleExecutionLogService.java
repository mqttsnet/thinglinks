package com.mqttsnet.thinglinks.service.linkage;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.BaseExecutionLogEvent;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogStatsResultVO;

/**
 * <p>
 * 业务接口
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 * @create [2024-12-02 18:41:26] [mqttsnet]
 */
public interface RuleExecutionLogService extends SuperService<Long, RuleExecutionLog> {


    /**
     * 处理规则日志存储
     *
     * @param event
     */
    void saveRuleExecutionLog(BaseExecutionLogEvent event);

    /**
     * 获取规则执行日志详情
     *
     * @param id 规则执行日志ID
     * @return 规则执行日志详情
     */
    RuleExecutionLogDetailsResultVO getRuleExecutionLogDetails(Long id);

    /**
     * 统计当前筛选条件下的执行日志。
     *
     * @param query 查询条件
     * @return 统计结果
     */
    RuleExecutionLogStatsResultVO getRuleExecutionLogStats(RuleExecutionLogPageQuery query);

    /**
     * 清理当前筛选条件下的执行日志。
     *
     * @param query 查询条件
     * @return 清理的主日志条数
     */
    Long clearRuleExecutionLogs(RuleExecutionLogPageQuery query);
}
