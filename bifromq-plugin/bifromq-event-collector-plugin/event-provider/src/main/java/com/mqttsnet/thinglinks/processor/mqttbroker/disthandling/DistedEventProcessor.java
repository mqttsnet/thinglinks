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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import cn.hutool.core.util.HexUtil;
import com.baidu.bifromq.plugin.eventcollector.Event;
import com.baidu.bifromq.plugin.eventcollector.distservice.Disted;
import com.baidu.bifromq.type.ClientInfo;
import com.google.protobuf.ByteString;
import com.mqttsnet.thinglinks.constant.CommonConstants;
import com.mqttsnet.thinglinks.processor.AbstractEventProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息分发成功事件处理器（包含遗嘱消息）
 * <p>
 * 数据编码说明：
 * <p>
 * 需要注意问题：
 * 1. 数据损坏风险：MQTT payload包含二进制数据，直接放入JSON会因UTF-8编码转换而损坏
 * 2. 设备协议失效：设备通信协议通常基于原始字节数据，编码损坏导致协议解析失败
 * 3. 调试困难：损坏的数据难以人工阅读和分析，增加问题排查难度
 * 4. 系统异常：下游系统可能因数据格式异常而崩溃或产生错误行为
 * <p>
 * 示例问题：原始设备数据 "00000003e6010000..." 被错误转换为 "00000003efbfbd010000..."
 * 其中字节 0xE6 被UTF-8解码器替换为 efbfbd，导致设备无法识别数据格式
 * <p>
 * 解决方案：
 * 1. payload字段：使用Base64编码，确保二进制数据在JSON中安全传输
 * 2. payloadHex字段：提供十六进制格式，便于设备协议解析和人工调试
 * 3. originalSize字段：记录原始数据大小，用于数据完整性校验
 * 4. encoding字段：明确标识编码方式，避免下游系统解析歧义
 * <p>
 * 设计原则：
 * - 数据完整性优先：确保原始数据在传输过程中不被损坏
 * - 多格式兼容：同时满足系统处理和人工分析的需求
 * - 明确标识：清晰说明数据格式，避免误解和错误使用
 * - 标准化实践：Base64是业界处理二进制数据文本化的标准方案
 *
 * @author mqttsnet
 * @since 1.0.0
 */
@Slf4j
public class DistedEventProcessor extends AbstractEventProcessor {

    @Override
    protected Map<String, Object> buildEventData(Event<?> event) {
        Map<String, Object> data = new HashMap<>();

        // 确保事件是 Disted 类型
        if (!(event instanceof Disted)) {
            log.error("Invalid event type. Expected Disted, but got {}", event.getClass().getSimpleName());
            return data;
        }

        Optional.of(event)
            .map(e -> (Disted) e.clone()).flatMap(disted -> {
                data.put("reqId", getSafeValue(disted.reqId(), 0L));
                // BifroMQ 字段(3.3.5 / 4.0 一致):fanout = 本次分发触达的订阅者数量,
                // 0 表示没有订阅者(消息被丢)、>0 表示成功扇出数.
                // 4.0 兼容:包路径 com.baidu.bifromq.plugin.eventcollector.distservice 改为 org.apache.bifromq.*,API 一致.
                data.put("fanout", getSafeValue(disted.fanout(), 0));
                return Optional.ofNullable(disted.messages());
            }).ifPresent(messagePack -> {
                messagePack.forEach(pack -> {
                    Optional<String> tenantIdOpt = Optional.of(pack.getPublisher()).map(ClientInfo::getTenantId);
                    tenantIdOpt.ifPresent(tenantId -> data.put(CommonConstants.TENANT_ID, getSafeValue(tenantId, "")));

                    Optional<Map<String, String>> metadataMapOpt = Optional.of(pack.getPublisher().getMetadataMap());
                    metadataMapOpt.ifPresent(metadataMap -> {
                        data.put(CommonConstants.ACL_RULE, getSafeValue(metadataMap.get(CommonConstants.ACL_RULE), ""));
                        data.put("ver", getSafeValue(metadataMap.get("ver"), ""));
                        data.put(CommonConstants.CLIENT_ID, getSafeValue(metadataMap.get(CommonConstants.CLIENT_ID), ""));
                        data.put(CommonConstants.USER_ID, getSafeValue(metadataMap.get(CommonConstants.USER_ID), ""));
                        data.put("channelId", getSafeValue(metadataMap.get("channelId"), ""));
                        data.put("address", getSafeValue(metadataMap.get("address"), ""));
                        data.put("broker", getSafeValue(metadataMap.get("broker"), ""));
                        data.put(CommonConstants.VERSION, getSafeValue(metadataMap.get("ver"), ""));
                        data.put("sessionType", getSafeValue(metadataMap.get("sessionType"), ""));
                    });
                    Optional.of(pack.getMessagePackList())
                        .ifPresent(messagePackList -> {
                            messagePackList.parallelStream().forEach(msg -> {
                                String topic = msg.getTopic();
                                Optional.of(msg.getMessageList())
                                    .ifPresent(messageList -> {
                                        messageList.parallelStream().forEach(message -> {
                                            data.put(CommonConstants.TOPIC, getSafeValue(topic, ""));
                                            data.put("messageId", getSafeValue(message.getMessageId(), 0));
                                            data.put("qos", getSafeValue(message.getPubQoSValue(), 0));
                                            data.put("isRetain", getSafeValue(message.getIsRetain(), false));
                                            data.put("isRetained", getSafeValue(message.getIsRetained(), false));
                                            data.put("isUTF8String", getSafeValue(message.getIsUTF8String(), false));
                                            data.put("contentType", getSafeValue(message.getContentType(), ""));
                                            data.put("responseTopic", getSafeValue(message.getResponseTopic(), ""));
                                            data.put("correlationData", getSafeValue(message.getCorrelationData().toStringUtf8(), ""));
                                            data.put("userProperties", getSafeValue(message.getUserProperties().toString(), ""));
                                            data.put("timestamp", getSafeValue(message.getTimestamp(), 0));
                                            data.put("time", getSafeValue(message.getTimestamp(), 0));
                                            data.put("expiryInterval", getSafeValue(message.getExpiryInterval(), 0));
                                            ByteString payload = message.getPayload();
                                            byte[] payloadBytes = payload.toByteArray();
                                            // 统一使用Base64编码（安全传输）
                                            data.put("payload", Base64.getEncoder().encodeToString(payloadBytes));
                                            data.put("payloadHex", HexUtil.encodeHexStr(payloadBytes));
                                            // 原始数据大小
                                            data.put("originalSize", payloadBytes.length);
                                            // 编码方式说明
                                            data.put("encoding", "base64");
                                        });
                                    });
                            });
                        });
                });
            });

        return data;
    }

}
