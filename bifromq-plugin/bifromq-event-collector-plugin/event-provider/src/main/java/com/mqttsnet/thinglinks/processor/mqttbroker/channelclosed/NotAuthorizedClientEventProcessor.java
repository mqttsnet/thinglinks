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

package com.mqttsnet.thinglinks.processor.mqttbroker.channelclosed;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.baidu.bifromq.plugin.eventcollector.Event;
import com.baidu.bifromq.plugin.eventcollector.mqttbroker.channelclosed.NotAuthorizedClient;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.processor.AbstractEventProcessor;

/**
 * {@link NotAuthorizedClient} 事件处理器 ── 客户端认证失败(协议未通过或凭证无效),broker 断开 channel.
 *
 * <p>{@link com.baidu.bifromq.plugin.eventcollector.mqttbroker.channelclosed.ChannelClosedEvent} 家族特殊性:
 * channel 还未建立完成 → 无 {@code ClientInfo},无 metadataMap;字段 {@code tenantId / userId / clientId}
 * 由事件类本身直接持有(对应认证请求里携带的值,可能为空).
 *
 * <p>所以不复用 {@link com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor},
 * 直接实现 buildEventData.
 *
 * @author mqttsnet
 * @since 1.1
 */
public class NotAuthorizedClientEventProcessor extends AbstractEventProcessor {

    @Override
    protected Map<String, Object> buildEventData(Event<?> event) {
        Map<String, Object> data = new HashMap<>();
        Optional.ofNullable(event)
            .filter(NotAuthorizedClient.class::isInstance)
            .map(e -> (NotAuthorizedClient) e.clone())
            .ifPresent(notAuth -> {
                data.put(CommonConstants.TENANT_ID, Optional.ofNullable(notAuth.tenantId()).orElse(""));
                data.put(CommonConstants.USER_ID, Optional.ofNullable(notAuth.userId()).orElse(""));
                data.put(CommonConstants.CLIENT_ID, Optional.ofNullable(notAuth.clientId()).orElse(""));
                Optional.ofNullable(notAuth.peerAddress())
                    .ifPresent(addr -> data.put("address", addr.toString()));
            });
        return data;
    }
}
