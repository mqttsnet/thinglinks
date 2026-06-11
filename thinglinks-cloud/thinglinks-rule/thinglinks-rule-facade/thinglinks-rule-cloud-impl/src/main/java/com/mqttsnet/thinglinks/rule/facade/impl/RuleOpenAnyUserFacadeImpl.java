package com.mqttsnet.thinglinks.rule.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.rule.api.RuleOpenAnyUserApi;
import com.mqttsnet.thinglinks.rule.facade.RuleOpenAnyUserFacade;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * @author mqttsnet
 * @date 2025/4/15 15:03
 */
@Slf4j
@Service("RuleOpenAnyUserFacade")
public class RuleOpenAnyUserFacadeImpl implements RuleOpenAnyUserFacade {

    @Autowired
    @Lazy
    private RuleOpenAnyUserApi ruleOpenAnyUserApi;

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param) {
        return ruleOpenAnyUserApi.executeScript(param);
    }

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScriptContent(RuleGroovyScriptDirectCompileParam param) {
        return ruleOpenAnyUserApi.executeScriptContent(param);
    }
}
