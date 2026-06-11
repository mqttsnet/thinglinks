package com.mqttsnet.thinglinks.rule.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;

/**
 * ============================================================================
 * Description:
 * Rule 所有用户开放API
 * ============================================================================
 *
 * @author Sun Shihuan
 * @version 1.0.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2025/4/14      Sun Shihuan        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2025/4/14 14:56
 */
public interface RuleOpenAnyUserFacade {
    /**
     * 直接执行Groovy脚本
     *
     * @param param Groovy脚本执行参数
     * @return {@link R < GroovyScriptEngineExecutorResultVO >} 包含执行结果的响应对象
     */
    R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param);

    /**
     * 直接按脚本内容执行 Groovy(不经缓存 KEY 加载)── 供设备上行前置转换链路:
     * mqs 命中 topic 模式后拿到脚本内容,直接执行产出平台标准 datas 报文。
     *
     * @param param 含 scriptContent(脚本内容)+ executeParams(执行参数 JSON)
     * @return {@link R}&lt;{@link GroovyScriptEngineExecutorResultVO}&gt; 执行结果(context 为转换产物)
     */
    R<GroovyScriptEngineExecutorResultVO> executeScriptContent(RuleGroovyScriptDirectCompileParam param);

}
