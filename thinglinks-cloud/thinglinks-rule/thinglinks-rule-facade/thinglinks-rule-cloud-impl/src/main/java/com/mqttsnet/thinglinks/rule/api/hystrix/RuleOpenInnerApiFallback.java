package com.mqttsnet.thinglinks.rule.api.hystrix;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.rule.api.RuleOpenInnerApi;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Description:
 * <p>
 * Rule AnyUser API熔断
 * ============================================================================
 *
 * @author Sun Shihuan
 * @version 1.0.0
 * @email
 * @date 2025/4/15 15:01
 */
@Component
public class RuleOpenInnerApiFallback implements RuleOpenInnerApi {

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param) {
        return R.timeout();
    }

    @Override
    public R<GroovyScriptEngineExecutorResultVO> executeScriptContent(RuleGroovyScriptDirectCompileParam param) {
        return R.timeout();
    }
}
