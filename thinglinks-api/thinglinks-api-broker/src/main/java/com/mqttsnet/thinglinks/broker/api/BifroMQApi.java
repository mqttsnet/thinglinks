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
    @PostMapping(path = "/pub", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<String> publishMessage(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader(name = "tenant_id") String tenantId,
            @RequestHeader(name = "topic") String topic,
            @RequestHeader(name = "client_type") String clientType,
            @RequestHeader(name = "pub_qos") String pubQos,
            @RequestHeader(name = "retain", required = false) String retain,
            @RequestHeader(name = "client_meta_*", required = false) String clientMeta,
            @RequestBody String payload
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
            @RequestHeader(name = "client_meta_*", required = false) String clientMeta  // Note: This logic needs further implementation
    );

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
    @PutMapping("/sub")
    ResponseEntity<String> addTopicSubscription(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader("tenant_id") String tenantId,
            @RequestHeader("topic_filter") String topicFilter,
            @RequestHeader("sub_qos") String subQos,
            @RequestHeader("inbox_id") String inboxId,
            @RequestHeader(name = "deliverer_key", required = false) String delivererKey,
            @RequestHeader("subbroker_id") Integer subBrokerId
    );

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
    @DeleteMapping("/unsub")
    ResponseEntity<String> removeTopicSubscription(
            @RequestHeader(name = "req_id", required = false) Long reqId,
            @RequestHeader("tenant_id") String tenantId,
            @RequestHeader("topic_filter") String topicFilter,
            @RequestHeader("inbox_id") String inboxId,
            @RequestHeader(name = "deliverer_key", required = false) String delivererKey,
            @RequestHeader("subbroker_id") Integer subBrokerId
    );

}
