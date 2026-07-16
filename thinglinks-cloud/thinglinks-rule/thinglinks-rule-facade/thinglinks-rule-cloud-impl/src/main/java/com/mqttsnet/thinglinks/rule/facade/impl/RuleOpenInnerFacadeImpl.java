package com.mqttsnet.thinglinks.rule.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.rule.api.RuleOpenInnerApi;
import com.mqttsnet.thinglinks.rule.facade.RuleOpenInnerFacade;
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
@Service("RuleOpenInnerFacade")
public class RuleOpenInnerFacadeImpl implements RuleOpenInnerFacade {

    @Autowired
    @Lazy
    private RuleOpenInnerApi ruleOpenInnerApi;

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param) {
        return ruleOpenInnerApi.executeScript(param);
    }

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScriptContent(RuleGroovyScriptDirectCompileParam param) {
        return ruleOpenInnerApi.executeScriptContent(param);
    }
}
