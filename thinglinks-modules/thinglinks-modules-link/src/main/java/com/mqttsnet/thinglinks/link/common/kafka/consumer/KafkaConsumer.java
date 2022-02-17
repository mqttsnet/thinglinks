//package com.mqttsnet.thinglinks.link.common.kafka.consumer;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.util.Optional;
//
///**
// * @Description: kafka消费者
// * @Author: ShiHuan Sun
// * @E-mail: 13733918655@163.com
// * @Website: http://thinglinks.mqttsnet.com
// * @CreateDate: 2021/11/27$ 16:02$
// * @UpdateUser: ShiHuan Sun
// * @UpdateDate: 2021/11/27$ 16:02$
// * @UpdateRemark: 修改内容
// * @Version: 1.0
// */
//@RefreshScope
//@Component
//@EnableKafka
//@Slf4j
//public class KafkaConsumer {
//
//    //消费者：监听topic1
//    @KafkaListener(topics = {"thinglinks-link"})
//    public void consumer1(ConsumerRecord<Integer,String> record){
//        Optional message = Optional.ofNullable(record.value());
//        if (message.isPresent()) {
//            Object msg = message.get();
//            log.info("kafka消费： Topic:" + record.topic() + ",Message:" + msg);
//        }
//    }
//    //消费者：监听mqtts，groupId2
//    @KafkaListener(topics = {"thinglinks-link"},groupId = "groupId2")
//    public void consumer3(ConsumerRecord<Integer,String> record){
//        Optional message = Optional.ofNullable(record.value());
//        if (message.isPresent()) {
//            Object msg = message.get();
//            log.info("kafka消费： Topic:" + record.topic() + ",Message:" + msg);
//        }
//    }
//    //消费者：监听mqtts，groupId2
//    @KafkaListener(topics = {"thinglinks-link"},groupId = "groupId2")
//    public void consumer2(ConsumerRecord<Integer,String> record){
//        Optional message = Optional.ofNullable(record.value());
//        if (message.isPresent()) {
//            Object msg = message.get();
//            log.info("kafka消费： Topic:" + record.topic() + ",Message:" + msg);
//        }
//    }
//}
