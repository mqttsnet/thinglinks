package com.mqttsnet.thinglinks.manager.linkage;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleExecutionLogStatsResultVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 通用业务接口
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 * @create [2024-12-02 18:41:26] [mqttsnet]
 */
public interface RuleExecutionLogManager extends SuperManager<RuleExecutionLog> {

    /**
     * 按场景联动执行日志查询条件获取列表。
     *
     * @param query 查询条件
     * @return 执行日志列表
     */
    List<RuleExecutionLog> getRuleExecutionLogList(RuleExecutionLogPageQuery query);

    /**
     * 按查询条件聚合执行日志统计。
     *
     * @param query 查询条件
     * @return 统计结果
     */
    RuleExecutionLogStatsResultVO getRuleExecutionLogStats(RuleExecutionLogPageQuery query);

    /**
     * 按查询条件获取执行日志时间范围。
     *
     * @param query 查询条件
     * @return 时间范围
     */
    Map<String, Object> getRuleExecutionLogTimeBounds(RuleExecutionLogPageQuery query);

    /**
     * 按查询条件聚合执行日志趋势。
     *
     * @param query       查询条件
     * @param granularity 时间粒度
     * @return 趋势点
     */
    List<RuleExecutionLogStatsResultVO.TimelinePoint> getRuleExecutionLogTimeline(RuleExecutionLogPageQuery query,
                                                                                  String granularity);

    /**
     * 按查询条件分批获取待清理的执行日志主键。
     *
     * @param query 查询条件
     * @param limit 批次大小
     * @return 执行日志主键
     */
    List<Long> getRuleExecutionLogIds(RuleExecutionLogPageQuery query, int limit);

}
