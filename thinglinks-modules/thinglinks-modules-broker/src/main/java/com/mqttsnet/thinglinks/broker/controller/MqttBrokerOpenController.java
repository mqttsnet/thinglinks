package com.mqttsnet.thinglinks.broker.controller;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.broker.api.domain.vo.PublishMessageRequestVO;
import com.mqttsnet.thinglinks.broker.service.MqttBrokerService;
import com.mqttsnet.thinglinks.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MqttBroker相关开放接口（anyTenant）
 *
 * @author mqttsnet
 * @date 2023-05-22
 * @create [2021-06-30] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/mqttBrokerOpen")
@Api(value = "MqttBrokerOpenController", tags = "开放接口-MQTTBroker")
public class MqttBrokerOpenController {

    @Autowired
    private MqttBrokerService mqttBrokerService;

    /**
     * MQTT推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    @ApiOperation(value = "MQTT推送消息", notes = "根据提供的主题、服务质量等级、保留标志和消息内容推送MQTT消息")
    @PostMapping("/sendMessage")
    public R<?> sendMessage(@ApiParam(value = "推送消息请求参数", required = true)
                            @RequestBody PublishMessageRequestVO publishMessageRequestVO) {
        log.info("MQTT Broker publish {}", publishMessageRequestVO.toString());
        return R.ok(mqttBrokerService.publishMessage(publishMessageRequestVO));
    }


    /**
     * 关闭客户端连接
     * TODO 暂时不可用
     *
     * @param clientIdentifiers 客户端标识集合
     * @return {@link R} 结果
     */
    @ApiOperation(value = "关闭连接", notes = "关闭指定客户端的连接")
    @PostMapping("/close/connection")
    public R closeConnection(@RequestBody @ApiParam(value = "客户端标识符列表") List<String> clientIdentifiers) {
        log.info("MQTT Broker 关闭连接 {}", clientIdentifiers.toString());
        JSONObject param = new JSONObject();
        param.put("ids", clientIdentifiers);
        String result = HttpRequest.post("http://127.0.0.1:60000/smqtt/close/connection")
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(param.toString())
                .execute().body();
        return R.ok(result);
    }

}
