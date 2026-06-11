package com.mqttsnet.thinglinks.dto.bridge.protocol.postgresql;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * PostgreSQL 连接参数 DTO（jdbc + HikariCP；通用 OLTP）。
 *
 * @author mqttsnet
 */
public class PostgreSqlConnectionDto implements ProtocolConnectionDto {

    /**
     * JDBC URL（jdbc:postgresql://host:5432/db）。必填
     */
    public String jdbcUrl;

    /**
     * 数据库用户名。必填
     */
    public String username;

    /**
     * 目标 schema（如 public）。默认 public
     */
    public String schemaName;

    /**
     * 目标表名，支持模板。必填
     */
    public String table;

    /**
     * 字段映射 JSON。必填
     */
    public String columnMapping;

    /**
     * 冲突策略：INSERT / UPSERT(ON CONFLICT) / IGNORE(ON CONFLICT DO NOTHING)。默认 INSERT
     */
    public String onConflict;

    /**
     * UPSERT 时的冲突键（逗号分隔列名）。onConflict=UPSERT 时必填
     */
    public String conflictKeys;
}
