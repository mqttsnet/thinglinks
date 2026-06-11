package com.mqttsnet.thinglinks.mapper.bridge;

import com.mqttsnet.basic.base.mapper.SuperMapper;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * 桥接执行 trace 主表（链路回放用）
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Repository
public interface BridgeExecutionTraceMapper extends SuperMapper<BridgeExecutionTrace> {
}
