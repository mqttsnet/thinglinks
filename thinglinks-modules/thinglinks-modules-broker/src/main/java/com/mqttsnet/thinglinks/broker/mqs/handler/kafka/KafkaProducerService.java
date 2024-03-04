package com.mqttsnet.thinglinks.broker.mqs.handler.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @Description: kafka生产者
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @CreateDate: 2023/01/06$ 16:02$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2023/01/06$ 16:02$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    @Qualifier("thingLinksKafkaTemplate")
    private KafkaTemplate thingLinksKafkaTemplate;


    public void thingLinksKafkaTemplateSendMsg(String topic, String msg) {
        log.info("thingLinksKafkaTemplate sendMsg ,topic:{},msg:{}", topic, msg);

        ListenableFuture<SendResult<Integer, String>> sendMsg = thingLinksKafkaTemplate.send(topic, msg);
        //消息确认
        sendMsg.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("send error,ex:{},topic:{},msg:{}", throwable, topic, msg);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> stringStringSendResult) {
                log.info("send success,topic:{},msg:{}", topic, msg);
            }
        });
        log.info("kafka send end!");
    }
}
