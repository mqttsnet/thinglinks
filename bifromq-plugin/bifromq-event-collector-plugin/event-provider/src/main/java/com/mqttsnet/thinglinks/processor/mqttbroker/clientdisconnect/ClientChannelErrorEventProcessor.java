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

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.ClientChannelError;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link ClientChannelError} 事件处理器 ── 客户端 TCP 通道异常(连接重置 / IO 错误),broker 被动断开.
 *
 * <h3>BifroMQ 字段(3.3.5 / 4.0 一致)</h3>
 * <ul>
 *   <li>{@code Throwable cause} ── channel 异常根因(网络重置 / IO 错误 / Netty 异常等)</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 1.1
 */
public class ClientChannelErrorEventProcessor extends AbstractClientEventProcessor<ClientChannelError> {

    @Override
    protected Class<ClientChannelError> eventClass() {
        return ClientChannelError.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, ClientChannelError event) {
        // 4.0 兼容:cause 字段类型 java.lang.Throwable,3.3.5/4.0 一致.
        Optional.ofNullable(event.cause())
            .map(Throwable::getMessage)
            .ifPresent(msg -> data.put("cause", msg));
    }
}
