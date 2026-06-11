package com.mqttsnet.thinglinks.video.gb28181.device;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 海康威视设备适配器。
 * 处理海康威视设备的非标行为：
 * - INVITE 响应携带非标 SDP 字段
 * - 心跳间隔可能不稳定
 * - 目录查询超时需要适当延长
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@Order(1)
public class HikvisionDeviceAdapter implements DeviceAdapter {

    private static final String MANUFACTURER_KEY = "hikvision";
    private static final String UA_KEYWORD = "Hikvision";
    private static final String UA_KEYWORD_ALT = "HIKVISION";
    private static final String UA_KEYWORD_IP_CAMERA = "IP Camera";

    @Override
    public String getManufacturer() {
        return MANUFACTURER_KEY;
    }

    @Override
    public boolean matches(String manufacturer, String userAgent) {
        if (StrUtil.isNotBlank(manufacturer)) {
            var lower = manufacturer.toLowerCase();
            if (lower.contains(MANUFACTURER_KEY) || lower.contains("海康")) {
                return true;
            }
        }
        if (StrUtil.isNotBlank(userAgent)) {
            return userAgent.contains(UA_KEYWORD) || userAgent.contains(UA_KEYWORD_ALT) || userAgent.contains(UA_KEYWORD_IP_CAMERA);
        }
        return false;
    }

    @Override
    public boolean requiresCustomSdpParsing() {
        return true;
    }

    @Override
    public int getCatalogQueryTimeout() {
        return 45;
    }

    @Override
    public int getDefaultHeartbeatInterval() {
        return 60;
    }
}
