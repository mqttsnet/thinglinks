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

import com.baidu.bifromq.plugin.eventcollector.mqttbroker.clientdisconnect.Kicked;
import com.mqttsnet.thinglinks.processor.AbstractClientEventProcessor;

/**
 * {@link Kicked} 事件处理器 ── 同 clientId 的新连接接入,broker 踢掉老连接.
 *
 * <h3>BifroMQ 字段差异(3.3.5 vs 4.0)</h3>
 * <ul>
 *   <li>3.3.5:无特异字段(被踢者信息从 {@code clientInfo} 取)</li>
 *   <li>4.0 新增:{@code ClientInfo kicker} ── 踢线发起方的 ClientInfo,排查恶意抢占有用</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 1.1
 */
public class KickedEventProcessor extends AbstractClientEventProcessor<Kicked> {

    // TODO 4.0 升级时补 kicker 字段(BifroMQ 4.0 新增):
    //   @Override
    //   protected void enrichSubtype(Map<String, Object> data, Kicked event) {
    //       Optional.ofNullable(event.kicker()).ifPresent(kicker -> {
    //           data.put("kickerTenantId", Optional.ofNullable(kicker.getTenantId()).orElse(""));
    //           Optional.ofNullable(kicker.getMetadataMap()).ifPresent(meta -> {
    //               data.put("kickerClientId", meta.getOrDefault(CommonConstants.CLIENT_ID, ""));
    //               data.put("kickerUserId", meta.getOrDefault(CommonConstants.USER_ID, ""));
    //               data.put("kickerAddress", meta.getOrDefault("address", ""));
    //           });
    //       });
    //   }

    @Override
    protected Class<Kicked> eventClass() {
        return Kicked.class;
    }
}
