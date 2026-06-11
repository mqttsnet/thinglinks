package com.mqttsnet.thinglinks.broker.api;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.broker.api.hystrix.MqttBrokerOpenAnyUserApiFallback;
import com.mqttsnet.thinglinks.vo.query.KillClientRequestVO;
import com.mqttsnet.thinglinks.vo.query.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.vo.result.MqttSessionDetailsResultVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: thinglinks-cloud
 * @description: MqttBroker-开放接口API
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-05-06 12:35
 **/
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:thinglinks-broker-server}", fallback = MqttBrokerOpenAnyUserApiFallback.class, path = "/anyUser/mqttBrokerOpen")
public interface MqttBrokerOpenAnyUserApi {


    /**
     * MQTT推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    @Operation(summary = "MQTT推送消息", description = "根据提供的主题、服务质量等级、保留标志和消息内容推送MQTT消息")
    @PostMapping(path = "/sendMessage", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<?> sendMessage(@RequestBody PublishMessageRequestVO publishMessageRequestVO);

    /**
     * 关闭客户端连接
     *
     * @param killClientRequestVO 关闭客户端请求参数
     * @return {@link R} 结果
     */
    @Operation(summary = "关闭连接", description = "关闭指定客户端的连接")
    @DeleteMapping(path = "/kill", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<?> closeConnection(@RequestBody KillClientRequestVO killClientRequestVO);


    /**
     * Retrieves session information for a specified user and client ID from the MQTT broker.
     * This is useful for system administrators to monitor or manage MQTT session states.
     *
     * @param tenantId The tenant identifier under which the session is registered, required.
     * @param userId   The unique identifier of the user who established the session, required.
     * @param clientId The unique client identifier of the MQTT session, required.
     * @return {@link R<MqttSessionDetailsResultVO>}  containing the session details or an error message if not found.
     */
    @Operation(summary = "查询会话信息", description = "根据租户ID、用户ID和客户端ID查询MQTT会话信息")
    @GetMapping(path = "/session", consumes = MediaType.APPLICATION_JSON_VALUE)
    R<MqttSessionDetailsResultVO> getSessionInfo(@RequestParam(name = "tenantId") String tenantId, @RequestParam(name = "userId") String userId, @RequestParam(name = "clientId") String clientId);

    /**
     * 查询设备 BifroMQ session 实时在线状态(三态语义).
     *
     * @param tenantId             租户 ID
     * @param deviceIdentification 设备标识(作为 BifroMQ userId)
     * @param clientId             MQTT clientId
     * @return {@link R#success(Object)} {@code (true)} 在线;{@link R#success(Object)} {@code (false)} 离线(broker 404);
     *         {@link R#fail()} 不确定(broker 临时异常 / 超时,调用方应保留现状)
     */
    @Operation(summary = "查询设备 session 在线状态", description = "返回设备在 BifroMQ 的实时 session 是否存在(三态)")
    @GetMapping(path = "/session/isOnline")
    R<Boolean> isOnline(@RequestParam(name = "tenantId") String tenantId,
                        @RequestParam(name = "deviceIdentification") String deviceIdentification,
                        @RequestParam(name = "clientId") String clientId);
}
