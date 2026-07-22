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

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.InboxTransientError;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link InboxTransientError} 事件处理器 ── inbox 服务暂时错误(临时性,可重连恢复).
 *
 * <h3>BifroMQ 字段差异(3.3.5 vs 4.0)</h3>
 * <ul>
 *   <li>3.3.5:无特异字段</li>
 *   <li>4.0 新增:{@code String reason} ── inbox 暂时错误原因(便于排查)</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 1.1
 */
public class InboxTransientErrorEventProcessor extends AbstractClientEventProcessor<InboxTransientError> {

    // TODO 4.0 升级时补 reason 字段(BifroMQ 4.0 新增):
    //   @Override
    //   protected void enrichSubtype(Map<String, Object> data, InboxTransientError event) {
    //       data.put("reason", event.reason());
    //   }

    @Override
    protected Class<InboxTransientError> eventClass() {
        return InboxTransientError.class;
    }
}
