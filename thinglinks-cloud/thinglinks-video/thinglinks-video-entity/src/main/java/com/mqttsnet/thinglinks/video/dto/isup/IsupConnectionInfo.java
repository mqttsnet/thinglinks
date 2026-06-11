package com.mqttsnet.thinglinks.video.dto.isup;

import java.time.LocalDateTime;

/**
 * ISUP 设备连接信息记录。记录已登录设备的连接元数据，用于连接管理。
 *
 * @param deviceSerial 设备序列号
 * @param userId       登录句柄
 * @param channelCount 通道数
 * @param ip           设备 IP
 * @param port         设备端口
 * @param loginTime    登录时间
 * @author mqttsnet
 */
public record IsupConnectionInfo(
        String deviceSerial,
        Integer userId,
        Integer channelCount,
        String ip,
        Integer port,
        LocalDateTime loginTime
) {
}
