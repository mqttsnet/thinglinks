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

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.ByServer;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link ByServer} 事件处理器 ── 服务器侧业务逻辑主动断开客户端(管理 / 配置触发,非协议错误).
 *
 * @author mqttsnet
 * @since 1.1
 */
public class ByServerEventProcessor extends AbstractClientEventProcessor<ByServer> {

    @Override
    protected Class<ByServer> eventClass() {
        return ByServer.class;
    }
}
