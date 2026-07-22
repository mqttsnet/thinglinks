package com.mqttsnet.thinglinks.rule.facade.impl;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.thinglinks.bridge.scheduler.BridgeMaintenanceScheduler;
import com.mqttsnet.thinglinks.rule.facade.RuleJobHandlerFacade;
import com.mqttsnet.thinglinks.service.linkage.RuleService;
import com.mqttsnet.thinglinks.service.script.RuleGroovyScriptService;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.result.linkage.RuleDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author tangyh
 * @since 2024/12/24 21:07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleJobHandlerFacadeImpl implements RuleJobHandlerFacade {
    private final RuleService ruleService;

    private final RuleGroovyScriptService ruleGroovyScriptService;

    private final BridgeMaintenanceScheduler bridgeMaintenanceScheduler;

    @Override
    public R<RuleDetailsResultVO> triggerRulePolicy(Long tenantId, String ruleIdentification) {

        ArgumentAssert.notNull(tenantId, "tenantId  Cannot be null");
        ArgumentAssert.notEmpty(ruleIdentification, "ruleIdentification Cannot be null");
        log.info("Trigger Rule Policy - Tenant ID: {}, Rule Identification: {}", tenantId, ruleIdentification);
        try {
            ContextUtil.setTenantId(tenantId);
            RuleDetailsResultVO ruleDetailsResultVO = ruleService.triggerRulePolicy(tenantId, ruleIdentification);
            log.info("Successfully triggered rule policy - Tenant ID: {}, Rule Identification: {}, Result: {}", tenantId, ruleIdentification, ruleDetailsResultVO);
            return R.success(ruleDetailsResultVO);
        } catch (BizException bizException) {
            log.warn("Business exception while triggering rule policy - Tenant ID: {}, Rule Identification: {}, Message: {}", tenantId, ruleIdentification, bizException.getMessage(), bizException);
            return R.fail("Business error triggering rule policy: " + bizException.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while triggering rule policy - Tenant ID: {}, Rule Identification: {}", tenantId, ruleIdentification, e);
            return R.fail("Unexpected error triggering rule policy: " + e.getMessage());
        }

    }

    @Override
    public R<Boolean> flushGroovyScriptCache() {
        try {
            return R.success(ruleGroovyScriptService.flushGroovyScriptCache());
        } catch (BizException bizException) {
            log.warn("Business exception while flushing groovy script cache: ", bizException);
            return R.fail(bizException);
        } catch (Exception e) {
            log.error("Unexpected error while flushing groovy script cache: ", e);
            return R.fail("Unexpected error flushing groovy script cache: " + e.getMessage());
        }
    }

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
    public R<Boolean> runBridgeHealthCheck() {
        try {
            bridgeMaintenanceScheduler.runHealthCheck(true);
            return R.success(true);
        } catch (Exception e) {
            log.error("Bridge health check failed", e);
            return R.fail("Bridge health check failed: " + e.getMessage());
        }
    }

    @Override
    public R<Boolean> runBridgeTraceCleanup(Integer retentionDays) {
        try {
            bridgeMaintenanceScheduler.cleanupOldTraces(retentionDays);
            return R.success(true);
        } catch (Exception e) {
            log.error("Bridge trace cleanup failed", e);
            return R.fail("Bridge trace cleanup failed: " + e.getMessage());
        }
    }
}
