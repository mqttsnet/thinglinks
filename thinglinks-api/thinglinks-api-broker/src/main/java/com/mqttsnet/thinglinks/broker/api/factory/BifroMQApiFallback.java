package com.mqttsnet.thinglinks.broker.api.factory;

import com.mqttsnet.thinglinks.broker.api.BifroMQApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * -----------------------------------------------------------------------------
 * File Name: BifroMQApiFallback.java
 * -----------------------------------------------------------------------------
 * Description:
 * BifroMQApi API熔断
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
 * @date 2023-10-31 12:31
 */
@Slf4j
@Component
public class BifroMQApiFallback implements FallbackFactory<BifroMQApi> {

    @Override
    public BifroMQApi create(Throwable throwable) {
        log.error("Broker推送设备消息服务调用失败:{}", throwable.getMessage());

        return new BifroMQApi() {


            @Override
            public ResponseEntity<String> publishMessage(Long reqId, String tenantId, String topic, String qos, String expirySeconds, String clientType, Map<String, String> clientMetadata, String payload) {
                return new ResponseEntity<>("BifroMQApiFallback.publishMessage() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> retainMessage(Long reqId, String tenantId, String topic, String qos, String expirySeconds, String clientType, Map<String, String> clientMetadata, String payload) {
                return new ResponseEntity<>("BifroMQApiFallback.retainMessage() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> getSessionInfo(Long reqId, String tenantId, String userId, String clientId) {
                return new ResponseEntity<>("BifroMQApiFallback.getSessionInfo() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> expireSession(Long reqId, String tenantId, String expirySeconds) {
                return new ResponseEntity<>("BifroMQApiFallback.expireSession() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> expireInbox(Long reqId, String tenantId, String expirySeconds) {
                return new ResponseEntity<>("BifroMQApiFallback.expireInbox() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> killClientConnection(Long reqId, String tenantId, String userId, String clientId, String clientType, String clientMeta) {
                return new ResponseEntity<>("BifroMQApiFallback.killClientConnection() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> addTopicSubscription(Long reqId, String tenantId, String userId, String clientId, String topicFilter, String subQos) {
                return new ResponseEntity<>("BifroMQApiFallback.addTopicSubscription() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            @Override
            public ResponseEntity<String> removeTopicSubscription(Long reqId, String tenantId, String userId, String clientId, String topicFilter) {
                return new ResponseEntity<>("BifroMQApiFallback.removeTopicSubscription() Service call failure e:{}", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }


}
