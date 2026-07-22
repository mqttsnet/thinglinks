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

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.baidu.bifromq.plugin.eventcollector.EventType;
import com.mqttsnet.thinglinks.processor.mqttbroker.PingReqEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.channelclosed.NotAuthorizedClientEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientconnected.ClientConnectedEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.BadPacketEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ByClientEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ByServerEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ClientChannelErrorEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ExceedPubRateEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ExceedReceivingLimitEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.IdleEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.InboxTransientErrorEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.InvalidTopicEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.InvalidTopicFilterEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.KickedEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.MalformedTopicEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.MalformedTopicFilterEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.NoPubPermissionEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ProtocolViolationEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ReAuthFailedEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ResourceThrottledEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.ServerBusyEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.TooLargeSubscriptionEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.clientdisconnect.TooLargeUnsubscriptionEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.disthandling.DistErrorEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.disthandling.DistedEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.subhandling.SubAckedEventProcessor;
import com.mqttsnet.thinglinks.processor.mqttbroker.subhandling.UnsubAckedEventProcessor;
import com.mqttsnet.thinglinks.processor.session.MqttSessionStartEventProcessor;
import com.mqttsnet.thinglinks.processor.session.MqttSessionStopEventProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 事件处理器工厂 ── 按 {@link EventType} 静态注册全部 processor.
 *
 * <h3>覆盖范围</h3>
 * <ul>
 *   <li>{@code clientconnected} ── CONNECT(1)</li>
 *   <li>{@code clientdisconnect} ── 20 个 disconnect 子类型(BY_CLIENT / BY_SERVER / KICKED / IDLE / ...)</li>
 *   <li>{@code channelclosed} ── 仅 NOT_AUTHORIZED_CLIENT(其余 10 个 P2 跟进)</li>
 *   <li>{@code subhandling} ── SUB_ACKED / UNSUB_ACKED</li>
 *   <li>{@code disthandling} ── DISTED / DIST_ERROR</li>
 *   <li>{@code mqttbroker} ── PING_REQ</li>
 *   <li>{@code session} ── MQTT_SESSION_START / STOP</li>
 * </ul>
 *
 * <h3>4.0 升级备注</h3>
 * <ul>
 *   <li>本次注册的 27 种 {@link EventType} 在 4.0.0-incubating 全部保留</li>
 *   <li>4.0 新增 {@code SERVER_REDIRECTED}(对应 {@code Redirect} class)── 3.3.5 无此 class,升级后需补 processor</li>
 *   <li>4.0 删除 {@code DELIVER_NO_INBOX / SUBSCRIBED / SUBSCRIBE_ERROR / UNSUBSCRIBED / UNSUBSCRIBED_ERROR} ── 本工厂未注册,无影响</li>
 *   <li>4.0 包路径 {@code com.baidu.bifromq} → {@code org.apache.bifromq} ── 升级时全局 import 替换</li>
 * </ul>
 *
 * @author mqttsnet
 * @since 1.1
 */
@Slf4j
public class EventProcessorFactory {

    private final Map<EventType, EventProcessor> processorMap = new EnumMap<>(EventType.class);

    public EventProcessorFactory() {
        // ── client connected ──
        processorMap.put(EventType.CLIENT_CONNECTED, new ClientConnectedEventProcessor());

        // ── client disconnect(20 个子类型,3.3.5 全集)──
        processorMap.put(EventType.BAD_PACKET, new BadPacketEventProcessor());
        processorMap.put(EventType.BY_CLIENT, new ByClientEventProcessor());
        processorMap.put(EventType.BY_SERVER, new ByServerEventProcessor());
        processorMap.put(EventType.CLIENT_CHANNEL_ERROR, new ClientChannelErrorEventProcessor());
        processorMap.put(EventType.EXCEED_PUB_RATE, new ExceedPubRateEventProcessor());
        processorMap.put(EventType.EXCEED_RECEIVING_LIMIT, new ExceedReceivingLimitEventProcessor());
        processorMap.put(EventType.IDLE, new IdleEventProcessor());
        processorMap.put(EventType.INBOX_TRANSIENT_ERROR, new InboxTransientErrorEventProcessor());
        processorMap.put(EventType.INVALID_TOPIC, new InvalidTopicEventProcessor());
        processorMap.put(EventType.INVALID_TOPIC_FILTER, new InvalidTopicFilterEventProcessor());
        processorMap.put(EventType.KICKED, new KickedEventProcessor());
        processorMap.put(EventType.MALFORMED_TOPIC, new MalformedTopicEventProcessor());
        processorMap.put(EventType.MALFORMED_TOPIC_FILTER, new MalformedTopicFilterEventProcessor());
        processorMap.put(EventType.NO_PUB_PERMISSION, new NoPubPermissionEventProcessor());
        processorMap.put(EventType.PROTOCOL_VIOLATION, new ProtocolViolationEventProcessor());
        processorMap.put(EventType.RE_AUTH_FAILED, new ReAuthFailedEventProcessor());
        processorMap.put(EventType.RESOURCE_THROTTLED, new ResourceThrottledEventProcessor());
        processorMap.put(EventType.SERVER_BUSY, new ServerBusyEventProcessor());
        processorMap.put(EventType.TOO_LARGE_SUBSCRIPTION, new TooLargeSubscriptionEventProcessor());
        processorMap.put(EventType.TOO_LARGE_UNSUBSCRIPTION, new TooLargeUnsubscriptionEventProcessor());
        // TODO 4.0 升级时补:processorMap.put(EventType.SERVER_REDIRECTED, new ServerRedirectedEventProcessor());

        // ── channel closed(目前只补 NOT_AUTHORIZED_CLIENT 一个孤儿,其余 10 个 P2)──
        processorMap.put(EventType.NOT_AUTHORIZED_CLIENT, new NotAuthorizedClientEventProcessor());

        // ── subscription handling ──
        processorMap.put(EventType.SUB_ACKED, new SubAckedEventProcessor());
        processorMap.put(EventType.UNSUB_ACKED, new UnsubAckedEventProcessor());

        // ── distribution handling ──
        processorMap.put(EventType.DISTED, new DistedEventProcessor());
        processorMap.put(EventType.DIST_ERROR, new DistErrorEventProcessor());

        // ── mqtt broker ──
        processorMap.put(EventType.PING_REQ, new PingReqEventProcessor());

        // ── session lifecycle ──
        processorMap.put(EventType.MQTT_SESSION_START, new MqttSessionStartEventProcessor());
        processorMap.put(EventType.MQTT_SESSION_STOP, new MqttSessionStopEventProcessor());
    }

    /**
     * 按事件类型查 processor;未注册返 empty.
     *
     * @param eventType 事件类型
     * @return processor Optional 包装
     */
    public Optional<EventProcessor> getProcessor(EventType eventType) {
        return Optional.ofNullable(processorMap.get(eventType));
    }
}
