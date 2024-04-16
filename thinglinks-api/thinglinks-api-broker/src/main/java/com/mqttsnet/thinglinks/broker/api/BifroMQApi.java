package com.mqttsnet.thinglinks.broker.api;

import com.mqttsnet.thinglinks.common.core.constant.Constants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * -----------------------------------------------------------------------------
 * File Name: BifroMQApi.java
 * -----------------------------------------------------------------------------
 * Description:
 * BifroMQ  Api
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
 * @date 2023-10-31 12:20
 */
@FeignClient(name = "BifroMQApi", url = "${" + Constants.PROJECT_PREFIX + ".feign.bifromq.bifromq-api-server:http://127.0.0.1:8091}", path = "/")
public interface BifroMQApi {

    /**
     * Publish a message to a given topic.
     *
     * @param reqId          Optional caller provided request id, expected as a Long.
     * @param tenantId       The tenant ID, required.
     * @param topic          The message topic, required.
     * @param qos            QoS of the message to be published, required.
     * @param expirySeconds  The message expiry seconds, optional.
     * @param clientType     The publisher type, required.
     * @param clientMetadata Metadata headers about the publisher, must start with 'client_meta_', optional.
     * @param payload        Message payload will be treated as binary, required.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping(path = "/pub", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> publishMessage(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader(name = "tenant_id", required = true) String tenantId,
            @RequestHeader(name = "topic", required = true) String topic,
            @RequestHeader(name = "qos", required = true) String qos,
            @RequestHeader(name = "expiry_seconds", required = false) String expirySeconds,
            @RequestHeader(name = "client_type", required = true) String clientType,
            @RequestHeader(name = "client_meta_*", required = false) String clientMetadata,
            @RequestBody byte[] payload
    );

    /**
     * Manually expires the inbox based on the provided parameters.
     *
     * @param reqId         Optional caller provided request id.
     * @param tenantId      The tenant id.
     * @param expirySeconds The inbox's expiry time.
     * @return ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/expireinbox")
    ResponseEntity<String> expireInbox(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader("tenant_id") String tenantId,
            @RequestHeader("expiry_seconds") String expirySeconds
    );

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
    @DeleteMapping("/kill")
    ResponseEntity<String> killClientConnection(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader("tenant_id") String tenantId,
            @RequestHeader("user_id") String userId,
            @RequestHeader("client_id") String clientId,
            @RequestHeader("client_type") String clientType,
            @RequestHeader(name = "client_meta_*", required = false) String clientMeta
    );

    /**
     * Adds a topic subscription to an MQTT session.
     *
     * @param reqId       Optional caller provided request id.
     * @param tenantId    The tenant id.
     * @param userId      The id of user who established the session.
     * @param clientId    The client id of the MQTT session.
     * @param topicFilter The topic filter to add.
     * @param subQos      The QoS of the subscription.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PutMapping("/sub")
    ResponseEntity<String> addTopicSubscription(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader(name = "tenant_id", required = true) String tenantId,
            @RequestHeader(name = "user_id", required = true) String userId,
            @RequestHeader(name = "client_id", required = true) String clientId,
            @RequestHeader(name = "topic_filter", required = true) String topicFilter,
            @RequestHeader(name = "sub_qos", required = true) String subQos
    );

    /**
     * Removes a topic subscription from an inbox.
     *
     * @param reqId       Optional caller provided request id.
     * @param tenantId    The tenant id.
     * @param userId      The id of user who established the session.
     * @param clientId    The client id of the MQTT session.
     * @param topicFilter The topic filter to add.
     * @return ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/unsub")
    ResponseEntity<String> removeTopicSubscription(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader(name = "tenant_id", required = true) String tenantId,
            @RequestHeader(name = "user_id", required = true) String userId,
            @RequestHeader(name = "client_id", required = true) String clientId,
            @RequestHeader(name = "topic_filter", required = true) String topicFilter
    );

}
