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

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.ResourceThrottled;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link ResourceThrottled} 事件处理器 ── 资源限流(全局或租户级配额触顶),broker 断连.
 *
 * @author mqttsnet
 * @since 1.1
 */
public class ResourceThrottledEventProcessor extends AbstractClientEventProcessor<ResourceThrottled> {

    @Override
    protected Class<ResourceThrottled> eventClass() {
        return ResourceThrottled.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, ResourceThrottled event) {
        data.put("reason", event.reason());
    }
}
