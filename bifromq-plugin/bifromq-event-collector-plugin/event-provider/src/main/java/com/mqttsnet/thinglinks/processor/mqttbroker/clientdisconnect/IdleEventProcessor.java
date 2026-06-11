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

package com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect;

import java.util.Map;

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.Idle;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link Idle} 事件处理器 ── 客户端 KeepAlive 超时(1.5× keepAliveTimeSeconds 无任何活动),broker 主动断开.
 * <p>设备真实掉线场景最常见来源,下游 {@code device.connect_status} 必须能据此切到 OFFLINE.
 *
 * @author mqttsnet
 * @since 1.1
 */
public class IdleEventProcessor extends AbstractClientEventProcessor<Idle> {

    @Override
    protected Class<Idle> eventClass() {
        return Idle.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, Idle event) {
        data.put("keepAliveTimeSeconds", event.keepAliveTimeSeconds());
    }
}
