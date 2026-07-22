package com.mqttsnet.thinglinks.common.cache;

/**
 * 缓存模块
 *
 * @author mqttsnet
 * @date 2020/10/21
 */
public class CacheKeyModular {

    /**
     * 仅video服务使用的缓存
     */
    public static final String VIDEO = "video";

    /**
     * 仅mqs服务使用的缓存
     */
    public static final String MQS = "mqs";
    /**
     * 仅 broker 服务使用的缓存(协议接入层 ── ws / mqtt / coap / lwm2m / tcp 等)。
     * 跨节点协调状态走此命名空间。
     */
    public static final String BROKER = "broker";
    /**
     * 仅card服务使用的缓存
     */
    public static final String CARD = "card";

    /**
     * 仅link服务使用的缓存
     */
    public static final String LINK = "link";

    /**
     * 仅rule服务使用的缓存
     */
    public static final String RULE = "rule";

    /**
     * 多个服务都会使用的缓存
     */
    public static final String COMMON = "common";
    /**
     * 仅基础服务base使用的缓存
     */
    public static final String BASE = "base";
    /**
     * 仅消息服务msg使用的缓存
     */
    public static final String MSG = "msg";
    /**
     * 仅认证服务oauth使用的缓存
     */
    public static final String OAUTH = "oauth";
    /**
     * 仅文件服务file使用的缓存
     */
    public static final String FILE = "file";
    /**
     * 仅租户服务tenant使用的缓存
     */
    public static final String SYSTEM = "system";
    /**
     * 仅网关服务gateway使用的缓存
     */
    public static final String GATEWAY = "gateway";

    /** 缓存key前缀， 可以在启动时覆盖该参数。
     * 系统启动时，注入。
     */
    public static String PREFIX;
}
