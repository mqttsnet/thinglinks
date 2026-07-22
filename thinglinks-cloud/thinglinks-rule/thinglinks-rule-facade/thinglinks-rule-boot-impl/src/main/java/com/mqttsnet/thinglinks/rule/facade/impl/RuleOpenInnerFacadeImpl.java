package com.mqttsnet.thinglinks.rule.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.rule.facade.RuleOpenInnerFacade;
import com.mqttsnet.thinglinks.service.script.RuleGroovyScriptService;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author mqttsnet
 * @date 2025/4/15 15:03
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleOpenInnerFacadeImpl implements RuleOpenInnerFacade {

    private final RuleGroovyScriptService ruleGroovyScriptService;

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param) {
        try {
            return R.success(ruleGroovyScriptService.executeScript(param));
        } catch (BizException bizException) {
            log.warn("Business exception while executing script: ", bizException);
            return R.fail(bizException);
        } catch (Exception e) {
            log.error("Unexpected error while executing script: ", e);
            return R.fail("Unexpected error executing script: " + e.getMessage());
        }
    }

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScriptContent(RuleGroovyScriptDirectCompileParam param) {
        try {
            return R.success(ruleGroovyScriptService.runDirectCompile(param));
        } catch (BizException bizException) {
            log.warn("Business exception while executing script content: ", bizException);
            return R.fail(bizException);
        } catch (Exception e) {
            log.error("Unexpected error while executing script content: ", e);
            return R.fail("Unexpected error executing script content: " + e.getMessage());
        }
    }
}
