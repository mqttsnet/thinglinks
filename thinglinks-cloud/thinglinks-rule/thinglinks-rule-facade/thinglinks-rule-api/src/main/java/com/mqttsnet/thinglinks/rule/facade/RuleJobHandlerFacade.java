package com.mqttsnet.thinglinks.rule.facade;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;

/**
 * rule 服务调度任务远程接口
 *
 * @author xiaonannet
 */
public interface RuleJobHandlerFacade {


    /**
     * Triggers the policy of a specific rule identified by the tenant ID and the rule identification string.
     * <p>
     * This method is responsible for initiating the execution of a rule's policy for a given tenant.
     * It requires the tenant's ID and the rule's unique identification string. The method returns the details
     * of the rule after the policy has been triggered, which may include updated status or configuration information.
     * </p>
     *
     * @param tenantId           The ID of the tenant.
     * @param ruleIdentification The unique identification string of the rule.
     * @return {@link RuleDetailsResultVO} containing the updated details of the rule after triggering its policy.
     */
    R<RuleDetailsResultVO> triggerRulePolicy(Long tenantId, String ruleIdentification);


    /**
     * 执行全租户Groovy脚本缓存刷新操作
     * 此方法将遍历所有租户上下文，异步刷新每个租户的规则脚本缓存。
     * 适用于系统维护或缓存失效后的全局刷新场景。
     */
    R<Boolean> flushGroovyScriptCache();


    /**
     * 直接执行Groovy脚本
     *
     * @param param Groovy脚本执行参数
     * @return {@link R<GroovyScriptEngineExecutorResultVO>} 包含执行结果的响应对象
     */
    R<GroovyScriptEngineExecutorResultVO> executeScript(RuleGroovyScriptExecuteScriptParam param);

    /**
     * 触发桥接数据源健康检查。
     * <p>对所有 enable=true 的数据源调 testConnection，更新 health_status 字段。
     *
     * @return {@code R<Boolean>} 执行成功返回 true
     */
    R<Boolean> runBridgeHealthCheck();

    /**
     * 触发桥接 trace 历史数据清理。
     * <p>删除超过保留期的 bridge_execution_trace + bridge_execution_step 记录。
     *
     * @param retentionDays 保留天数；null 时使用默认 90 天
     * @return {@code R<Boolean>} 执行成功返回 true
     */
    R<Boolean> runBridgeTraceCleanup(Integer retentionDays);

}
