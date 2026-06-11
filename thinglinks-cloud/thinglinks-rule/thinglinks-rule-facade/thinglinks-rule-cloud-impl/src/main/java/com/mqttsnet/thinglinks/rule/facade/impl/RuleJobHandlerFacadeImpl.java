package com.mqttsnet.thinglinks.rule.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.rule.api.RuleJobHandlerApi;
import com.mqttsnet.thinglinks.rule.facade.RuleJobHandlerFacade;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author tangyh
 * @since 2024/12/24 21:07
 */
@Slf4j
@Service
public class RuleJobHandlerFacadeImpl implements RuleJobHandlerFacade {

    @Autowired
    @Lazy
    private RuleJobHandlerApi ruleJobHandlerApi;

    @Override
    public R<RuleDetailsResultVO> triggerRulePolicy(Long tenantId, String ruleIdentification) {
        return ruleJobHandlerApi.triggerRulePolicy(tenantId, ruleIdentification);
    }

    @Override
    public R<Boolean> flushGroovyScriptCache() {
        return ruleJobHandlerApi.flushGroovyScriptCache();
    }

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param) {
        return ruleJobHandlerApi.executeScript(param);
    }

    @Override
    public R<Boolean> runBridgeHealthCheck() {
        return ruleJobHandlerApi.runBridgeHealthCheck();
    }

    @Override
    public R<Boolean> runBridgeTraceCleanup(Integer retentionDays) {
        return ruleJobHandlerApi.runBridgeTraceCleanup(retentionDays);
    }
}
