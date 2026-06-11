package com.mqttsnet.thinglinks.mqtt.handler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.chatgpt.model.enumeration.OpenAiGptModelEnum;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.protocol.factory.ProtocolMessageAdapter;
import com.mqttsnet.basic.protocol.model.EncryptionDetailsDTO;
import com.mqttsnet.basic.protocol.model.ProtocolDataMessageDTO;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.broker.MqttBrokerOpenAnyUserFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.CommonConstants;
import com.mqttsnet.thinglinks.entity.mqtt.source.MqttMessageEventSource;
import com.mqttsnet.thinglinks.link.facade.DeviceOpenAnyUserFacade;
import com.mqttsnet.thinglinks.mqtt.handler.factory.AbstractMessageHandler;
import com.mqttsnet.thinglinks.protocol.vo.param.OpenAiChatRequestParam;
import com.mqttsnet.thinglinks.protocol.vo.result.OpenAiChatResponseResultVO;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.stereotype.Service;

/**
 * @program: thinglinks-cloud
 * @description: 处理CHATGPT_REQUEST主题
 * TODO 优化 Chatgpt 调用逻辑
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-12-12 23:00
 **/
@Slf4j
@Service
public class ChatgptRequestHandler extends AbstractMessageHandler implements TopicHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Map<String, List<Message>> CONTEXT = new HashMap<>();

    public ChatgptRequestHandler(LinkCacheDataHelper linkCacheDataHelper,
                                 DeviceOpenAnyUserFacade deviceOpenAnyUserApi,
                                 MqttBrokerOpenAnyUserFacade mqttBrokerOpenAnyTenantApi,
                                 ProtocolMessageAdapter protocolMessageAdapter) {
        super(linkCacheDataHelper, deviceOpenAnyUserApi, mqttBrokerOpenAnyTenantApi, protocolMessageAdapter);
    }


    /**
     * ¬
     * 处理CHATGPT_REQUEST主题的MQTT消息
     *
     * @param eventSource 包含MQTT消息事件源数据的对象
     */
    @Override
    public void handle(MqttMessageEventSource eventSource) {
        String topic = eventSource.getTopic();
        String qos = eventSource.getQos();
        byte[] payloadBytes = eventSource.getPayloadBytes();
        String body = StrUtil.str(payloadBytes, StandardCharsets.UTF_8);
        log.info("Received CHATGPT_REQUEST message: topic: {}, qos: {}, payload(body): {}", topic, qos, body);
        if (!protocolMessageAdapter.validateProtocolData(body)) {
            log.warn("The protocol format is incorrect");
            return;
        }

        Map<String, String> variables = protocolMessageAdapter.extractVariables(topic);
        String version = variables.get(CommonConstants.VERSION);
        String deviceId = variables.get(CommonConstants.DEVICE_ID);

        DeviceCacheVO deviceCacheVO = getDeviceCacheVO(deviceId);
        if (deviceCacheVO == null) {
            log.warn("Device with ID {} not found.", deviceId);
            return;
        }

        try {
            ProtocolDataMessageDTO protocolDataMessageDTO = protocolMessageAdapter.parseProtocolDataMessage(body);
            EncryptionDetailsDTO encryptionDetailsDTO = EncryptionDetailsDTO.builder()
                    .signKey(deviceCacheVO.getSignKey())
                    .encryptKey(deviceCacheVO.getEncryptKey())
                    .encryptVector(deviceCacheVO.getEncryptVector())
                    .cipherFlag(deviceCacheVO.getEncryptMethod())
                    .build();
            String dataBody = protocolMessageAdapter.decryptMessage(body, encryptionDetailsDTO);

            Optional<OpenAiChatRequestParam> paramOpt = parseOpenAiChatRequestParam(dataBody);
            if (paramOpt.isPresent()) {
                // 处理主题消息
                String resultDataBody = processingTopicMessage(paramOpt.get());

                // 处理返回结果
                ProtocolDataMessageDTO handleResult = protocolMessageAdapter.buildResponse(protocolDataMessageDTO, resultDataBody, encryptionDetailsDTO);

                // 生成响应主题字符串
                String responseTopicStr = generateResponseTopic(version, deviceId, "/chatgpt/response");

                // 序列化 handleResult 对象为 JSON 字符串
                String resultData = OBJECT_MAPPER.writeValueAsString(handleResult);

                // 推送消息到 MQTT 通知设备添加子设备成功&失败
                sendMessage(responseTopicStr, qos, resultData, String.valueOf(ContextUtil.getTenantId()));
            }
        } catch (Exception e) {
            log.error("Failed to decrypt the message", e);
        }
    }


    @Override
    protected String processingTopicMessage(Object openAiChatRequestParam) throws Exception {
        ChatCompletionResponse openAiResponse = getOpenAiResponse((OpenAiChatRequestParam) openAiChatRequestParam);
        OpenAiChatResponseResultVO openAiChatResponseResultVO = BeanPlusUtil.toBeanIgnoreError(openAiResponse, OpenAiChatResponseResultVO.class);
        return JSON.toJSONString(openAiChatResponseResultVO);
    }

    /**
     * Parses the OpenAiChatRequestParam from the provided data body.
     *
     * @param dataBody The data body to parse.
     * @return An Optional of OpenAiChatRequestParam.
     */
    private Optional<OpenAiChatRequestParam> parseOpenAiChatRequestParam(String dataBody) {
        try {
            return Optional.of(JSON.parseObject(dataBody, OpenAiChatRequestParam.class));
        } catch (Exception e) {
            log.warn("Failed to parse OpenAI Chat Request Param: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public ChatCompletionResponse getOpenAiResponse(OpenAiChatRequestParam requestParam) {
        OpenAiClient openAiClient = buildOpenAiClient(requestParam.getApiKey(), requestParam.getApiHost());

        Message message = Message.builder()
                .role(Message.Role.USER)
                .content(requestParam.getContent())
                .build();


        List<Message> messages = get(requestParam.getId());
        messages.add(message);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(messages)
                .model(OpenAiGptModelEnum.fromValue(requestParam.getModel()).get().getModelName())
                .build();

        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);

        add(requestParam.getId(), chatCompletionResponse.getChoices().get(0).getMessage());

        return chatCompletionResponse;
    }

    public List<Message> get(String id) {
        return CONTEXT.computeIfAbsent(id, k -> new ArrayList<>());
    }

    public void add(String id, Message message) {
        List<Message> messages = CONTEXT.computeIfAbsent(id, k -> new ArrayList<>());
        messages.add(message);
    }

    private OpenAiClient buildOpenAiClient(String apiKey, String apiHost) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)//自定义日志输出
                .addInterceptor(new OpenAiResponseInterceptor())//自定义返回值拦截
                .connectTimeout(10, TimeUnit.SECONDS)//自定义超时时间
                .writeTimeout(30, TimeUnit.SECONDS)//自定义超时时间
                .readTimeout(30, TimeUnit.SECONDS)//自定义超时时间
                .build();

        OpenAiClient.Builder clientBuilder = OpenAiClient.builder()
                .okHttpClient(okHttpClient);

        if (StrUtil.isNotBlank(apiKey)) {
            clientBuilder.apiKey(Collections.singletonList(apiKey));
        } else {
            throw BizException.wrap("API Key is required");
        }

        if (StrUtil.isNotBlank(apiHost)) {
            clientBuilder.apiHost(apiHost);
        }


        return clientBuilder.build();
    }

}
