package com.mqttsnet.thinglinks.constants.bus;

/**
 * 协议总线常量,集中管理日志标签 / 指标名 / 线程池名 / Context key。
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
public final class BusConstants {

    private BusConstants() {
    }

    /**
     * 关键日志统一前缀,grep 友好。
     */
    public static final class Log {
        public static final String KAFKA_RECEIVE = "[bus.kafka]";
        public static final String DISPATCH = "[bus.dispatch]";
        public static final String NO_ROUTE = "[bus.route.miss]";
        public static final String STAGE_FAIL = "[bus.stage.fail]";
        public static final String STAGE_SKIP = "[bus.stage.skip]";
        public static final String POST_SUBMIT = "[bus.post.submit]";
        public static final String DROP = "[bus.drop]";
        public static final String TRACE = "[bus.trace]";
        private Log() {
        }
    }

    /**
     * Micrometer 指标 key,字典化方便 Grafana。
     */
    public static final class Metric {
        public static final String KAFKA_CONSUME = "bus.kafka.consume";
        public static final String DISPATCH_TOTAL = "bus.dispatch.total";
        public static final String DISPATCH_LATENCY = "bus.dispatch.latency";
        public static final String STAGE_EXECUTIONS = "bus.stage.executions";
        public static final String STAGE_LATENCY = "bus.stage.latency";
        public static final String NO_ROUTE = "bus.route.miss";
        public static final String CANONICALIZE_FAIL = "bus.adapter.fail";
        public static final String RELAY_SEND = "bus.relay.send";
        private Metric() {
        }
    }

    /**
     * dynamictp.yml 配置 + 业务 @Qualifier 引用统一用这些常量。
     */
    public static final class Pool {
        /**
         * 默认 POST 池(无专属命名时兜底)。
         */
        public static final String DEFAULT_POST = "mqsBusDefaultExecutor";
        public static final String BRIDGE_RELAY = "mqsBusBridgeRelayExecutor";
        public static final String ALARM_RELAY = "mqsBusAlarmRelayExecutor";
        public static final String ANALYTICS_RELAY = "mqsBusAnalyticsRelayExecutor";
        public static final String AUDIT = "mqsBusAuditExecutor";
        public static final String METRIC = "mqsBusMetricExecutor";
        public static final String TRACE = "mqsBusTraceExecutor";
        private Pool() {
        }
    }

    /**
     * StageContext 共享 key,集中定义防字面量散落。
     */
    public static final class Ctx {
        public static final String DEVICE_CACHE = "deviceCache";
        public static final String DISPATCH_GROUP = "dispatchGroup";
        public static final String SOURCE_TOPIC = "sourceTopic";
        /**
         * 上下文 LocalMap 快照,异步线程恢复用。
         */
        public static final String CONTEXT_SNAPSHOT = "contextSnapshot";
        private Ctx() {
        }
    }
}
