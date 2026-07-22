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

package com.mqttsnet.thinglinks.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.baidu.bifromq.plugin.eventcollector.ClientEvent;
import com.baidu.bifromq.plugin.eventcollector.Event;
import com.baidu.bifromq.type.ClientInfo;
import com.mqttsnet.thinglinks.constant.CommonConstants;

/**
 * {@link ClientEvent} 家族通用 processor 基类 ── 统一抽取 {@link ClientInfo} 公共字段(tenantId / metadataMap).
 *
 * <p>子类只需声明 {@link #eventClass()} 与可选 {@link #enrichSubtype} 填特异字段,
 * 样板抽取逻辑集中到此处,Optional 全链路防 null + 类型安全 cast.
 *
 * <h3>4.0 升级备注</h3>
 * 本基类只依赖 {@link ClientEvent} / {@link ClientInfo} 两个 4.0 仍保留的抽象,
 * 升级时仅 package import 从 {@code com.baidu.bifromq} 改为 {@code org.apache.bifromq},
 * 无逻辑变更.
 *
 * @param <T> 具体 ClientEvent 子类型
 * @author mqttsnet
 * @since 1.1
 */
public abstract class AbstractClientEventProcessor<T extends ClientEvent<T>> extends AbstractEventProcessor {

    @Override
    protected final Map<String, Object> buildEventData(Event<?> event) {
        Map<String, Object> data = new HashMap<>();
        Optional.ofNullable(event)
            .filter(eventClass()::isInstance)
            .map(e -> eventClass().cast(e.clone()))
            .ifPresent(typed -> {
                fillFromClientInfo(data, typed.clientInfo());
                enrichSubtype(data, typed);
            });
        return data;
    }

    /**
     * 本 processor 对应的具体 {@link ClientEvent} 子类型 Class 字面量.
     * <p>用于 instanceof 过滤 + 类型安全 cast,避免向下转型 NPE / ClassCastException.
     *
     * @return 具体 ClientEvent 子类型 Class
     */
    protected abstract Class<T> eventClass();

    /**
     * 子类型特异字段填充钩子.
     * <p>例如 {@link com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.Idle#keepAliveTimeSeconds}
     * / {@link com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.ByClient#withoutDisconnect} 等;
     * 默认空实现 ── 不需特异字段的子类不必 override.
     *
     * @param data  累积事件数据 map
     * @param event 已 cast 到具体子类型的事件
     */
    protected void enrichSubtype(Map<String, Object> data, T event) {
        // no-op
    }

    /**
     * 从 {@link ClientInfo} 抽取 tenantId + metadata 公共字段.
     * <p>{@code clientInfo} 缺失即整段跳过(channelclosed 等场景可能无 clientInfo,容忍).
     *
     * @param data       累积事件数据 map
     * @param clientInfo 当前事件的 client 上下文,可空
     */
    private void fillFromClientInfo(Map<String, Object> data, ClientInfo clientInfo) {
        Optional.ofNullable(clientInfo).ifPresent(ci -> {
            data.put(CommonConstants.TENANT_ID, Optional.ofNullable(ci.getTenantId()).orElse(""));
            Optional.ofNullable(ci.getMetadataMap()).ifPresent(metadata -> {
                data.put(CommonConstants.ACL_RULE, metadata.getOrDefault(CommonConstants.ACL_RULE, ""));
                data.put("ver", metadata.getOrDefault("ver", ""));
                data.put(CommonConstants.CLIENT_ID, metadata.getOrDefault(CommonConstants.CLIENT_ID, ""));
                data.put(CommonConstants.USER_ID, metadata.getOrDefault(CommonConstants.USER_ID, ""));
                data.put("channelId", metadata.getOrDefault("channelId", ""));
                data.put("address", metadata.getOrDefault("address", ""));
                data.put("broker", metadata.getOrDefault("broker", ""));
                data.put(CommonConstants.VERSION, metadata.getOrDefault("ver", ""));
                data.put("sessionType", metadata.getOrDefault("sessionType", ""));
            });
        });
    }
}
