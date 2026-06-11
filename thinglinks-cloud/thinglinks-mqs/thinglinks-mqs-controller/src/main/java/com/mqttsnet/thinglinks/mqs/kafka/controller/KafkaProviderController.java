package com.mqttsnet.thinglinks.mqs.kafka.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mqttsnet.basic.kafka.producer.KafkaProducerService;
import com.mqttsnet.basic.kafka.producer.KafkaSendResultHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: thinglinks-cloud
 * @description: KafkaController
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-06-18 11:37
 **/
@Slf4j
@RestController
@RequestMapping("/kafka")
//这个注解代表这个类开启Springboot事务，因为我们在Kafka的配置文件开启了Kafka事务，不然会报错
@Transactional(rollbackFor = RuntimeException.class)
@Tag(name = "Kafka接口")
public class KafkaProviderController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaProducerService kafkaProducerService;

    public KafkaProviderController(KafkaTemplate<String, String> kafkaTemplate, KafkaSendResultHandler kafkaSendResultHandler) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplate.setProducerListener(kafkaSendResultHandler);
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息到指定的主题", description = "发送消息到指定的主题")
    public void sendMessage(@RequestParam String topic, @RequestParam String message) {
        kafkaProducerService.thingLinksKafkaTemplateSendMsg(topic, message);
        log.info("消息发送成功：topic={}, message={}", topic, message);
    }

    @PostMapping("/send/partition")
    @Operation(summary = "发送消息到指定的主题，并指定消息的分区", description = "发送消息到指定的主题，并指定消息的分区")
    public void sendMessageToPartition(@RequestParam String topic, @RequestParam int partition, @RequestParam String message) {
        kafkaTemplate.send(topic, partition, null, message);
        log.info("消息发送成功：topic={}, partition={}, message={}", topic, partition, message);
    }

    @PostMapping("/send/with-key")
    @Operation(summary = "发送带有键的消息到指定的主题", description = "发送带有键的消息到指定的主题")
    public void sendMessageWithKey(@RequestParam String topic, @RequestParam String key, @RequestParam String message) {
        kafkaTemplate.send(topic, key, message);
        log.info("消息发送成功：topic={}, key={}, message={}", topic, key, message);
    }

    @PostMapping("/send/batch")
    @Operation(summary = "批量发送消息到指定的主题", description = "批量发送消息到指定的主题")
    public void sendBatchMessages(@RequestParam String topic, @RequestBody List<String> messages) {
        messages.forEach(message -> {
            kafkaTemplate.send(topic, message);
            log.info("消息发送成功：topic={}, message={}", topic, message);
        });
    }

    @PostMapping("/send/async")
    @Operation(summary = "异步发送消息到指定的主题", description = "异步发送消息到指定的主题")
    public void sendAsyncMessage(@RequestParam String topic, @RequestParam String message) {
//        TODO mqttsnet
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
//        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
//            @Override
//            public void onSuccess(SendResult<String, String> result) {
//                // 消息发送成功处理逻辑
//                log.info("消息发送成功：{}", result);
//            }
//
//            @Override
//            public void onFailure(Throwable ex) {
//                // 消息发送失败处理逻辑
//                log.error("消息发送失败：{}", ex.getMessage());
//            }
//        });
    }

}
