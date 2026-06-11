package com.mqttsnet.thinglinks.dto.bridge.protocol.kingbase;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * 人大金仓 KingBase 凭证 DTO。
 *
 * @author mqttsnet
 */
public class KingBaseCredentialDto implements ProtocolCredentialDto {
    /**
     * 数据库密码（落盘 AES 加密）
     */
    public String password;
}
