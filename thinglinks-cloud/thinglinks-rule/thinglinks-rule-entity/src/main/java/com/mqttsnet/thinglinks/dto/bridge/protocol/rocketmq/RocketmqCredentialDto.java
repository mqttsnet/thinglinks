package com.mqttsnet.thinglinks.dto.bridge.protocol.rocketmq;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * RocketMQ 凭证 DTO（阿里云 ACL 鉴权用；自建 Apache 一般空）。
 *
 * @author mqttsnet
 */
public class RocketmqCredentialDto implements ProtocolCredentialDto {

    /**
     * Access Key（阿里云 RocketMQ ACL；自建留空）
     */
    public String accessKey;

    /**
     * Secret Key（阿里云 RocketMQ ACL；自建留空）
     */
    public String secretKey;
}
