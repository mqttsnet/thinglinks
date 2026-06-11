package com.mqttsnet.thinglinks.dto.bridge.protocol.mongodb;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * MongoDB 凭证 DTO（如果连接 URI 已包含 user:pass@... 则可留空）。
 *
 * @author mqttsnet
 */
public class MongoDbCredentialDto implements ProtocolCredentialDto {

    /**
     * 用户名
     */
    public String username;

    /**
     * 密码（落盘 AES 加密）
     */
    public String password;

    /**
     * 认证数据库（默认 admin）
     */
    public String authDatabase;
}
