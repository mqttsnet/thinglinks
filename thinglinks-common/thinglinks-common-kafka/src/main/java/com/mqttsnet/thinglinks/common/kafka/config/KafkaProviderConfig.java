package com.mqttsnet.thinglinks.common.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: thinglinks-cloud-pro-datasource-column
 * @description: KafkaProviderConfig
 * @packagename: com.mqttsnet.thinglinks.config.kafka
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2023-06-18 11:28
 **/
@Configuration
public class KafkaProviderConfig {

    @Value("${spring.kafka.thingLinks.producer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.thingLinks.producer.transaction-id-prefix}")
    private String transactionIdPrefix;
    @Value("${spring.kafka.thingLinks.producer.acks}")
    private String acks;
    @Value("${spring.kafka.thingLinks.producer.retries}")
    private String retries;
    @Value("${spring.kafka.thingLinks.producer.batch-size}")
    private String batchSize;
    @Value("${spring.kafka.thingLinks.producer.buffer-memory}")
    private String bufferMemory;

    @Bean
    public Map<String, Object> producerConfigs() {

        Map<String, Object> props = new HashMap<>(16);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        //acks=0 ： 生产者在成功写入消息之前不会等待任何来自服务器的响应。
        //acks=1 ： 只要集群的首领节点收到消息，生产者就会收到一个来自服务器成功响应。
        //acks=all ：只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应。
        //开启事务必须设为all
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        //发生错误后，消息重发的次数，开启事务必须大于0
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        //当多个消息发送到相同分区时,生产者会将消息打包到一起,以减少请求交互. 而不是一条条发送
        //批次的大小可以通过batch.size 参数设置.默认是16KB
        //较小的批次大小有可能降低吞吐量（批次大小为0则完全禁用批处理）。
        //比如说，kafka里的消息5秒钟Batch才凑满了16KB，才能发送出去。那这些消息的延迟就是5秒钟
        //实测batchSize这个参数没有用
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        //有的时刻消息比较少,过了很久,比如5min也没有凑够16KB,这样延时就很大,所以需要一个参数. 再设置一个时间,到了这个时间,
        //即使数据没达到16KB,也将这个批次发送出去
        props.put(ProducerConfig.LINGER_MS_CONFIG, "5000");
        //生产者内存缓冲区的大小
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        //反序列化，和生产者的序列化方式对应
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {

        DefaultKafkaProducerFactory<String, String> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        //开启事务，会导致 LINGER_MS_CONFIG 配置失效
        factory.setTransactionIdPrefix(transactionIdPrefix);
        return factory;
    }

    @Bean
    public KafkaTransactionManager<String, String> kafkaTransactionManager(ProducerFactory<String, String> producerFactory) {

        return new KafkaTransactionManager<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, String> thingLinksProKafkaTemplate() {

        return new KafkaTemplate<>(producerFactory());
    }
}