package com.mqttsnet.thinglinks.dto.bridge.protocol.mongodb;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * MongoDB 连接参数 DTO（mongodb-driver-sync；存原始 JSON / 复杂 schema）。
 *
 * @author mqttsnet
 */
public class MongoDbConnectionDto implements ProtocolConnectionDto {

    /**
     * Connection URI（mongodb://host:27017 或 mongodb+srv://...）。必填
     */
    public String uri;

    /**
     * 数据库名。必填
     */
    public String database;

    /**
     * Collection 名，支持模板 ${productId}。必填
     */
    public String collection;

    /**
     * 写入策略：INSERT / UPSERT。默认 INSERT
     */
    public String writeMode;

    /**
     * UPSERT 时的过滤键（如 deviceIdentification）。writeMode=UPSERT 时必填
     */
    public String upsertKey;

    /**
     * Write concern：UNACKNOWLEDGED / ACKNOWLEDGED / MAJORITY。默认 ACKNOWLEDGED
     */
    public String writeConcern;
}
