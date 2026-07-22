package com.mqttsnet.thinglinks.dto.bridge.protocol.tdengine;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * TDengine 连接参数 DTO。
 *
 * <p>支持两种连接模式：
 * <ul>
 *   <li>{@code RESTful}：HTTP API（默认 6041 端口），简单但性能略低</li>
 *   <li>{@code JDBC-RESTful} / {@code JDBC-Native}：通过 taos-jdbcdriver</li>
 * </ul>
 *
 * <p>本 sink 用 JDBC-RESTful（无需本地 taosc 库，跨平台部署友好）。
 *
 * @author mqttsnet
 */
public class TDengineConnectionDto implements ProtocolConnectionDto {

    /**
     * TDengine host。必填
     */
    public String host;

    /**
     * RESTful 端口（默认 6041）
     */
    public Integer port;

    /**
     * 数据库名（database）。必填
     */
    public String database;

    /**
     * 超表名（STable）。必填，桥接消息会 INSERT 到子表
     */
    public String superTable;

    /**
     * 子表命名模板（按设备分子表用），如 d_${deviceIdentification}。可选
     */
    public String childTableTemplate;

    /**
     * 标签字段映射 JSON（TAGS）：{"productId":"${productId}","deviceId":"${deviceIdentification}"}。可选
     */
    public String tagsMapping;

    /**
     * 字段映射 JSON：{"ts":"${timestamp}","value":"${payload}"}。必填
     */
    public String columnMapping;
}
