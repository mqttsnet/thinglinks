package com.mqttsnet.thinglinks.dto.bridge.protocol.clickhouse;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * ClickHouse 连接参数 DTO（列存 OLAP，写实时大宽表）。
 *
 * @author mqttsnet
 */
public class ClickHouseConnectionDto implements ProtocolConnectionDto {

    /**
     * JDBC URL（jdbc:clickhouse://host:8123/db）。必填
     */
    public String jdbcUrl;

    /**
     * 数据库名
     */
    public String database;

    /**
     * 目标表名。必填
     */
    public String table;

    /**
     * 字段映射 JSON：{"ts":"${timestamp}","device_id":"${deviceIdentification}"}。必填
     */
    public String columnMapping;

    /**
     * 是否使用批量插入（async insert）。默认 true
     */
    public Boolean useAsyncInsert;
}
