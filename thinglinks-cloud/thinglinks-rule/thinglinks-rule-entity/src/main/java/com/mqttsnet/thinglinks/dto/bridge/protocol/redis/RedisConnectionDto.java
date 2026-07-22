package com.mqttsnet.thinglinks.dto.bridge.protocol.redis;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * Redis 连接参数 DTO。
 *
 * @author mqttsnet
 */
public class RedisConnectionDto implements ProtocolConnectionDto {

    /**
     * 部署模式：STANDALONE / SENTINEL / CLUSTER。必填
     */
    public String mode;

    /**
     * STANDALONE 模式 host
     */
    public String host;

    /**
     * STANDALONE 模式 port，默认 6379
     */
    public Integer port;

    /**
     * STANDALONE / SENTINEL 模式 db 索引，默认 0
     */
    public Integer db;

    /**
     * SENTINEL 模式 sentinels 地址列表，多个用英文逗号分隔（如 host1:26379,host2:26379）
     */
    public String sentinels;

    /**
     * SENTINEL 模式 master name
     */
    public String masterName;

    /**
     * CLUSTER 模式节点地址列表，多个用英文逗号分隔
     */
    public String clusterNodes;

    /**
     * 写入命令：LPUSH / RPUSH / XADD / PUBLISH / SET。默认 LPUSH
     */
    public String command;

    /**
     * Key 模板，支持 ${tenantId} ${productId} ${deviceId} 占位符
     */
    public String keyTemplate;
}
