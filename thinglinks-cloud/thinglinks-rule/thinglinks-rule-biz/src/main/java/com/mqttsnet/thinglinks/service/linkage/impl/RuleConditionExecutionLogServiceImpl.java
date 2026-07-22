package com.mqttsnet.thinglinks.service.linkage.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.linkage.RuleConditionExecutionLog;
import com.mqttsnet.thinglinks.manager.linkage.RuleConditionExecutionLogManager;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.BaseExecutionLogEvent;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.ConditionExecutionLogEvent;
import com.mqttsnet.thinglinks.service.linkage.RuleConditionExecutionLogService;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleConditionExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleConditionExecutionLogResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 业务实现类
 * 规则条件执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:53:47
 * @create [2024-12-02 18:53:47] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleConditionExecutionLogServiceImpl extends SuperServiceImpl<RuleConditionExecutionLogManager, Long, RuleConditionExecutionLog> implements RuleConditionExecutionLogService {


    @Override
    public void saveConditionExecutionLog(BaseExecutionLogEvent event) {
        RuleConditionExecutionLog conditionLog = new RuleConditionExecutionLog();
        conditionLog.setRuleExecutionId(event.getRuleExecutionId());
        conditionLog.setConditionUuid(((ConditionExecutionLogEvent) event).getConditionUuid());
        conditionLog.setConditionType(((ConditionExecutionLogEvent) event).getConditionTypeEnum().getValue());
        conditionLog.setEvaluationResult(((ConditionExecutionLogEvent) event).getEvaluationResult());
        conditionLog.setStartTime(event.getStartTime());
        conditionLog.setEndTime(event.getEndTime());
        conditionLog.setExtendParams(event.getExtendParams());
        conditionLog.setRemark(event.getRemark());
        superManager.save(conditionLog);
        log.info("Condition execution log saved: {}", conditionLog);
    }


    /**
     * 获取规则条件执行日志列表
     *
     * @param query 查询条件 {@link RuleConditionExecutionLogPageQuery}
     * @return 规则条件执行日志列表 {@link RuleConditionExecutionLogResultVO}
     */
    @Override
    public List<RuleConditionExecutionLogResultVO> getRuleConditionExecutionLogResultVOList(RuleConditionExecutionLogPageQuery query) {
        List<RuleConditionExecutionLog> logs = superManager.getRuleConditionExecutionLogList(query);
        return BeanPlusUtil.toBeanList(logs, RuleConditionExecutionLogResultVO.class);
    }

    @Override
    public boolean removeByRuleExecutionIds(Collection<Long> ruleExecutionIds) {
        if (ruleExecutionIds == null || ruleExecutionIds.isEmpty()) {
            return false;
        }
        return superManager.remove(Wrappers.<RuleConditionExecutionLog>lambdaQuery()
                .in(RuleConditionExecutionLog::getRuleExecutionId, ruleExecutionIds));
    }
}

