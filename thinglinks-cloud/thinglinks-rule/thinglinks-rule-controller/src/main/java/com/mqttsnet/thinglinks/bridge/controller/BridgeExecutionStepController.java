package com.mqttsnet.thinglinks.bridge.controller;

import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.bridge.BridgeExecutionStep;
import com.mqttsnet.thinglinks.service.bridge.BridgeExecutionStepService;
import com.mqttsnet.thinglinks.vo.query.bridge.BridgeExecutionStepPageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.BridgeExecutionStepResultVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 桥接执行步骤明细控制器(主要给监控告警按状态查异常步骤)。
 * 常规链路回放走 BridgeExecutionTraceController.getDetail() 附带的 steps 子集合。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/bridgeExecutionStep")
@Tag(name = "桥接执行步骤明细")
public class BridgeExecutionStepController extends SuperController<BridgeExecutionStepService, Long, BridgeExecutionStep,
        Object, Object, BridgeExecutionStepPageQuery, BridgeExecutionStepResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<BridgeExecutionStep> handlerWrapper(BridgeExecutionStep model,
                                                        PageParams<BridgeExecutionStepPageQuery> params) {
        QueryWrap<BridgeExecutionStep> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("rule_bridge_execution_step");
        return queryWrap;
    }
}
