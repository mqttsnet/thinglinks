package com.mqttsnet.thinglinks.dto.bridge.protocol.clickhouse;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * ClickHouse 凭证 DTO。
 *
 * @author mqttsnet
 */
public class ClickHouseCredentialDto implements ProtocolCredentialDto {

    /**
     * 用户名（默认 default）
     */
    public String username;

    /**
     * 密码（落盘 AES 加密）
     */
    public String password;
}
