package com.mqttsnet.thinglinks.broker.service.impl;

import com.mqttsnet.thinglinks.broker.api.BifroMQApi;
import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.broker.service.MqttBrokerService;
import com.mqttsnet.thinglinks.common.core.exception.base.BaseException;
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
    private final BifroMQApi bifroMQApi;


    /**
     * Publishes a message to a specified topic and returns the content if successful.
     *
     * @param publishMessageRequestVO Object containing the required parameters for publishing.
     * @return The content of the published message.
     * @throws BaseException If the publishing fails.
     */
    @Override
    public String publishMessage(PublishMessageRequestVO publishMessageRequestVO) throws BaseException {
        log.info("Preparing to publish message with topic: {}", publishMessageRequestVO.getTopic());
        try {
            log.info("Calling publish API to publish message with payload: {}", publishMessageRequestVO.getPayload().toString());
            ResponseEntity<String> response = callPublishApi(publishMessageRequestVO);

            log.info("Response from BifroMQApi to publishMessage: {}", response);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully published message with topic: {}", publishMessageRequestVO.getTopic());
                return publishMessageRequestVO.getPayload();
            } else {
                log.error("Failed to publish message with topic: {}. Response Status: {}",
                        publishMessageRequestVO.getTopic(), response.getStatusCode());
                throw new BaseException("Failed to publish message with topic: " + publishMessageRequestVO.getTopic());
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error occurred while publishing message: {}", e.getMessage());
            throw new BaseException("Error during message publishing: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error occurred while publishing message: {}", e.getMessage());
            throw new BaseException("Unexpected error during message publishing: " + e.getMessage());
        }
    }


    /**
     * Makes the actual API call to publish a message.
     *
     * @param publishMessageRequestVO Object containing the required parameters for publishing.
     * @return ResponseEntity indicating success or failure from the API.
     */
    private ResponseEntity<String> callPublishApi(PublishMessageRequestVO publishMessageRequestVO) {
        // TODO expirySeconds 设置为空，临时解决mqtt5.0版本消息过期问题
        return bifroMQApi.publishMessage(
                publishMessageRequestVO.getReqId(),
                publishMessageRequestVO.getTenantId(),
                publishMessageRequestVO.getTopic(),
                publishMessageRequestVO.getQos(),
                "",
                publishMessageRequestVO.getClientType(),
                publishMessageRequestVO.getClientMetadata(),
                publishMessageRequestVO.getPayload()
        );
    }
}
