package com.mqttsnet.thinglinks.manager.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionTrace;
import com.mqttsnet.thinglinks.manager.bridge.BridgeExecutionTraceManager;
import com.mqttsnet.thinglinks.mapper.bridge.BridgeExecutionTraceMapper;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionTracePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 桥接执行 trace
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class BridgeExecutionTraceManagerImpl extends SuperManagerImpl<BridgeExecutionTraceMapper, BridgeExecutionTrace> implements BridgeExecutionTraceManager {

    private final BridgeExecutionTraceMapper traceMapper;

    @Override
    public List<BridgeExecutionTrace> getTraceList(BridgeExecutionTracePageQuery query) {
        QueryWrap<BridgeExecutionTrace> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(StrUtil.isNotBlank(query.getTraceId()), BridgeExecutionTrace::getTraceId, query.getTraceId())
                .eq(query.getBridgeRuleId() != null, BridgeExecutionTrace::getBridgeRuleId, query.getBridgeRuleId())
                .eq(StrUtil.isNotBlank(query.getDirection()), BridgeExecutionTrace::getDirection, query.getDirection())
                .eq(StrUtil.isNotBlank(query.getTriggerSource()), BridgeExecutionTrace::getTriggerSource, query.getTriggerSource())
                .eq(StrUtil.isNotBlank(query.getTenantId()), BridgeExecutionTrace::getTenantId, query.getTenantId())
                .eq(StrUtil.isNotBlank(query.getProductIdentification()), BridgeExecutionTrace::getProductIdentification, query.getProductIdentification())
                .eq(StrUtil.isNotBlank(query.getDeviceIdentification()), BridgeExecutionTrace::getDeviceIdentification, query.getDeviceIdentification())
                .eq(StrUtil.isNotBlank(query.getActionType()), BridgeExecutionTrace::getActionType, query.getActionType())
                .eq(StrUtil.isNotBlank(query.getStatus()), BridgeExecutionTrace::getStatus, query.getStatus())
                .ge(query.getStartTimeBegin() != null, BridgeExecutionTrace::getStartTime, query.getStartTimeBegin())
                .le(query.getStartTimeEnd() != null, BridgeExecutionTrace::getStartTime, query.getStartTimeEnd())
                .orderByDesc(BridgeExecutionTrace::getStartTime);
        return traceMapper.selectList(wrap);
    }

    @Override
    public BridgeExecutionTrace getByTraceId(String traceId) {
        if (StrUtil.isBlank(traceId)) {
            return null;
        }
        QueryWrap<BridgeExecutionTrace> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(BridgeExecutionTrace::getTraceId, traceId)
                .orderByDesc(BridgeExecutionTrace::getId)
                .last("LIMIT 1");
        return traceMapper.selectOne(wrap);
    }

    @Override
    public BridgeExecutionTrace getByTraceIdAndRuleId(String traceId, Long ruleId) {
        if (StrUtil.isBlank(traceId) || ruleId == null) {
            return null;
        }
        QueryWrap<BridgeExecutionTrace> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(BridgeExecutionTrace::getTraceId, traceId)
                .eq(BridgeExecutionTrace::getBridgeRuleId, ruleId);
        return traceMapper.selectOne(wrap);
    }
}
