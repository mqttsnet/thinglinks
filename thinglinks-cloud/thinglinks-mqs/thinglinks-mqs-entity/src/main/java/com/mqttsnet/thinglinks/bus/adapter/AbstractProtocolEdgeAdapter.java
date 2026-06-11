package com.mqttsnet.thinglinks.bus.adapter;

import java.util.HashMap;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 协议适配器基类(模板方法),封装 traceId 解析 / 异常包装 / 入站时间打点。
 * 子类仅关心 {@link #doCanonicalize} 协议特异归一化。
 *
 * <p>traceId 唯一来源:Kafka consumer 入口从 BifroMQ {@code X-Trace-Id} header 设进
 * {@link ContextUtil#getLogTraceId()}。<b>不再 fallback {@code IdUtil} 自生</b>,避免上下游断链。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@Slf4j
public abstract class AbstractProtocolEdgeAdapter implements ProtocolEdgeAdapter {

    @Override
    public final DeviceProtocolEvent canonicalize(Object rawSource) {
        DeviceProtocolEvent event = doCanonicalize(rawSource);
        if (event == null) {
            throw new IllegalStateException("[bus.adapter] " + supports().getValue()
                + " doCanonicalize returned null, raw=" + StrUtil.maxLength(String.valueOf(rawSource), 200));
        }
        ensureBasic(event);
        return event;
    }

    /**
     * 子类实现协议归一化,不可返 null。
     */
    protected abstract DeviceProtocolEvent doCanonicalize(Object rawSource);

    /**
     * 兜底必填字段:traceId / 协议类型 / 入站时间戳 / 扩展 Map。
     */
    protected void ensureBasic(DeviceProtocolEvent event) {
        if (StrUtil.isBlank(event.getTraceId())) {
            Optional.ofNullable(ContextUtil.getLogTraceId())
                .filter(StrUtil::isNotBlank)
                .ifPresentOrElse(event::setTraceId,
                    () -> log.warn("[bus.adapter] traceId missing for protocol={} "
                        + "(BifroMQ X-Trace-Id header not set?)", supports().getValue()));
        }
        if (StrUtil.isBlank(event.getProtocolType())) {
            event.setProtocolType(supports().getValue());
        }
        if (event.getTs() == null || event.getTs() <= 0L) {
            event.setTs(System.currentTimeMillis());
        }
        if (event.getExtension() == null) {
            event.setExtension(new HashMap<>());
        }
    }
}
