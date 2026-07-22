package com.mqttsnet.thinglinks.service.bridge.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.manager.bridge.BridgeExecutionStepManager;
import com.mqttsnet.thinglinks.service.bridge.BridgeExecutionStepService;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionStepPageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionStepResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 桥接执行步骤明细业务实现。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class BridgeExecutionStepServiceImpl
        extends SuperServiceImpl<BridgeExecutionStepManager, Long, BridgeExecutionStep>
        implements BridgeExecutionStepService {

    @Override
    public List<BridgeExecutionStepResultVO> getStepResultVOList(BridgeExecutionStepPageQuery query) {
        return BeanPlusUtil.copyToList(superManager.getStepList(query), BridgeExecutionStepResultVO.class);
    }

    @Override
    public boolean removeBefore(LocalDateTime cutoff) {
        return superManager.remove(Wrappers.<BridgeExecutionStep>lambdaQuery()
                .lt(BridgeExecutionStep::getStartedAt, cutoff));
    }

    @Override
    public List<BridgeExecutionStep> getStepsByTraceId(String traceId) {
        return superManager.getStepsByTraceId(traceId);
    }

    @Override
    public List<BridgeExecutionStep> getStepsByTraceIdAndRuleId(String traceId, Long ruleId) {
        return superManager.getStepsByTraceIdAndRuleId(traceId, ruleId);
    }
}
