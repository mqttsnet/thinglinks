package com.mqttsnet.thinglinks.tdengine.common.consumer.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.kafka.constant.ConsumerTopicConstant;
import com.mqttsnet.thinglinks.common.rocketmq.constant.ConsumerGroupConstant;
import com.mqttsnet.thinglinks.tdengine.service.ProductSuperTableCreateOrUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @Description: TDengine超级表创键修改动作监听（kafka模式）
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
//@Component
public class ProductCreateSuperTableMessageKafkaConsumer {

    @Autowired
    private ProductSuperTableCreateOrUpdateService productSuperTableCreateOrUpdateService;

    /**
     * 超级表创建及修改处理
     *
     * @param record
     */
    @KafkaListener(topics = {ConsumerTopicConstant.PRODUCTSUPERTABLE_CREATEORUPDATE})
    public void onMessage(ConsumerRecord<?, ?> record) {

        try {
            // 消息处理逻辑
            if (null == record) {
                log.warn("message cannot be empty {}", record);
                return;
            }

            Object message = JSONObject.parse(String.valueOf(record.value()));
            JSONObject stableMessage = JSONObject.parseObject(message.toString());
            log.info("TDengine消费{}超级表消息:{}", stableMessage.get("type"), stableMessage.get("msg"));
            if ("create".equals(stableMessage.get("type"))) {
                try {
                    productSuperTableCreateOrUpdateService.createProductSuperTable(String.valueOf(stableMessage.get("msg")));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            } else if ("update".equals(stableMessage.get("type"))) {
                productSuperTableCreateOrUpdateService.updateProductSuperTable(String.valueOf(stableMessage.get("msg")));
            }
        } catch (Exception e) {
            // 记录具体错误信息和相关内容
            log.error(e.getMessage());
        }

    }
}
