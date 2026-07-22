package com.mqttsnet.thinglinks.dto.bridge.protocol.iotdb;

import com.mqttsnet.thinglinks.dto.bridge.protocol.ProtocolConnectionDto;

/**
 * Apache IoTDB 连接参数 DTO（jdbc / session SDK；Apache 顶级时序）。
 *
 * @author mqttsnet
 */
public class IoTDbConnectionDto implements ProtocolConnectionDto {

    /**
     * IoTDB host。必填
     */
    public String host;

    /**
     * IoTDB 端口（默认 6667）
     */
    public Integer port;

    /**
     * 存储组（storage group），如 root.iot。必填
     */
    public String storageGroup;

    /**
     * 时间序列模板（time series path），如 root.iot.${productId}.${deviceIdentification}.value
     */
    public String timeseriesTemplate;

    /**
     * 数据类型：INT32 / INT64 / FLOAT / DOUBLE / BOOLEAN / TEXT。默认 DOUBLE
     */
    public String dataType;

    /**
     * 编码方式：PLAIN / RLE / TS_2DIFF / GORILLA。默认 GORILLA（适合时序数据）
     */
    public String encoding;

    /**
     * 压缩方式：UNCOMPRESSED / SNAPPY / LZ4 / GZIP / ZSTD。默认 SNAPPY
     */
    public String compressor;
}
