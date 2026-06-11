package com.mqttsnet.thinglinks.video.gb28181.device;

/**
 * Description:
 * GB28181 设备厂商适配器接口。
 * 处理不同厂商设备（海康、大华、宇视等）的非标行为，
 * 业务层通过此接口统一操作，无需关心厂商差异。
 * <p>
 * 适配器通过设备注册时的 manufacturer 字段或 User-Agent 自动选择，
 * 对业务代码完全透明。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
public interface DeviceAdapter {

    /**
     * 获取适配器对应的厂商标识
     *
     * @return 厂商标识（如 "hikvision"、"dahua"、"uniview"、"default"）
     */
    String getManufacturer();

    /**
     * 判断是否匹配该厂商。
     * 通过 manufacturer 字段或 User-Agent 字符串进行匹配。
     *
     * @param manufacturer 设备上报的厂商名称
     * @param userAgent    SIP 注册时的 User-Agent 头
     * @return 是否匹配
     */
    boolean matches(String manufacturer, String userAgent);

    /**
     * 获取该厂商设备的默认心跳间隔（秒）。
     * 部分厂商心跳间隔不稳定，需要按厂商调整超时阈值。
     *
     * @return 默认心跳间隔（秒）
     */
    default int getDefaultHeartbeatInterval() {
        return 60;
    }

    /**
     * 获取该厂商设备的心跳超时次数。
     * 超过此次数未收到心跳则判定设备离线。
     *
     * @return 超时次数
     */
    default int getHeartbeatTimeoutCount() {
        return 3;
    }

    /**
     * 是否需要对 SDP 响应做特殊解析。
     * 部分厂商设备在 INVITE 响应中携带非标 SDP 字段。
     *
     * @return 是否需要特殊 SDP 解析
     */
    default boolean requiresCustomSdpParsing() {
        return false;
    }

    /**
     * 获取 PTZ 速度范围上限。
     * 不同厂商 PTZ 速度范围不同。
     *
     * @return PTZ 速度上限值（标准为 255）
     */
    default int getPtzSpeedMax() {
        return 255;
    }

    /**
     * 是否支持目录查询分包。
     * 部分厂商目录查询分包方式存在差异。
     *
     * @return 是否支持分包
     */
    default boolean supportsCatalogPartition() {
        return true;
    }

    /**
     * 获取目录查询超时时间（秒）。
     * 通道数量多的设备需要更长的等待时间。
     *
     * @return 超时时间（秒）
     */
    default int getCatalogQueryTimeout() {
        return 30;
    }
}
