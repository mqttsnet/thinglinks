package com.mqttsnet.thinglinks.dto.bridge.protocol.dm;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

import java.util.Map;

/**
 * 达梦数据库 DM 连接参数 DTO（国产 OLTP，金融 / 政企首选）。
 *
 * @author mqttsnet
 */
public class DmConnectionDto implements ProtocolConnectionDto {

    /**
     * JDBC URL（jdbc:dm://host:5236/SCHEMA）。必填
     */
    public String jdbcUrl;

    /**
     * 数据库用户名（默认 SYSDBA）
     */
    public String username;

    /**
     * 模式名（schema），DM 用大写
     */
    public String schemaName;

    /**
     * 目标表名。必填
     */
    public String table;

    /**
     * 字段映射 JSON
     */
    public String columnMapping;

    /**
     * 冲突策略：INSERT / UPDATE / IGNORE。默认 INSERT
     */
    public String onDuplicate;
}
