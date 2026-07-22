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

package com.mqttsnet.thinglinks.processor.mqttbroker.subhandling;

import java.util.Map;
import java.util.Optional;

import cn.hutool.core.collection.CollUtil;
import com.baidu.bifromq.plugin.eventcollector.mqttbroker.subhandling.UnsubAcked;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link UnsubAcked} 事件处理器 ── 退订请求被 broker 确认(UNSUBACK).
 * <p>仅记录首个 topicFilter(简化场景,多 topic 退订取第一个).
 *
 * @author mqttsnet
 * @since 1.1
 */
public class UnsubAckedEventProcessor extends AbstractClientEventProcessor<UnsubAcked> {

    @Override
    protected Class<UnsubAcked> eventClass() {
        return UnsubAcked.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, UnsubAcked event) {
        data.put("messageId", event.messageId());
        Optional.ofNullable(event.topicFilter())
            .filter(CollUtil::isNotEmpty)
            .map(topics -> topics.get(0))
            .ifPresent(topic -> data.put(CommonConstants.TOPIC, topic));
    }
}
