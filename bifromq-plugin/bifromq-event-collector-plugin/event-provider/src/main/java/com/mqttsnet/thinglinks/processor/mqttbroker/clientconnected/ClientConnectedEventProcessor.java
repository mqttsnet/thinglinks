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

package com.mqttsnet.thinglinks.processor.mqttbroker.clientconnected;

import java.util.Map;
import java.util.Optional;

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientconnected.ClientConnected;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link ClientConnected} 事件处理器 ── 客户端成功完成 MQTT CONNECT 握手(认证通过 + session 建立).
 *
 * @author mqttsnet
 * @since 1.1
 */
public class ClientConnectedEventProcessor extends AbstractClientEventProcessor<ClientConnected> {

    @Override
    protected Class<ClientConnected> eventClass() {
        return ClientConnected.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, ClientConnected event) {
        data.put("serverId", Optional.ofNullable(event.serverId()).orElse(""));
        data.put("userSessionId", Optional.ofNullable(event.userSessionId()).orElse(""));
        data.put("cleanSession", event.cleanSession());
        data.put("sessionPresent", event.sessionPresent());
        data.put("keepAliveTimeSeconds", event.keepAliveTimeSeconds());
        Optional.ofNullable(event.lastWill()).ifPresent(lastWill -> {
            data.put("lastWillTopic", Optional.ofNullable(lastWill.topic()).orElse(""));
            data.put("lastWillQos", Optional.ofNullable(lastWill.qos()).map(q -> q.getNumber()).orElse(0));
            data.put("lastWillRetain", lastWill.isRetain());
            data.put("lastWillPayload", Optional.ofNullable(lastWill.payload()).map(Object::toString).orElse(""));
        });
    }
}
