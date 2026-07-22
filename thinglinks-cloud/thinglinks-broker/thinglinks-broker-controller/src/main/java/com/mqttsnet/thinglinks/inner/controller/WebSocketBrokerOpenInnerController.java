package com.mqttsnet.thinglinks.inner.controller;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.thinglinks.broker.ws.service.WebSocketBrokerService;
import com.mqttsnet.thinglinks.vo.query.PublishWebSocketMessageRequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebSocket 相关内部接口（inner）
 *
 * @author mqttsnet
 * @date 2023-05-22
 * @create [2021-06-30] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/webSocketBrokerOpen")
@Tag(name = "inner-WebSocketBroker")
public class WebSocketBrokerOpenInnerController {
    @Autowired
    private WebSocketBrokerService webSocketBrokerService;

    /**
     * WebSocket 推送消息接口
     *
     * @param publishMessageRequestVO 推送消息请求参数
     * @return {@link R} 结果
     */
    @Operation(summary = "WebSocket 推送消息", description = "根据提供的主题、服务质量等级、保留标志和消息内容推送WebSocket消息")
    @PostMapping("/sendMessage")
    public R<?> sendMessage(@Parameter(description = "推送消息请求参数", required = true)
                            @RequestBody PublishWebSocketMessageRequestVO publishMessageRequestVO) {
        log.info("WebSocket Broker publish {}", publishMessageRequestVO.toString());
        try {
            return R.success(webSocketBrokerService.publishMessage(publishMessageRequestVO));
        } catch (BizException e) {
            log.error("Failed to send message. param: {}", JSON.toJSONString(publishMessageRequestVO), e);
            return R.fail(e.getMessage());
        }
    }

}
