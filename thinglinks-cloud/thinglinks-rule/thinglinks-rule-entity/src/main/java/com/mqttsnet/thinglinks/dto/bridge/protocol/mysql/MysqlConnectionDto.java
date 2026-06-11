package com.mqttsnet.thinglinks.dto.bridge.protocol.mysql;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * MySQL 连接参数 DTO。
 *
 * <p>⚠ {@code username} 落在 connection 段（不是 credential），与 util-pro
 * {@code MysqlSink.MysqlConnConfig} 对齐。前端 MysqlProtocol 模块的
 * connectionFields() 也把 username 放 connection 段。
 *
 * @author mqttsnet
 */
public class MysqlConnectionDto implements ProtocolConnectionDto {

    /**
     * JDBC URL（jdbc:mysql://host:3306/db?useSSL=false&...）。必填
     */
    public String jdbcUrl;

    /**
     * 数据库用户名。必填
     */
    public String username;

    /**
     * 目标表名，支持模板 iot_${productId}。必填
     */
    public String table;

    /**
     * 冲突策略：INSERT / UPDATE / IGNORE。默认 INSERT
     */
    public String onDuplicate;

    /**
     * 字段映射 JSON 字符串（如 {"device_id":"${deviceIdentification}",...}）。可选
     */
    public String columnMapping;
}
