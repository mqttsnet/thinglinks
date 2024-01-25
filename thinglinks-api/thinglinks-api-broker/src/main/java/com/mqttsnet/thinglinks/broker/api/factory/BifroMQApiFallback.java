package com.mqttsnet.thinglinks.broker.api.factory;

import com.mqttsnet.thinglinks.broker.api.BifroMQApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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

            /**
             * Publish a message to a given topic.
             *
             * @param reqId      Optional caller provided request id.
             * @param tenantId   The tenant id.
             * @param topic      The message topic.
             * @param clientType The client type.
             * @param pubQos     QoS of the message to be distributed.
             * @param retain     The message should be retained.
             * @param clientMeta Metadata header about the kicker client.
             * @param payload    Message payload.
             * @return Response indicating success or failure.
             */
            @Override
            public ResponseEntity<String> publishMessage(Long reqId, String tenantId, String topic, String clientType, String pubQos, String retain, String clientMeta, String payload) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            /**
             * Manually expires the inbox based on the provided parameters.
             *
             * @param reqId         Optional caller provided request id.
             * @param tenantId      The tenant id.
             * @param expirySeconds The inbox's expiry time.
             * @return ResponseEntity indicating the result of the operation.
             */
            @Override
            public ResponseEntity<String> expireInbox(Long reqId, String tenantId, String expirySeconds) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            /**
             * Disconnects a MQTT client connection based on the provided parameters.
             *
             * @param reqId      Optional caller provided request id.
             * @param tenantId   The tenant id.
             * @param userId     The user id of the MQTT client connection to be disconnected.
             * @param clientId   The client id of the MQTT client connection to be disconnected.
             * @param clientType The client type.
             * @param clientMeta Metadata header about the kicker client, must start with client_meta_.
             * @return ResponseEntity indicating the result of the operation.
             */
            @Override
            public ResponseEntity<String> killClientConnection(Long reqId, String tenantId, String userId, String clientId, String clientType, String clientMeta) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }


            /**
             * Adds a topic subscription to an inbox.
             *
             * @param reqId        Optional caller provided request id.
             * @param tenantId     The tenant id.
             * @param topicFilter  The topic filter to add.
             * @param subQos       The QoS of the subscription.
             * @param inboxId      The inbox for receiving subscribed messages.
             * @param delivererKey Deliverer key for subBroker.
             * @param subBrokerId  The ID of the subbroker hosting the inbox.
             * @return ResponseEntity indicating the result of the operation.
             */
            @Override
            public ResponseEntity<String> addTopicSubscription(Long reqId, String tenantId, String topicFilter, String subQos, String inboxId, String delivererKey, Integer subBrokerId) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            /**
             * Removes a topic subscription from an inbox.
             *
             * @param reqId        Optional caller provided request id.
             * @param tenantId     The tenant id.
             * @param topicFilter  The topic filter to remove.
             * @param inboxId      The inbox for receiving subscribed messages.
             * @param delivererKey Deliverer key for subBroker.
             * @param subBrokerId  The ID of the subbroker hosting the inbox.
             * @return ResponseEntity indicating the result of the operation.
             */
            @Override
            public ResponseEntity<String> removeTopicSubscription(Long reqId, String tenantId, String topicFilter, String inboxId, String delivererKey, Integer subBrokerId) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }


}
