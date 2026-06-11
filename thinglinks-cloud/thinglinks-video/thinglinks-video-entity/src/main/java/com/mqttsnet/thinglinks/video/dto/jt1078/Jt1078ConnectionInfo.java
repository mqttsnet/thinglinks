package com.mqttsnet.thinglinks.video.dto.jt1078;

import java.time.LocalDateTime;

/**
 * JT/T 1078 终端连接信息记录。记录已连接车载终端的基本信息，用于在线状态管理。
 *
 * @param simNumber    终端SIM卡号
 * @param plateNumber  车牌号
 * @param channelCount 通道数
 * @param ip           终端IP
 * @param port         终端端口
 * @param loginTime    登录时间
 * @author mqttsnet
 */
public record Jt1078ConnectionInfo(
        String simNumber,
        String plateNumber,
        Integer channelCount,
        String ip,
        Integer port,
        LocalDateTime loginTime
) {
}
