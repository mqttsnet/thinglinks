package com.mqttsnet.thinglinks.broker.Actors;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
@RequestMapping("/close")
public class CloseConnectionActor {

    /**
     * 关闭连接
     * @param clientIdentifiers
     * @return
     */
    @PostMapping("/connection")
    public R closeConnection(@RequestBody List<String> clientIdentifiers) {
        log.info("MQTT Broker 关闭连接 {}", clientIdentifiers.toString());
        JSONObject param = new JSONObject();
        param.put("ids", clientIdentifiers);
        String result = HttpRequest.post("http://127.0.0.1:60000/smqtt/close/connection")
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(param.toString())
                .execute().body();
        return R.ok();
    }
}
