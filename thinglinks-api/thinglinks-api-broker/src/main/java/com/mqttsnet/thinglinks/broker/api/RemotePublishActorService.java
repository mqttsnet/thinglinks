package com.mqttsnet.thinglinks.broker.api;

import com.mqttsnet.thinglinks.broker.api.factory.RemotePublishActorFallbackFactory;
import com.mqttsnet.thinglinks.common.core.constant.ServiceNameConstants;
import com.mqttsnet.thinglinks.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @Description: Broker推送设备消息
 * @Author: ShiHuan SUN
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2022/1/14$ 16:46$
 * @UpdateUser: ShiHuan SUN
 * @UpdateDate: 2022/1/14$ 16:46$
 * @UpdateRemark: 修改内容
 * @Version: V1.0
 */
@FeignClient(contextId = "remotePublishActorService", value = ServiceNameConstants.THINGLINKS_BROKER, fallbackFactory = RemotePublishActorFallbackFactory.class)
public interface RemotePublishActorService {

    /**
     * 通知ThingLins MQTT Broker推送消息
     * @param params
     * @return
     */
    @PostMapping("/sendMessage")
    public R sendMessage(@RequestBody Map<String, String> params);
}
