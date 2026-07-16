package com.mqttsnet.thinglinks.service.linkage.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.linkage.RuleActionExecutionLog;
import com.mqttsnet.thinglinks.manager.linkage.RuleActionExecutionLogManager;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.ActionExecutionLogEvent;
import com.mqttsnet.thinglinks.service.execution.event.executionlog.BaseExecutionLogEvent;
import com.mqttsnet.thinglinks.service.linkage.RuleActionExecutionLogService;
import com.mqttsnet.thinglinks.vo.query.linkage.RuleActionExecutionLogPageQuery;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleActionExecutionLogResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 业务实现类
 * 规则动作执行日志表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-12-02 18:54:41
 * @create [2024-12-02 18:54:41] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class RuleActionExecutionLogServiceImpl extends SuperServiceImpl<RuleActionExecutionLogManager, Long, RuleActionExecutionLog> implements RuleActionExecutionLogService {


    @Override
    public void saveActionExecutionLog(BaseExecutionLogEvent event) {
        RuleActionExecutionLog actionLog = new RuleActionExecutionLog();
        actionLog.setRuleExecutionId(event.getRuleExecutionId());
        actionLog.setActionType(((ActionExecutionLogEvent) event).getActionType());
        actionLog.setActionContent(((ActionExecutionLogEvent) event).getActionContent());
        actionLog.setResult(((ActionExecutionLogEvent) event).getResult());
        actionLog.setStartTime(event.getStartTime());
        actionLog.setEndTime(event.getEndTime());
        actionLog.setExtendParams(event.getExtendParams());
        actionLog.setRemark(event.getRemark());
        superManager.save(actionLog);
        log.info("Action execution log saved: {}", actionLog);
    }


    /**
     * 获取规则动作执行日志列表
     *
     * @param query 查询条件 {@link RuleActionExecutionLogPageQuery}
     * @return 规则动作执行日志列表 {@link RuleActionExecutionLogResultVO}
     */
    @Override
    public List<RuleActionExecutionLogResultVO> getRuleActionExecutionLogResultVOList(RuleActionExecutionLogPageQuery query) {
        List<RuleActionExecutionLog> logs = superManager.getRuleActionExecutionLogList(query);
        return BeanPlusUtil.toBeanList(logs, RuleActionExecutionLogResultVO.class);
    }

    @Override
    public boolean removeByRuleExecutionIds(Collection<Long> ruleExecutionIds) {
        if (ruleExecutionIds == null || ruleExecutionIds.isEmpty()) {
            return false;
        }
        return superManager.remove(Wrappers.<RuleActionExecutionLog>lambdaQuery()
                .in(RuleActionExecutionLog::getRuleExecutionId, ruleExecutionIds));
    }
}

