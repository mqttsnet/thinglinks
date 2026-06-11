package com.mqttsnet.thinglinks.mqs.bus.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.thinglinks.common.mq.KafkaConsumerTopicConstant;
import com.mqttsnet.thinglinks.dto.bus.DeviceEventOutcome;
import com.mqttsnet.thinglinks.mqs.bus.dispatcher.BusPipelineDispatcher;
import com.mqttsnet.thinglinks.mqs.bus.dispatcher.SourceTopicHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 协议总线测试接口,手动触发 dispatcher 跑一遍 pipeline,联调 / 排错用.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/mqs/bus/test")
@RequiredArgsConstructor
@Tag(name = "协议总线-测试", description = "手动触发 dispatcher,用于联调 / 排错")
@Slf4j
public class DeviceBusTestController {

    private final BusPipelineDispatcher dispatcher;

    @Operation(summary = "手动触发 dispatcher",
        description = "传 sourceTopic + raw JSON,经过 adapter → dispatcher → stages 全流程")
    @PostMapping("/dispatch")
    public R<DeviceEventOutcome> dispatch(
        @Parameter(description = "来源 topic(走 @TopicRoute 匹配 adapter)", required = true)
        @RequestParam String sourceTopic,
        @Parameter(description = "原始报文 JSON", required = true)
        @RequestBody String rawJson) {
        if (StrUtil.isBlank(sourceTopic)) {
            return R.fail("sourceTopic 不能为空");
        }
        if (StrUtil.isBlank(rawJson)) {
            return R.fail("rawJson 不能为空");
        }
        return doDispatch(sourceTopic, rawJson);
    }

    @Operation(summary = "MQTT PUBLISH 简化测试 ── 自动用主数据 topic")
    @PostMapping("/dispatch/mqtt-publish")
    public R<DeviceEventOutcome> dispatchMqttPublish(@RequestBody String rawJson) {
        if (StrUtil.isBlank(rawJson)) {
            return R.fail("rawJson 不能为空");
        }
        return doDispatch(KafkaConsumerTopicConstant.Mqs.MqsMqtt.THINGLINKS_MQTT_DISTRIBUTION_COMPLETED_TOPIC, rawJson);
    }

    /**
     * dispatch 公共骨架,模拟 Kafka consumer 入口:set context → dispatch → finally clear。
     */
    private R<DeviceEventOutcome> doDispatch(String sourceTopic, String rawJson) {
        try {
            // 测试场景没有 BifroMQ X-Trace-Id header,单点兜底生成
            if (StrUtil.isBlank(ContextUtil.getLogTraceId())) {
                ContextUtil.setLogTraceId(IdUtil.fastSimpleUUID());
            }
            SourceTopicHolder.set(sourceTopic);
            return R.success(dispatcher.dispatch(sourceTopic, rawJson));
        } finally {
            SourceTopicHolder.clear();
            ContextUtil.remove();
        }
    }
}
