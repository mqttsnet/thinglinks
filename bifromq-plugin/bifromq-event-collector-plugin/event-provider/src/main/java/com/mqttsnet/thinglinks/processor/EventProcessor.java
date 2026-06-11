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

import com.baidu.bifromq.plugin.eventcollector.Event;
import com.mqttsnet.thinglinks.sender.KafkaMessageSender;

/**
 * 事件处理器接口 ── 把 event 对象转换为 Kafka 消息并投递.
 *
 * @author mqttsnet
 * @since 1.0.0
 */
public interface EventProcessor {

    /**
     * 处理事件并发送到 Kafka.
     * <p>
     * eventHlc / eventUtc 由事件采集器在 report 入口同步从 {@code Event.hlc()} / {@code Event.utc()}
     * 抓取并传入,避免 64 worker 异步池内取值导致的因果时钟乱序;下游 mqs 使用 hlc 做状态机单调写.
     *
     * @param event    event 对象
     * @param topic    目标 Kafka topic
     * @param sender   Kafka 消息发送器
     * @param eventHlc 因果时钟 HLC(64-bit,下游单调写权威键)
     * @param eventUtc 物理 UTC ms(时间锚点,业务展示用)
     */
    void process(Event<?> event, String topic, KafkaMessageSender sender, long eventHlc, long eventUtc);

}
