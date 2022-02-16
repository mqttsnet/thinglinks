//package com.mqttsnet.thinglinks.link.common.kafka.producer;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
//import org.springframework.stereotype.Component;
//import org.springframework.util.concurrent.ListenableFuture;
//import org.springframework.util.concurrent.ListenableFutureCallback;
//
///**
// * @Description: kafka生产者
// * @Author: ShiHuan Sun
// * @E-mail: 13733918655@163.com
// * @Website: http://thinglinks.mqttsnet.com
// * @CreateDate: 2021/11/27$ 16:04$
// * @UpdateUser: ShiHuan Sun
// * @UpdateDate: 2021/11/27$ 16:04$
// * @UpdateRemark: 修改内容
// * @Version: 1.0
// */
//@RefreshScope
//@Component
//@EnableKafka
//@Slf4j
//public class KafkaProducer {
//    @Autowired
//    private KafkaTemplate<Integer,String> kafkaTemplate;
//
//    //生产者
//    public void sendMsg(String topic , String msg){
//        log.info("开始发送kfk消息,topic:{},msg:{}",topic,msg);
//
//        ListenableFuture<SendResult<Integer, String>> sendMsg = kafkaTemplate.send(topic, msg);
//        //消息确认
//        sendMsg.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
//            @Override
//            public void onFailure(Throwable throwable) {
//                log.error("send error,ex:{},topic:{},msg:{}",throwable,topic,msg);
//            }
//
//            @Override
//            public void onSuccess(SendResult<Integer, String> stringStringSendResult) {
//                log.info("send success,topic:{},msg:{}",topic,msg);
//            }
//        });
//        log.info("kfk send end!");
//    }
//}
