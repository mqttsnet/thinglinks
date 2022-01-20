package com.mqttsnet.thinglinks.tdengine.consumer;

import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.tdengine.service.ProductSuperTableCreateOrUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: TDengine超级表创键修改动作监听（Rocketmq模式）
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/11/22$ 16:11$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/22$ 16:11$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@Component
@RocketMQMessageListener(consumerGroup = "thinglinks-tdengine", topic = "productSuperTable-createOrUpdate")
public class ProductCreateSuperTableMessageConsumer implements RocketMQListener {

    @Autowired
    private ProductSuperTableCreateOrUpdateService productSuperTableCreateOrUpdateService;

    /**
     * 超级表创建及修改处理
     * @param message
     */
    @Override
    public void onMessage(Object message) {
        assert message!=null;
        JSONObject stableMessage = JSONObject.parseObject(String.valueOf(message));
        log.info("TDengine消费{}超级表消息:{}"+stableMessage.get("type")+stableMessage.get("msg"));
        if("create".equals(stableMessage.get("type"))){
            productSuperTableCreateOrUpdateService.createProductSuperTable(String.valueOf(stableMessage.get("msg")));
        }else if("update".equals(stableMessage.get("type"))){
            productSuperTableCreateOrUpdateService.updateProductSuperTable(String.valueOf(stableMessage.get("msg")));
        }

    }
}
