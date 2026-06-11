package com.mqttsnet.thinglinks.bridge.event.publisher;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.bridge.event.BridgeRuleChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.BridgeRuleDeletedEvent;
import com.mqttsnet.thinglinks.bridge.event.DataSourceChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.DataSourceDeletedEvent;
import com.mqttsnet.thinglinks.bridge.event.RuleGroovyScriptChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.RuleGroovyScriptDeletedEvent;
import com.mqttsnet.thinglinks.bridge.event.source.BridgeRuleChangedEventSource;
import com.mqttsnet.thinglinks.bridge.event.source.DataSourceChangedEventSource;
import com.mqttsnet.thinglinks.bridge.event.source.RuleGroovyScriptChangedEventSource;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.entity.script.RuleGroovyScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 桥接领域事件发布器 ── 集中管理事件构造 + publish。对齐 {@code DeviceEventPublisher} 模式。
 *
 * <h3>事件类型</h3>
 * 每个领域(桥接规则 / 数据源 / 规则脚本)各 2 个事件:Changed(create/update/enable/disable)
 * 和 Deleted。Listener 按类型直接注册,不需要解析 operation 字段做路由分支。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BridgeEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    // ============================== 桥接规则 ==============================

    /**
     * 桥接规则变更(create / update / enable / disable)。
     */
    public void publishRuleChangedEvent(DataBridge rule) {
        BridgeRuleChangedEventSource source = BridgeRuleChangedEventSource.builder()
            .ruleId(rule.getId())
            .ruleCode(rule.getRuleCode())
            .appId(rule.getAppId())
            .direction(rule.getDirection())
            .tenantId(ContextUtil.getTenantIdStr())
            .build();
        log.info("[BridgeEventPublisher] rule changed ruleId={} appId={} direction={}",
            source.getRuleId(), source.getAppId(), source.getDirection());
        eventPublisher.publishEvent(new BridgeRuleChangedEvent(source));
    }

    /**
     * 桥接规则删除。
     */
    public void publishRuleDeletedEvent(DataBridge rule) {
        BridgeRuleChangedEventSource source = BridgeRuleChangedEventSource.builder()
            .ruleId(rule.getId())
            .ruleCode(rule.getRuleCode())
            .appId(rule.getAppId())
            .direction(rule.getDirection())
            .tenantId(ContextUtil.getTenantIdStr())
            .build();
        log.info("[BridgeEventPublisher] rule deleted ruleId={} appId={} direction={}",
            source.getRuleId(), source.getAppId(), source.getDirection());
        eventPublisher.publishEvent(new BridgeRuleDeletedEvent(source));
    }

    // ============================== 数据源 ==============================

    /**
     * 数据源变更(create / update / enable / disable)。
     */
    public void publishDataSourceChangedEvent(Long dataSourceId) {
        DataSourceChangedEventSource source = DataSourceChangedEventSource.builder()
            .dataSourceId(dataSourceId)
            .tenantId(ContextUtil.getTenantIdStr())
            .build();
        log.info("[BridgeEventPublisher] dataSource changed dsId={}", source.getDataSourceId());
        eventPublisher.publishEvent(new DataSourceChangedEvent(source));
    }

    /**
     * 数据源删除。
     */
    public void publishDataSourceDeletedEvent(Long dataSourceId) {
        DataSourceChangedEventSource source = DataSourceChangedEventSource.builder()
            .dataSourceId(dataSourceId)
            .tenantId(ContextUtil.getTenantIdStr())
            .build();
        log.info("[BridgeEventPublisher] dataSource deleted dsId={}", source.getDataSourceId());
        eventPublisher.publishEvent(new DataSourceDeletedEvent(source));
    }

    // ============================== 规则脚本 ==============================

    /**
     * 规则脚本变更(create / update / enable / disable)。
     */
    public void publishGroovyScriptChangedEvent(RuleGroovyScript script) {
        RuleGroovyScriptChangedEventSource source = buildScriptEventSource(script);
        log.info("[BridgeEventPublisher] groovy script changed scriptId={} scriptType={}", source.getScriptId(), source.getScriptType());
        eventPublisher.publishEvent(new RuleGroovyScriptChangedEvent(source));
    }

    /**
     * 规则脚本删除。
     */
    public void publishGroovyScriptDeletedEvent(RuleGroovyScript script) {
        RuleGroovyScriptChangedEventSource source = buildScriptEventSource(script);
        log.info("[BridgeEventPublisher] groovy script deleted scriptId={} scriptType={}",
            source.getScriptId(), source.getScriptType());
        eventPublisher.publishEvent(new RuleGroovyScriptDeletedEvent(source));
    }

    /**
     * 构造脚本事件源 ── 4 个 identity 字段是 cache key 身份,Listener 用它们重建
     * {@code GroovyScriptCacheKeyBuilder} 缓存键(尤其 Deleted 事件 DB 行已删除时)。
     */
    private RuleGroovyScriptChangedEventSource buildScriptEventSource(RuleGroovyScript script) {
        return RuleGroovyScriptChangedEventSource.builder()
            .scriptId(script.getId())
            .tenantId(ContextUtil.getTenantIdStr())
            .scriptType(script.getScriptType())
            .channelCode(script.getChannelCode())
            .productIdentification(script.getProductIdentification())
            .topicPattern(script.getTopicPattern())
            .objectVersion(script.getObjectVersion())
            .build();
    }
}
