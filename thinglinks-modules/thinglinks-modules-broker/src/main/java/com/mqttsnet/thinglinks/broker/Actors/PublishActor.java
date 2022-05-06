package com.mqttsnet.thinglinks.broker.Actors;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Description: Broker推送设备消息
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2021/10/25$ 17:46$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/10/25$ 17:46$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/publish")
public class PublishActor{

    /**
     * MQTT推送消息接口
     * @param params
     * @return
     */
    @PostMapping("/sendMessage")
    public R sendMessage(@RequestBody Map<String, Object> params) {
        log.info("MQTT Broker publish {}", params.toString());
        JSONObject param = new JSONObject();
        param.put("topic", params.get("topic"));
        param.put("qos", Integer.valueOf(params.get("qos").toString()));
        param.put("retain", Boolean.valueOf(params.get("retain").toString()));
        param.put("message", String.valueOf(params.get("message")));
        String result = HttpRequest.post("http://127.0.0.1:60000/smqtt/publish")
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(param.toString())
                .execute().body();
        return R.ok();
    }
}
