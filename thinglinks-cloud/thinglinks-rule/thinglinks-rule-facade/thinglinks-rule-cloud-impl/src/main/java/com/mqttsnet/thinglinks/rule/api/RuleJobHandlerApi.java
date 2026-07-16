package com.mqttsnet.thinglinks.rule.api;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.rule.api.hystrix.RuleJobHandlerApiFallback;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * rule 服务调度任务远程接口
 *
 * @author xiaonannet
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-rule-server}", fallback = RuleJobHandlerApiFallback.class, path = "/")
public interface RuleJobHandlerApi {


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
    @Operation(summary = "触发规则策略", description = "Triggers the policy of a specific rule for a given tenant.")
    @Parameters({
            @Parameter(name = "tenantId", description = "Tenant ID", required = true),
            @Parameter(name = "ruleIdentification", description = "Rule Identification", required = true),
    })
    @PostMapping("/inner/ruleOpen/triggerRulePolicy")
    R<RuleDetailsResultVO> triggerRulePolicy(@RequestParam("tenantId") Long tenantId, @RequestParam("ruleIdentification") String ruleIdentification);


    /**
     * 刷新Groovy脚本缓存
     *
     * @return {@link R<Boolean>} 包含刷新结果的布尔值
     */
    @Operation(summary = "刷新Groovy脚本缓存", description = "Flushes the Groovy script cache")
    @GetMapping("/flushGroovyScriptCache")
    R<Boolean> flushGroovyScriptCache();


    /**
     * 直接编译执行脚本
     * <p>实时执行以下流程：</p>
     * <ol>
     *   <li>动态编译传入的脚本内容为Class</li>
     *   <li>绑定执行参数到脚本上下文</li>
     *   <li>执行脚本逻辑并返回结果</li>
     * </ol>
     *
     * @param param 包含脚本内容与执行参数的请求体
     * @return {@link R< GroovyScriptEngineExecutorResultVO >} 标准化执行结果包装
     */
    @Operation(summary = "直接编译脚本", description = "实时编译脚本内容并执行，适用于动态脚本编译场景")
    @PostMapping("/executeScript")
    R<GroovyScriptEngineExecutorResultVO> executeScript(@RequestBody RuleGroovyScriptExecuteScriptParam param);


    /**
     * 桥接数据源健康检查。
     *
     * @return {@code R<Boolean>} 执行成功返回 true
     */
    @Operation(summary = "运行桥接数据源健康检查",
            description = "扫描所有启用数据源调 testConnection 更新 health_status")
    @PostMapping("/inner/ruleOpen/runBridgeHealthCheck")
    R<Boolean> runBridgeHealthCheck();

    /**
     * 桥接 trace 清理。
     *
     * @param retentionDays 保留天数（可选，默认 90 天）
     * @return {@code R<Boolean>} 执行成功返回 true
     */
    @Operation(summary = "运行桥接 trace 历史清理",
            description = "删除超过保留期的 bridge_execution_trace + step")
    @PostMapping("/inner/ruleOpen/runBridgeTraceCleanup")
    R<Boolean> runBridgeTraceCleanup(@RequestParam(value = "retentionDays", required = false) Integer retentionDays);

}
