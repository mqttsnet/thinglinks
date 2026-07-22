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
import java.util.Optional;

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.BadPacket;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link BadPacket} 事件处理器 ── 客户端发了无法解析的 MQTT 包,broker 主动断连.
 *
 * <h3>BifroMQ 字段(3.3.5 / 4.0 一致)</h3>
 * <ul>
 *   <li>{@code Throwable cause} ── 解析失败的根因异常</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 1.1
 */
public class BadPacketEventProcessor extends AbstractClientEventProcessor<BadPacket> {

    @Override
    protected Class<BadPacket> eventClass() {
        return BadPacket.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, BadPacket event) {
        // 4.0 兼容:cause 字段类型 java.lang.Throwable,3.3.5/4.0 一致.
        Optional.ofNullable(event.cause())
            .map(Throwable::getMessage)
            .ifPresent(msg -> data.put("cause", msg));
    }
}
