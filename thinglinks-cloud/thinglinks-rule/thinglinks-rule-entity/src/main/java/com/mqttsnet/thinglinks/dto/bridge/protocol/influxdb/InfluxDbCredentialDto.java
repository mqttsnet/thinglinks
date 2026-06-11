package com.mqttsnet.thinglinks.dto.bridge.protocol.influxdb;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolCredentialDto;

/**
 * InfluxDB 凭证 DTO（v2 API token / v1 username+password）。
 *
 * @author mqttsnet
 */
public class InfluxDbCredentialDto implements ProtocolCredentialDto {

    /**
     * v2 API token（落盘 AES 加密；优先级高于 v1 用户名密码）
     */
    public String token;

    /**
     * v1 用户名（apiVersion=V1 时填）
     */
    public String username;

    /**
     * v1 密码（落盘 AES 加密）
     */
    public String password;
}
