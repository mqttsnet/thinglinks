package com.mqttsnet.thinglinks.broker.api.factory;

import com.mqttsnet.thinglinks.broker.api.RemotePublishActorService;
import com.mqttsnet.thinglinks.common.core.domain.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @Description: Broker推送设备消息降级处理
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/1/14$ 16:49$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/1/14$ 16:49$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@Component
public class RemotePublishActorFallbackFactory implements FallbackFactory<RemotePublishActorService> {
    private static final Logger log = LoggerFactory.getLogger(RemotePublishActorFallbackFactory.class);

    @Override
    public RemotePublishActorService create(Throwable throwable) {
        log.error("Broker推送设备消息服务调用失败:{}", throwable.getMessage());
        return new RemotePublishActorService() {


            /**
             * 通知ThingLins MQTT Broker推送消息
             *
             * @param params
             * @return
             */
            @Override
            public R sendMessage(Map<String, Object> params) {
                return R.fail("通知ThingLins MQTT Broker推送消息失败:" + throwable.getMessage());
            }

            /**
             * 通知ThingLins MQTT Broker断开链接
             * @param clientIdentifiers  客户端ID
             * @return
             */
            @Override
            public R closeConnection(@RequestBody List<String> clientIdentifiers){
                return R.fail("通知ThingLins MQTT Broker推送消息失败:" + throwable.getMessage());
            }

        };
    }
}
