package com.mqttsnet.thinglinks.broker.mqs.consumer.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @program: thinglinks
 * @description: kafkaListener异常处理
 * @packagename: com.mqttsnet.thinglinks.common.kafka.handler
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-06-18 11:44
 **/
@Slf4j
@Component
public class MyKafkaListenerErrorHandler implements KafkaListenerErrorHandler {

    @Override
    @NonNull
    public Object handleError(@NonNull Message<?> message, @NonNull ListenerExecutionFailedException exception) {
        log.error("Error handling message: {}", message, exception);
        return new Object();
    }

    @Override
    @NonNull
    public Object handleError(@NonNull Message<?> message, @NonNull ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        log.error("Error handling message: {}", message, exception);
        log.info("Consumer details: {}", consumer.groupMetadata());
        log.info("Listener topic: {}", consumer.listTopics());
        return KafkaListenerErrorHandler.super.handleError(message, exception, consumer);
    }
}