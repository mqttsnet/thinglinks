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

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.NoPubPermission;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link NoPubPermission} 事件处理器 ── 客户端发布到无权限的 topic,broker 断连.
 *
 * <h3>BifroMQ 字段(3.3.5 / 4.0 一致)</h3>
 * <ul>
 *   <li>{@code String topic} ── 被拒绝的发布 topic</li>
 *   <li>{@code QoS qos} ── 发布尝试的 QoS 级别(枚举,4.0 包路径 {@code org.apache.bifromq.type.QoS})</li>
 *   <li>{@code boolean retain} ── 是否标记为 retain 消息</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 1.1
 */
public class NoPubPermissionEventProcessor extends AbstractClientEventProcessor<NoPubPermission> {

    @Override
    protected Class<NoPubPermission> eventClass() {
        return NoPubPermission.class;
    }

    @Override
    protected void enrichSubtype(Map<String, Object> data, NoPubPermission event) {
        data.put(CommonConstants.TOPIC, event.topic());
        // 4.0 兼容:QoS 枚举包路径从 com.baidu.bifromq.type.QoS 改为 org.apache.bifromq.type.QoS,API getNumber() 一致.
        Optional.ofNullable(event.qos())
            .map(qos -> qos.getNumber())
            .ifPresent(num -> data.put("qos", num));
        data.put("isRetain", event.retain());
    }
}
