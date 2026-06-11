package com.mqttsnet.thinglinks.config;

import lombok.Data;

@Data
public class EventCollectorConfig {

    /**
     * Kafka配置
     */
    private KafkaConfig kafka;

    /**
     * 获取完整的bootstrap servers地址
     *
     * @return Kafka服务器地址
     */
    public String getKafkaBootstrapServer() {
        return kafka != null ? kafka.getBootstrapServers() : null;
    }

    /**
     * 验证配置是否有效
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        if (kafka == null || kafka.getBootstrapServers() == null) {
            return false;
        }
        // 检查必要的生产者配置
        if (kafka.getProducer() != null) {
            ProducerConfig producer = kafka.getProducer();
            // 如果配置了SASL，必须提供完整的认证信息
            if (producer.getSecurityProtocol() != null &&
                producer.getSecurityProtocol().startsWith("SASL")) {
                if (producer.getSasl() == null ||
                    producer.getSasl().getJaas() == null ||
                    producer.getSasl().getJaas().getConfig() == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Kafka配置内部类
     */
    @Data
    public static class KafkaConfig {
        /**
         * 【必须】Kafka服务器地址列表
         * 格式：host1:port1,host2:port2
         */
        private String bootstrapServers;

        /**
         * 生产者配置
         */
        private ProducerConfig producer = new ProducerConfig();
    }

    /**
     * 生产者配置
     */
    @Data
    public static class ProducerConfig {
        // ==================== SASL认证配置 ====================
        /**
         * 【核心】安全协议配置
         * 可选值：SASL_PLAINTEXT, SASL_SSL, PLAINTEXT, SSL
         * 使用SASL认证时必须设置为 SASL_PLAINTEXT 或 SASL_SSL
         */
        private String securityProtocol;

        /**
         * SASL认证配置
         */
        private SaslConfig sasl = new SaslConfig();

        // ==================== 事务配置 ====================
        /**
         * 【核心】事务ID前缀
         * 开启事务时使用，必须在开启了事务的方法中发送消息
         * 格式：任意字符串 + "-"
         */
        private String transactionIdPrefix;

        // ==================== 重试和可靠性配置 ====================
        /**
         * 【核心】消息重试次数
         * 发生错误后，消息重发的次数
         * 开启事务时必须设置大于0，建议设置为5-10
         */
        private Integer retries;

        /**
         * 【核心】消息确认机制
         * acks=0：生产者在成功写入消息之前不会等待任何来自服务器的响应
         * acks=1：只要集群的首领节点收到消息，生产者就会收到一个来自服务器的成功响应
         * acks=all：只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应
         * 开启事务时，必须设置为all
         */
        private String acks;

        /**
         * 【核心】幂等性配置
         * 设置为true可以防止消息重复发送
         * 开启事务时建议设置为true
         */
        private Boolean enableIdempotence;

        // ==================== 批量提交配置 ====================
        /**
         * 【核心】批次大小
         * 当有多个消息需要被发送到同一个分区时，生产者会把它们放在同一个批次里
         * 该参数指定了一个批次可以使用的内存大小，按照字节数计算
         * 默认值：16384 (16KB)
         */
        private Integer batchSize;

        /**
         * 【核心】生产者内存缓冲区大小
         * 用于缓存待发送的消息批次
         * 默认值：33554432 (32MB)
         */
        private Long bufferMemory;

        /**
         * 【核心】消息延迟发送时间
         * 等待更多消息进入批次，优化吞吐量
         * 单位：毫秒，默认值：0
         */
        private Integer lingerMs;

        // ==================== 超时配置 ====================
        /**
         * 【核心】消息发送超时时间
         * 生产者尝试发送消息的最大时间，超过此时间未成功则放弃
         * 单位：毫秒，默认值：120000 (2分钟)
         */
        private Integer deliveryTimeoutMs;

        /**
         * 【核心】请求超时时间
         * 客户端等待请求响应的最长时间
         * 单位：毫秒，默认值：30000 (30秒)
         */
        private Integer requestTimeoutMs;

        /**
         * 【核心】最大阻塞时间
         * 当缓冲区满或元数据不可用时，生产者发送消息的最大阻塞时间
         * 单位：毫秒，默认值：60000 (1分钟)
         */
        private Integer maxBlockMs;

        // ==================== 重试间隔配置 ====================
        /**
         * 【核心】重试间隔时间
         * 在重试发送失败消息之前等待的时间
         * 单位：毫秒，默认值：100
         */
        private Integer retryBackoffMs;

        /**
         * 【核心】重连间隔时间
         * 在尝试重新连接到给定主机之前等待的时间
         * 单位：毫秒，默认值：50
         */
        private Integer reconnectBackoffMs;

        /**
         * 【核心】最大重连间隔时间
         * 重新连接回退的最大时间
         * 单位：毫秒，默认值：1000 (1秒)
         */
        private Integer reconnectBackoffMaxMs;

        // ==================== 序列化配置 ====================
        /**
         * 【核心】键序列化器
         * 用于序列化消息键的类
         * 默认值：org.apache.kafka.common.serialization.StringSerializer
         */
        private String keySerializer;

        /**
         * 【核心】值序列化器
         * 用于序列化消息值的类
         * 默认值：org.apache.kafka.common.serialization.StringSerializer
         */
        private String valueSerializer;

        // ==================== 压缩配置 ====================
        /**
         * 【核心】压缩类型
         * 用于压缩数据的压缩编解码器
         * 可选值：none, gzip, snappy, lz4, zstd
         * 默认值：none
         */
        private String compressionType;

        // ==================== 元数据配置 ====================
        /**
         * 【核心】元数据最大年龄
         * 强制刷新元数据的时间间隔，即使没有看到任何分区领导层变化
         * 单位：毫秒，默认值：300000 (5分钟)
         */
        private Integer metadataMaxAgeMs;
    }

    /**
     * SASL认证配置
     */
    @Data
    public static class SaslConfig {
        /**
         * 【核心】SASL机制
         * 可选值：PLAIN, SCRAM-SHA-256, SCRAM-SHA-512
         * 默认值：PLAIN
         */
        private String mechanism;

        /**
         * JAAS配置
         */
        private JaasConfig jaas = new JaasConfig();
    }

    /**
     * JAAS配置
     */
    @Data
    public static class JaasConfig {
        /**
         * 【核心】JAAS配置字符串
         * 格式：org.apache.kafka.common.security.plain.PlainLoginModule required username="用户名" password="密码";
         * 注意：必须以分号结尾
         */
        private String config;
    }
}
