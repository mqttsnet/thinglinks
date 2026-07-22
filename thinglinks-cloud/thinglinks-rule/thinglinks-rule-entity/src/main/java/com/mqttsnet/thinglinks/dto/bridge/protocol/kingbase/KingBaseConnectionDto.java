package com.mqttsnet.thinglinks.dto.bridge.protocol.kingbase;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * 人大金仓 KingBase 连接参数 DTO（PG 兼容生态；国产 OLTP）。
 *
 * @author mqttsnet
 */
public class KingBaseConnectionDto implements ProtocolConnectionDto {

    /**
     * JDBC URL（jdbc:kingbase8://host:54321/dbname）。必填
     */
    public String jdbcUrl;

    /**
     * 用户名（默认 system）
     */
    public String username;

    /**
     * Schema 名（默认 public）
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
     * 冲突策略：INSERT / UPSERT / IGNORE。默认 INSERT
     */
    public String onConflict;

    /**
     * UPSERT 冲突键
     */
    public String conflictKeys;
}
