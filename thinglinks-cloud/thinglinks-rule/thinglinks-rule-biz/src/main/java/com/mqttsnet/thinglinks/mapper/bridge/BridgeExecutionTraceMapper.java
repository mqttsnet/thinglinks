package com.mqttsnet.thinglinks.mapper.bridge;

import com.mqttsnet.basic.base.mapper.SuperMapper;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * 桥接执行 trace 主表（链路回放用）
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Repository
public interface BridgeExecutionTraceMapper extends SuperMapper<BridgeExecutionTrace> {

    /**
     * 聚合桥接执行日志统计。
     *
     * @param query 查询条件
     * @return 统计行
     */
    Map<String, Object> selectStats(@Param("query") BridgeExecutionTracePageQuery query);

    /**
     * 查询桥接执行日志时间范围。
     *
     * @param query 查询条件
     * @return 时间范围
     */
    Map<String, Object> selectTimeBounds(@Param("query") BridgeExecutionTracePageQuery query);

    /**
     * 聚合桥接执行日志趋势。
     *
     * @param query       查询条件
     * @param granularity 时间粒度
     * @return 趋势数据
     */
    List<Map<String, Object>> selectTimelineStats(@Param("query") BridgeExecutionTracePageQuery query,
                                                  @Param("granularity") String granularity);

    /**
     * 聚合触发量 Top 规则。
     *
     * @param query 查询条件
     * @param limit 返回数量
     * @return Top 规则数据
     */
    List<Map<String, Object>> selectTopRules(@Param("query") BridgeExecutionTracePageQuery query,
                                             @Param("limit") int limit);
}
