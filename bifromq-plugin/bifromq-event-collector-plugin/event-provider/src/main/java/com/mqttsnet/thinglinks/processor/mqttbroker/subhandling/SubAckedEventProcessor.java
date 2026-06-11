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
import com.baidu.bifromq.plugin.eventcollector.mqttbroker.subhandling.SubAcked;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link SubAcked} 事件处理器 ── 订阅请求被 broker 确认(SUBACK).
 * <p>仅记录首个 topicFilter + granted QoS(简化场景,多 topic 订阅取第一个).
 *
 * @author mqttsnet
 * @since 1.1
 */
public class SubAckedEventProcessor extends AbstractClientEventProcessor<SubAcked> {

    @Override
    protected Class<SubAcked> eventClass() {
        return SubAcked.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, SubAcked event) {
        data.put("messageId", event.messageId());
        Optional.ofNullable(event.topicFilter())
            .filter(CollUtil::isNotEmpty)
            .map(topics -> topics.get(0))
            .ifPresent(topic -> data.put(CommonConstants.TOPIC, topic));
        Optional.ofNullable(event.granted())
            .filter(CollUtil::isNotEmpty)
            .map(granted -> granted.get(0))
            .ifPresent(granted -> data.put("granted", granted));
    }
}
