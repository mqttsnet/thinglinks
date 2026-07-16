package com.mqttsnet.thinglinks.mapper.linkage;

import com.mqttsnet.basic.base.mapper.SuperMapper;
import com.mqttsnet.thinglinks.entity.linkage.RuleExecutionLog;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleExecutionLogPageQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * Mapper 接口
 * 规则执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:41:26
 * @create [2024-12-02 18:41:26] [mqttsnet]
 */
@Repository
public interface RuleExecutionLogMapper extends SuperMapper<RuleExecutionLog> {

    /**
     * 聚合执行日志统计。
     *
     * @param query 查询条件
     * @return 统计行
     */
    Map<String, Object> selectStats(@Param("query") RuleExecutionLogPageQuery query);

    /**
     * 查询执行日志时间范围。
     *
     * @param query 查询条件
     * @return 时间范围
     */
    Map<String, Object> selectTimeBounds(@Param("query") RuleExecutionLogPageQuery query);

    /**
     * 聚合执行日志趋势。
     *
     * @param query       查询条件
     * @param granularity 时间粒度
     * @return 趋势数据
     */
    List<Map<String, Object>> selectTimelineStats(@Param("query") RuleExecutionLogPageQuery query,
                                                  @Param("granularity") String granularity);

    /**
     * 分批查询待清理执行日志主键。
     *
     * @param query 查询条件
     * @param limit 批次大小
     * @return 主键列表
     */
    List<Long> selectIdsForClear(@Param("query") RuleExecutionLogPageQuery query,
                                 @Param("limit") int limit);

}

