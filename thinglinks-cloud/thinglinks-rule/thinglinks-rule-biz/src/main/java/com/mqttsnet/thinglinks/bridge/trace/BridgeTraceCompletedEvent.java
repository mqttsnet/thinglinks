package com.mqttsnet.thinglinks.bridge.trace;

import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 桥接 trace 完成事件,listener 端 {@code @Async("ruleBridgeLogExecutor")} 异步落库。
 *
 * <p>构造时快照 ContextUtil.LocalMap,worker 端无论 ttl wrapper 是否生效都能恢复上下文,
 * 保证 {@code @DS(BASE_TENANT)} 切库正常。
 *
 * @author mqttsnet
 * @since 2026-04-28
 */
@Getter
public class BridgeTraceCompletedEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private final BridgeExecutionTrace trace;
    private final List<BridgeExecutionStep> steps;

    /**
     * 主线程 LocalMap 不可变快照(含 tenantId / tenantBasePoolName / traceId / userId 等)。
     */
    private final Map<String, String> contextSnapshot;

    public BridgeTraceCompletedEvent(Object source,
                                     BridgeExecutionTrace trace,
                                     List<BridgeExecutionStep> steps) {
        super(source);
        this.trace = trace;
        this.steps = steps;
        this.contextSnapshot = Optional.ofNullable(ContextUtil.getLocalMap())
                .<Map<String, String>>map(HashMap::new)
                .orElseGet(HashMap::new);
    }
}
