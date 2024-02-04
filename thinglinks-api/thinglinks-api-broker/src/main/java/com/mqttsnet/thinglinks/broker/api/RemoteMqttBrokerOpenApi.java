package com.mqttsnet.thinglinks.broker.api;

import com.mqttsnet.thinglinks.broker.api.domain.model.PublishMessageRequest;
import com.mqttsnet.thinglinks.broker.api.factory.RemoteMqttBrokerOpenApiFallback;
import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Description: MqttBroker-开放接口API
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/1/14$ 16:46$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2024/1/14$ 16:46$
 * @UpdateRemark: 调整入参格式
 * @Version: V2.0
 */
@FeignClient(contextId = "remoteMqttBrokerOpenApi", value = ServiceNameConstants.THINGLINKS_BROKER, fallbackFactory = RemoteMqttBrokerOpenApiFallback.class)
public interface RemoteMqttBrokerOpenApi {


    /**
     * MQTT推送消息接口
     *
     * @param publishMessageRequest 推送消息请求参数
     * @return {@link R} 结果
     */
    @ApiOperation(value = "MQTT推送消息", notes = "根据提供的主题、服务质量等级、保留标志和消息内容推送MQTT消息")
    @PostMapping("/sendMessage")
    R<?> sendMessage(@ApiParam(value = "推送消息请求参数", required = true) @RequestBody PublishMessageRequest publishMessageRequest);

    /**
     * 关闭客户端连接
     *
     * @param clientIdentifiers 客户端标识集合
     * @return {@link R} 结果
     */
    @ApiOperation(value = "关闭连接", notes = "关闭指定客户端的连接")
    @PostMapping("/close/connection")
    R closeConnection(@RequestBody @ApiParam(value = "客户端标识符列表") List<String> clientIdentifiers);
}
