package com.mqttsnet.thinglinks.bus.hook;

import com.mqttsnet.thinglinks.dto.bus.DeviceEventOutcome;
import com.mqttsnet.thinglinks.entity.protocol.DeviceProtocolEvent;

/**
 * Pipeline 完成回调 SPI,主同步链路末尾调用(POST 异步 stage 此时可能还在跑)。
 * 典型用途:trace 落库 / 全链路指标 / 业务挂钩。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public interface DeviceEventCallback {

    void onComplete(DeviceProtocolEvent event, DeviceEventOutcome outcome);
}
