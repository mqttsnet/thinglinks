package com.mqttsnet.thinglinks.rule.api.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.rule.api.RuleJobHandlerApi;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks-cloud
 * @description: RuleJobHandlerApi API熔断
 * @packagename: com.mqttsnet.thinglinks.rule.api.hystrix
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2024-07-22 12:37
 **/
@Component
public class RuleJobHandlerApiFallback implements RuleJobHandlerApi {

    @Override
    public R<RuleDetailsResultVO> triggerRulePolicy(Long tenantId, String ruleIdentification) {
        return R.timeout();
    }

    @Override
    public R<Boolean> flushGroovyScriptCache() {
        return R.timeout();
    }

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param) {
        return R.timeout();
    }

    @Override
    public R<Boolean> runBridgeHealthCheck() {
        return R.timeout();
    }

    @Override
    public R<Boolean> runBridgeTraceCleanup(Integer retentionDays) {
        return R.timeout();
    }
}
