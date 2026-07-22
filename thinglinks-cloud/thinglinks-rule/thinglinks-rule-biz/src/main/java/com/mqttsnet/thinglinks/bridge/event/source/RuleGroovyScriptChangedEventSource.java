package com.mqttsnet.thinglinks.bridge.event.source;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 规则脚本变更/删除事件源(共用 payload)── {@code RuleGroovyScriptChangedEvent}/
 * {@code RuleGroovyScriptDeletedEvent} 承载。
 * <p>
 * 4 个 identity 字段(scriptType ~ topicPattern)是脚本的<b>缓存键身份</b>;
 * Listener 需要它们调用 {@code ScriptIdentifier.buildCacheKey} 重建 {@code GroovyScriptCacheKeyBuilder}
 * 缓存键 ── Deleted 事件 DB 行已删除,只能靠这 4 字段重建键定位旧缓存。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleGroovyScriptChangedEventSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 脚本主键 ID
     */
    private Long scriptId;

    /**
     * 租户 ID
     */
    private String tenantId;

    /**
     * 脚本类型(cache key 身份字段 1/4)
     */
    private String scriptType;

    /**
     * 渠道编码(cache key 身份字段 2/4)
     */
    private String channelCode;

    /**
     * 产品标识(cache key 身份字段 3/4)
     */
    private String productIdentification;

    /**
     * 主题模式(cache key 身份字段 4/4)
     */
    private String topicPattern;

    /**
     * 产品发布版本号 ── 设备上行前置转换脚本的 HASH 桶维度(产品 + 版本)。
     * 变更/删除事件据此 + channelCode + productIdentification 重建桶 KEY 整桶刷新。
     */
    private String objectVersion;
}
