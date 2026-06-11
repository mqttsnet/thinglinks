package com.mqttsnet.thinglinks.broker.mqtt.service.impl;

import java.util.Collections;
import java.util.Objects;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.broker.BifroMqFacade;
import com.mqttsnet.thinglinks.broker.common.counter.DownLinkDataReportCounter;
import com.mqttsnet.thinglinks.broker.mqtt.exception.SessionNotFoundException;
import com.mqttsnet.thinglinks.broker.mqtt.service.MqttBrokerService;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * MqttBroker API 实现类
 *
 * <p>调用 BifroMQ HTTP API 完成 MQTT 协议层操作:发布消息、查询会话、过期会话、踢线、订阅/取消订阅。
 *
 * @author ShiHuan Sun
 * @email 13733918655@163.com
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MqttBrokerServiceImpl implements MqttBrokerService {
    private final BifroMqFacade bifroMQApi;

    private final DownLinkDataReportCounter downLinkDataReportCounter;


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
        // 下行数据下发计数(命令 / 消息下发)── broker 自维护,旁路统计不影响主链路
        downLinkDataReportCounter.incrementDownLink();
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
            // 非 2xx 但又不是 404 ── 走通用业务异常,由 isOnline 等调用方按 BizException 兜底处理为 R.fail
            throw new BizException("Failed to retrieve session info, status=" + response.getStatusCode());
        } catch (FeignException.NotFound e) {
            // 404 ── BifroMQ 明确返回 session 不存在;抛专属语义异常,避免调用方靠 message 字符串识别
            log.info("Session not found for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            throw new SessionNotFoundException("Session not found.", e);
        } catch (FeignException e) {
            log.error("FeignException while retrieving session info for tenantId: {}, userId: {}, clientId: {}. Status: {}, Message: {}",
                    tenantId, userId, clientId, e.status(), e.getMessage());
            throw new Exception("Error occurred while retrieving session info.", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>三态由异常类型驱动:{@link SessionNotFoundException}(broker 404)→ 离线;
     * {@link BizException} / 其它 {@link Exception} → 不确定(broker 临时不可达,调用方保留现状).
     */
    @Override
    public R<Boolean> isOnline(String tenantId, String deviceIdentification, String clientId) {
        if (StrUtil.hasBlank(tenantId, deviceIdentification, clientId)) {
            return R.fail("invalid params: tenantId/deviceIdentification/clientId required");
        }
        try {
            // userId 传 deviceIdentification (与 ACL/认证体系一致)
            MqttSessionDetailsResultVO info = getSessionInfo(tenantId, deviceIdentification, clientId);
            return R.success(info != null);
        } catch (SessionNotFoundException e) {
            // 真离线 ── broker 明确返回 not found
            return R.success(false);
        } catch (BizException e) {
            // broker 业务异常(非 not-found):状态不确定,调用方应保留现状
            log.warn("[Broker.isOnline] biz-error tenantId={} deviceId={} clientId={} cause={}",
                    tenantId, deviceIdentification, clientId, e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            // 网络/反序列化/未知异常 ── 状态不确定
            log.warn("[Broker.isOnline] unexpected tenantId={} deviceId={} clientId={}",
                    tenantId, deviceIdentification, clientId, e);
            return R.fail("broker unreachable");
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
