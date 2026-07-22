/*
 * Copyright (c) 2024. The BifroMQ Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.mqttsnet.thinglinks.processor.mqttbroker;

import java.util.Map;

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.PingReq;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link PingReq} 事件处理器 ── 客户端发送 MQTT PINGREQ 心跳包.
 * <p>下游 {@code DeviceHeartbeatStage} 据此刷新 {@code device.last_heartbeat_time}
 * + 触发 60s 节流 reconcile 自愈状态.
 *
 * @author mqttsnet
 * @since 1.1
 */
public class PingReqEventProcessor extends AbstractClientEventProcessor<PingReq> {

    @Override
    protected Class<PingReq> eventClass() {
        return PingReq.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, PingReq event) {
        data.put("pong", event.pong());
    }
}
