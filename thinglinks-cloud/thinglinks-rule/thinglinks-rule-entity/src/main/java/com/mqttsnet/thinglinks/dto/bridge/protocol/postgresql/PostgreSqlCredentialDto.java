package com.mqttsnet.thinglinks.dto.bridge.protocol.postgresql;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * PostgreSQL 凭证 DTO。
 *
 * @author mqttsnet
 */
public class PostgreSqlCredentialDto implements ProtocolCredentialDto {

    /**
     * 数据库密码（落盘 AES 加密）
     */
    public String password;
}
