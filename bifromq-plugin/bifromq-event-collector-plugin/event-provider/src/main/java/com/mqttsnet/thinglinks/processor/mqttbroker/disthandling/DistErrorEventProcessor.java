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

package com.mqttsnet.thinglinks.processor.mqttbroker.disthandling;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.baidu.bifromq.plugin.eventcollector.Event;
import com.baidu.bifromq.plugin.eventcollector.distservice.DistError;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.processor.AbstractEventProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 分发错误事件处理器 ── broker 把上行 PUBLISH 分发给订阅者过程中失败.
 *
 * <h3>BifroMQ 字段(3.3.5 / 4.0 一致)</h3>
 * <ul>
 *   <li>{@code long reqId} ── 分发请求 ID</li>
 *   <li>{@code Iterable<PublisherMessagePack> messages} ── 失败消息列表</li>
 *   <li>{@code DistErrorCode code} ── 错误码枚举</li>
 * </ul>
 *
 * <h3>4.0 升级注意</h3>
 * 包路径 {@code com.baidu.bifromq.plugin.eventcollector.distservice} → {@code org.apache.bifromq.plugin.eventcollector.distservice};
 * {@code PublisherMessagePack} 与 {@code DistErrorCode} 内部 API 一致,无需改 plugin 解析逻辑.
 *
 * @author mqttsnet
 * @since 1.0.0
 */
@Slf4j
public class DistErrorEventProcessor extends AbstractEventProcessor {

    @Override
    protected Map<String, Object> buildEventData(Event<?> event) {
        Map<String, Object> data = new HashMap<>();

        // 确保事件是 DistError 类型
        if (!(event instanceof DistError)) {
            log.error("Invalid event type. Expected DistError, but got {}", event.getClass().getSimpleName());
            return data;
        }

        Optional.of(event)
            .map(e -> (DistError) e.clone())
            .ifPresent(distError -> {
                String tenantId = "";  // This doesn't seem to change or get assigned a value based on the given code

                data.put(CommonConstants.CLIENT_ID, getSafeValue(distError.reqId(), ""));
                data.put(CommonConstants.TENANT_ID, getSafeValue(tenantId, ""));
                Optional.ofNullable(distError.messages())
                    .ifPresent(messages -> data.put("message", getSafeValue(messages.toString(), "")));
                data.put("reqId", getSafeValue(distError.reqId(), ""));
                data.put("code", getSafeValue(distError.code(), 0));
            });
        return data;
    }

}
