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

package com.mqttsnet.thinglinks.processor.session;

import java.util.Map;

import com.baidu.bifromq.plugin.eventcollector.session.MQTTSessionStart;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link MQTTSessionStart} 事件处理器 ── MQTT session(transient / persistent)创建审计.
 *
 * @author mqttsnet
 * @since 1.1
 */
public class MqttSessionStartEventProcessor extends AbstractClientEventProcessor<MQTTSessionStart> {

    @Override
    protected Class<MQTTSessionStart> eventClass() {
        return MQTTSessionStart.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, MQTTSessionStart event) {
        data.put("sessionId", event.sessionId());
    }
}
