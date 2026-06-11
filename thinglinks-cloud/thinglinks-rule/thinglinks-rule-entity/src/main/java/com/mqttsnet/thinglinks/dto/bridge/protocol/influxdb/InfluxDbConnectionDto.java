package com.mqttsnet.thinglinks.dto.bridge.protocol.influxdb;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * InfluxDB 连接参数 DTO（v2 API + Line Protocol）。
 *
 * @author mqttsnet
 */
public class InfluxDbConnectionDto implements ProtocolConnectionDto {

    /**
     * InfluxDB URL（http://host:8086）。必填
     */
    public String url;

    /**
     * 组织（org）。v2 必填
     */
    public String org;

    /**
     * Bucket 名（v2）/ Database 名（v1）。必填
     */
    public String bucket;

    /**
     * Measurement 名（写入的 measurement）。必填，支持模板 ${productId}
     */
    public String measurement;

    /**
     * Tags 字段映射（如 {"productId":"${productId}","deviceId":"${deviceIdentification}"}）。可选
     */
    public String tagsMapping;

    /**
     * Fields 字段映射（如 {"value":"${payload}"}）。必填
     */
    public String fieldsMapping;

    /**
     * 时间字段（默认从 envelope.ts 取）
     */
    public String timestampField;

    /**
     * API 版本：V1 / V2。默认 V2
     */
    public String apiVersion;
}
