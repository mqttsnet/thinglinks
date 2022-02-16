//package com.mqttsnet.thinglinks.link.controller.kafka;
//
//import com.mqttsnet.thinglinks.link.common.kafka.producer.KafkaProducer;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
///**
// * @Description: kafka接口
// * @Author: ShiHuan Sun
// * @E-mail: 13733918655@163.com
// * @Website: http://thinglinks.mqttsnet.com
// * @CreateDate: 2021/12/1$ 11:39$
// * @UpdateUser: ShiHuan Sun
// * @UpdateDate: 2021/12/1$ 11:39$
// * @UpdateRemark: 修改内容
// * @Version: 1.0
// */
//@RestController
//@RequestMapping("/kafkaMessage")
//public class KafkaController {
//    @Autowired
//    private KafkaProducer kafkaProducer;
//
//    @PostMapping("/send")
//    public String kafkaMessageSend(@RequestBody Map<String, String> params){
//        kafkaProducer.sendMsg(String.valueOf(params.get("topic")),String.valueOf(params.get("msg")));
//        return "success-"+params.get("topic");
//    }
//}
