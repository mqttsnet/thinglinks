package com.mqttsnet.thinglinks.dto.bridge.protocol.mysql;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * MySQL 凭证 DTO（仅密码；username 在 {@link MysqlConnectionDto}）。
 *
 * @author mqttsnet
 */
public class MysqlCredentialDto implements ProtocolCredentialDto {

    /**
     * 数据库密码（落盘 AES 加密）
     */
    public String password;
}
