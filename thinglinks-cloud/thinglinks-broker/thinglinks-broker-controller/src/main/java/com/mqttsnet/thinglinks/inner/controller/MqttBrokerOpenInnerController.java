package com.mqttsnet.thinglinks.inner.controller;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.broker.mqtt.service.MqttBrokerService;
import com.mqttsnet.thinglinks.vo.query.AddSubscriptionRequestVO;
import com.mqttsnet.thinglinks.vo.query.ExpireSessionRequestVO;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.query.RemoveSubscriptionRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * MqttBroker相关内部接口（inner）
 *
 * @author mqttsnet
 * @date 2023-05-22
 * @create [2021-06-30] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/mqttBrokerOpen")
@Tag(name = "inner-MQTTBroker")
public class MqttBrokerOpenInnerController {
    @Autowired
    private MqttBrokerService mqttBrokerService;

    /**
     * MQTT推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    @Operation(summary = "MQTT推送消息", description = "根据提供的主题、服务质量等级、保留标志和消息内容推送MQTT消息")
    @PostMapping("/sendMessage")
    public R<?> sendMessage(@Parameter(description = "推送消息请求参数", required = true) @Validated @RequestBody PublishMessageRequestVO publishMessageRequestVO) {
        log.info("MQTT Broker publish {}", publishMessageRequestVO.toString());
        try {
            return R.success(mqttBrokerService.publishMessage(publishMessageRequestVO));
        } catch (BizException e) {
            log.error("Failed to send message. param: {}", JSON.toJSONString(publishMessageRequestVO), e);
            return R.fail(e.getMessage());
        }
    }


    /**
     * Retrieves session information for a specified user and client ID from the MQTT broker.
     * This is useful for system administrators to monitor or manage MQTT session states.
     *
     * @param tenantId The tenant identifier under which the session is registered, required.
     * @param userId   The unique identifier of the user who established the session, required.
     * @param clientId The unique client identifier of the MQTT session, required.
     * @return {@link R<MqttSessionDetailsResultVO>} containing the session details or an error message if not found.
     */
    @Operation(
            summary = "查询会话信息",
            description = "根据租户ID、用户ID和客户端ID查询MQTT会话信息"
    )
    @GetMapping(path = "/session", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<MqttSessionDetailsResultVO> getSessionInfo(
            @Parameter(description = "租户标识，必填参数，用于标识需要查询的租户。", required = true, example = "1")
            @RequestParam(name = "tenantId") String tenantId,
            @Parameter(description = "用户标识，必填参数，用于标识需要查询的用户(同客户端标识即可)。", required = true, example = "0191794041999360@1")
            @RequestParam(name = "userId") String userId,
            @Parameter(description = "客户端标识，必填参数，用于标识需要查询的客户端。", required = true, example = "0191794041999360@1")
            @RequestParam(name = "clientId") String clientId
    ) {
        log.info("Received request to get session info for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);

        try {
            MqttSessionDetailsResultVO mqttSessionDetailsResultVO = mqttBrokerService.getSessionInfo(tenantId, userId, clientId);
            log.info("Successfully retrieved session info for tenantId: {}, userId: {}, clientId: {}", tenantId, userId, clientId);
            return R.success(mqttSessionDetailsResultVO);
        } catch (BizException e) {
            log.warn("Business exception while retrieving session info for tenantId: {}, userId: {}, clientId: {}. Error: {}", tenantId, userId, clientId, e.getMessage());
            return R.fail(R.FAIL_CODE, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while retrieving session info for tenantId: {}, userId: {}, clientId: {}. Error: {}", tenantId, userId, clientId, e.getMessage());
            return R.fail("Error retrieving session info", e.getMessage());
        }
    }

    /**
     * 查询设备 BifroMQ session 实时在线状态(三态语义).
     *
     * @param tenantId             租户 ID
     * @param deviceIdentification 设备标识(作为 BifroMQ userId)
     * @param clientId             MQTT clientId
     * @return {@link R#success(Object)} (true) 在线;{@link R#success(Object)} (false) 离线(broker 404);
     *         {@link R#fail()} 不确定(broker 临时异常 / 超时)
     */
    @Operation(
            summary = "查询设备 session 在线状态",
            description = "返回设备在 BifroMQ 的实时 session 是否存在(三态:在线/离线/不确定)"
    )
    @GetMapping(path = "/session/isOnline", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<Boolean> isOnline(
            @Parameter(description = "租户 ID", required = true, example = "1")
            @RequestParam(name = "tenantId") String tenantId,
            @Parameter(description = "设备标识(作为 BifroMQ userId)", required = true, example = "deviceA")
            @RequestParam(name = "deviceIdentification") String deviceIdentification,
            @Parameter(description = "MQTT clientId", required = true, example = "deviceA@1")
            @RequestParam(name = "clientId") String clientId
    ) {
        return mqttBrokerService.isOnline(tenantId, deviceIdentification, clientId);
    }

    /**
     * Manually expires inactive persistent sessions for a specified tenant using an overridden expiry time.
     * Setting the expiry_seconds to zero will clear all sessions under this tenant, potentially disconnecting live sessions.
     *
     * @param expireSessionRequest expireSessionRequest
     * @return R<?> indicating whether the operation was successful or failed.
     */
    @DeleteMapping("/session")
    public R<?> expireSession(
            @RequestBody ExpireSessionRequestVO expireSessionRequest
    ) {
        log.info("Received request to expire session for tenantId: {} with expirySeconds: {}",
                expireSessionRequest.getTenantId(), expireSessionRequest.getExpirySeconds());
        try {
            mqttBrokerService.expireSession(expireSessionRequest.getTenantId(), expireSessionRequest.getExpirySeconds());
            log.info("Successfully expired session for tenantId: {}", expireSessionRequest.getTenantId());
            return R.success();
        } catch (BizException e) {
            log.error("Error expiring session for tenantId: {}. Error: {}", expireSessionRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        } catch (Exception e) {
            log.error("Unexpected error expiring session for tenantId: {}. Error: {}", expireSessionRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        }
    }

    /**
     * Disconnects an MQTT client connection based on the provided parameters.
     *
     * @param disconnectRequest Contains tenantId, userId, clientId, and clientType.
     * @return R<?> indicating the result of the operation.
     */
    @DeleteMapping("/kill")
    public R<?> killClientConnection(
            @RequestBody KillClientRequestVO disconnectRequest
    ) {
        log.info("Received request to kill client connection for tenantId: {}, userId: {}, clientId: {}, clientType: {}",
                disconnectRequest.getTenantId(), disconnectRequest.getUserId(),
                disconnectRequest.getClientId(), disconnectRequest.getClientType());
        try {
            mqttBrokerService.killClientConnection(disconnectRequest.getTenantId(),
                    disconnectRequest.getUserId(),
                    disconnectRequest.getClientId(),
                    disconnectRequest.getClientType());
            log.info("Successfully killed client connection for tenantId: {}, userId: {}, clientId: {}",
                    disconnectRequest.getTenantId(), disconnectRequest.getUserId(), disconnectRequest.getClientId());
            return R.success();
        } catch (BizException e) {
            log.error("Error killing client connection for tenantId: {}. Error: {}", disconnectRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        } catch (Exception e) {
            log.error("Unexpected error killing client connection for tenantId: {}. Error: {}", disconnectRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        }
    }

    /**
     * Adds a topic subscription to an MQTT session.
     *
     * @param addSubscriptionRequest Contains tenantId, userId, clientId, topicFilter, and subQos.
     * @return R<?> indicating whether the operation was successful or failed.
     */
    @PutMapping("/sub")
    public R<?> addTopicSubscription(@RequestBody AddSubscriptionRequestVO addSubscriptionRequest) {
        log.info("Received request to add topic subscription for tenantId: {}, userId: {}, clientId: {}, topicFilter: {}, subQos: {}",
                addSubscriptionRequest.getTenantId(), addSubscriptionRequest.getUserId(),
                addSubscriptionRequest.getClientId(), addSubscriptionRequest.getTopicFilter(),
                addSubscriptionRequest.getSubQos());
        try {
            mqttBrokerService.addTopicSubscription(addSubscriptionRequest.getTenantId(),
                    addSubscriptionRequest.getUserId(),
                    addSubscriptionRequest.getClientId(),
                    addSubscriptionRequest.getTopicFilter(),
                    addSubscriptionRequest.getSubQos());
            log.info("Successfully added topic subscription for tenantId: {}, userId: {}, clientId: {}",
                    addSubscriptionRequest.getTenantId(), addSubscriptionRequest.getUserId(), addSubscriptionRequest.getClientId());
            return R.success();
        } catch (BizException e) {
            log.error("Error adding topic subscription for tenantId: {}. Error: {}", addSubscriptionRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        } catch (Exception e) {
            log.error("Unexpected error adding topic subscription for tenantId: {}. Error: {}", addSubscriptionRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        }
    }

    /**
     * Removes a topic subscription from a session.
     *
     * @param removeSubscriptionRequest Contains tenantId, userId, clientId, and topicFilter.
     * @return R<?> indicating whether the operation was successful or failed.
     */
    @DeleteMapping("/unsub")
    public R<?> removeTopicSubscription(
            @RequestBody RemoveSubscriptionRequestVO removeSubscriptionRequest
    ) {
        log.info("Received request to remove topic subscription for tenantId: {}, userId: {}, clientId: {}, topicFilter: {}",
                removeSubscriptionRequest.getTenantId(), removeSubscriptionRequest.getUserId(),
                removeSubscriptionRequest.getClientId(), removeSubscriptionRequest.getTopicFilter());
        try {
            mqttBrokerService.removeTopicSubscription(removeSubscriptionRequest.getTenantId(),
                    removeSubscriptionRequest.getUserId(),
                    removeSubscriptionRequest.getClientId(),
                    removeSubscriptionRequest.getTopicFilter());
            log.info("Successfully removed topic subscription for tenantId: {}, userId: {}, clientId: {}",
                    removeSubscriptionRequest.getTenantId(), removeSubscriptionRequest.getUserId(), removeSubscriptionRequest.getClientId());
            return R.success();
        } catch (BizException e) {
            log.error("Error removing topic subscription for tenantId: {}. Error: {}", removeSubscriptionRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        } catch (Exception e) {
            log.error("Unexpected error removing topic subscription for tenantId: {}. Error: {}", removeSubscriptionRequest.getTenantId(), e.getMessage());
            return R.fail(e);
        }
    }

}
