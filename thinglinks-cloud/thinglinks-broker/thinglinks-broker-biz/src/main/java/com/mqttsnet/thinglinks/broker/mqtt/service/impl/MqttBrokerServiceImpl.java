package com.mqttsnet.thinglinks.service.impl;

import java.util.Collections;
import java.util.Objects;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.broker.BifroMqFacade;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.service.MqttBrokerService;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * -----------------------------------------------------------------------------
 * File Name: MqttBrokerServiceImpl.java
 * -----------------------------------------------------------------------------
 * Description:
 * MqttBroker API 实现类
 * -----------------------------------------------------------------------------
 *
 * @author ShiHuan Sun
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * <p>
 * -----------------------------------------------------------------------------
 * @email 13733918655@163.com
 * @date 2023-10-31 19:44
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MqttBrokerServiceImpl implements MqttBrokerService {
    private final BifroMqFacade bifroMQApi;

    private final LinkCacheDataHelper linkCacheDataHelper;


    /**
     * Publishes a message to a specified topic and returns the content if successful.
     *
     * @param publishMessageRequestVO Object containing the required parameters for publishing.
     * @return The content of the published message.
     * @throws BizException If the publishing fails.
     */
    @Override
    public String publishMessage(PublishMessageRequestVO publishMessageRequestVO) throws BizException {
        log.info("Preparing to publish message publishMessageRequestVO: {}", JSON.toJSONString(publishMessageRequestVO));
        ArgumentAssert.notBlank(publishMessageRequestVO.getTopic(), "Topic is required");
        ArgumentAssert.notBlank(publishMessageRequestVO.getTenantId(), "TenantId is required");
        ArgumentAssert.notNull(publishMessageRequestVO.getQos(), "Qos is required");
        linkCacheDataHelper.incrementDownLinkCounter();
        try {
            ResponseEntity<String> response = callPublishBifromqApi(publishMessageRequestVO);

            log.info("Response from BifroMQApi to publishMessage: {}", response);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully published message with topic: {}", publishMessageRequestVO.getTopic());
                return publishMessageRequestVO.getPayloadAsString();
            } else {
                log.error("Failed to publish message with topic: {}. Response Status: {}",
                        publishMessageRequestVO.getTopic(), response.getStatusCode());
                throw new BizException("Failed to publish message with topic: " + publishMessageRequestVO.getTopic());
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error occurred while publishing message: {}", e.getMessage());
            throw new BizException("Error during message publishing: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error occurred while publishing message: {}", e.getMessage());
            throw new BizException("Unexpected error during message publishing: " + e.getMessage());
        }
    }


    /**
     * Makes the actual API call to publish a message.
     *
     * @param publishMessageRequestVO Object containing the required parameters for publishing.
     * @return R Response indicating success or failure from the BifroMQApi.
     */
    private ResponseEntity<String> callPublishBifromqApi(PublishMessageRequestVO publishMessageRequestVO) {
        log.info("MQTT发布消息 -> 租户:[{}] 主题:[{}] QoS:[{}] 客户端类型:[{}] 消息负载:{}",
                publishMessageRequestVO.getTenantId(),
                publishMessageRequestVO.getTopic(),
                publishMessageRequestVO.getQos(),
                publishMessageRequestVO.getClientType(),
                publishMessageRequestVO.getPayload());

        byte[] payloadBytes = publishMessageRequestVO.getPayloadAsBytes();
        log.info("负载处理 -> 原始:{}字节 处理后:{}字节 Base64解码:{}",
                publishMessageRequestVO.getPayload() != null ? publishMessageRequestVO.getPayload().length() : 0,
                payloadBytes != null ? payloadBytes.length : 0,
                publishMessageRequestVO.getForceBase64Decode());

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = bifroMQApi.publishMessage(
                publishMessageRequestVO.getReqId(),
                publishMessageRequestVO.getTenantId(),
                publishMessageRequestVO.getTopic(),
                publishMessageRequestVO.getQos(),
                publishMessageRequestVO.getExpirySeconds(),
                publishMessageRequestVO.getClientType(),
                payloadBytes
        );

        log.info("发布完成 -> 耗时:{}ms 状态:{} 响应:{}", System.currentTimeMillis() - startTime, response.getStatusCode(), response.getBody());
        return response;
    }


    @Override
    public MqttSessionDetailsResultVO getSessionInfo(String tenantId, String userId, String clientId) throws Exception {
        log.info("Retrieving session info for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
        try {
            // 调用 Feign 客户端获取会话信息
            ResponseEntity<String> response = bifroMQApi.getSessionInfo(Long.valueOf(SnowflakeIdUtil.nextId()), tenantId, userId, clientId);
            log.info("Received response from MQTT broker for session info: {}", response.getBody());
            // 检查响应状态和内容
            if (response.getStatusCode().is2xxSuccessful()) {
                return JsonUtil.parse(Objects.requireNonNull(response.getBody()), MqttSessionDetailsResultVO.class);
            }
            log.warn("Failed to retrieve session info. Status: {}, tenantId: {}, userId: {}, clientId: {}",
                    response.getStatusCode(), tenantId, userId, clientId);
            throw new BizException("Session not found or failed to retrieve session info.");
        } catch (FeignException.NotFound e) {
            log.info("Session not found for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            throw new BizException("Session not found.", e);
        } catch (FeignException e) {
            log.error("FeignException while retrieving session info for tenantId: {}, userId: {}, clientId: {}. Status: {}, Message: {}",
                    tenantId, userId, clientId, e.status(), e.getMessage());
            throw new Exception("Error occurred while retrieving session info.", e);
        }
    }

    @Override
    public void expireSession(String tenantId, String expirySeconds) throws BizException {
        log.info("Expiring sessions for tenantId: {} with expirySeconds: {}", tenantId, expirySeconds);
        try {
            ResponseEntity<String> response = bifroMQApi.expireSession(Long.valueOf(SnowflakeIdUtil.nextId()), tenantId, expirySeconds);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully expired sessions for tenantId: {}", tenantId);
            } else {
                log.error("Failed to expire sessions. Response status: {}", response.getStatusCode());
                throw new BizException("Failed to expire sessions for tenantId: " + tenantId);
            }
        } catch (Exception e) {
            log.error("Error occurred while expiring sessions: {}", e.getMessage());
            throw new BizException("Error during session expiration: " + e.getMessage());
        }
    }

    @Override
    public void killClientConnection(String tenantId, String userId, String clientId, String clientType) throws BizException {
        log.info("Killing client connection for tenantId: {}, userId: {}, clientId: {}, clientType: {}", tenantId, userId, clientId, clientType);
        try {
            ResponseEntity<String> response = bifroMQApi.killClientConnection(Long.valueOf(SnowflakeIdUtil.nextId()), tenantId, userId, clientId, clientType, Collections.emptyMap());
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully killed client connection for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            } else {
                log.error("Failed to kill client connection. Response status: {}", response.getStatusCode());
                throw new BizException("Failed to kill client connection for tenantId: " + tenantId);
            }
        } catch (Exception e) {
            log.error("Error occurred while killing client connection: {}", e.getMessage());
            throw new BizException("Error during client disconnection: " + e.getMessage());
        }
    }

    @Override
    public void addTopicSubscription(String tenantId, String userId, String clientId, String topicFilter, String subQos) throws BizException {
        log.info("Adding topic subscription for tenantId: {}, userId: {}, clientId: {}, topicFilter: {}, subQos: {}",
                tenantId, userId, clientId, topicFilter, subQos);
        try {
            ResponseEntity<String> response = bifroMQApi.addTopicSubscription(Long.valueOf(SnowflakeIdUtil.nextId()), tenantId, userId, clientId, topicFilter, subQos);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully added topic subscription for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            } else {
                log.error("Failed to add topic subscription. Response status: {}", response.getStatusCode());
                throw new BizException("Failed to add topic subscription for tenantId: " + tenantId);
            }
        } catch (Exception e) {
            log.error("Error occurred while adding topic subscription: {}", e.getMessage());
            throw new BizException("Error during topic subscription: " + e.getMessage());
        }
    }

    @Override
    public void removeTopicSubscription(String tenantId, String userId, String clientId, String topicFilter) throws BizException {
        log.info("Removing topic subscription for tenantId: {}, userId: {}, clientId: {}, topicFilter: {}",
                tenantId, userId, clientId, topicFilter);
        try {
            ResponseEntity<String> response = bifroMQApi.removeTopicSubscription(Long.valueOf(SnowflakeIdUtil.nextId()), tenantId, userId, clientId, topicFilter);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully removed topic subscription for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            } else {
                log.error("Failed to remove topic subscription. Response status: {}", response.getStatusCode());
                throw new BizException("Failed to remove topic subscription for tenantId: " + tenantId);
            }
        } catch (Exception e) {
            log.error("Error occurred while removing topic subscription: {}", e.getMessage());
            throw new BizException("Error during topic unsubscription: " + e.getMessage());
        }
    }

}
