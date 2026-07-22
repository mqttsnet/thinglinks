package com.mqttsnet.thinglinks.video.gb28181.device;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 大华设备适配器。
 * 处理大华设备的非标行为：
 * - 目录查询分包方式存在差异
 * - PTZ 速度范围与标准不同（上限 127）
 * - 部分型号心跳间隔较短
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@Order(2)
public class DahuaDeviceAdapter implements DeviceAdapter {

    private static final String MANUFACTURER_KEY = "dahua";
    private static final String UA_KEYWORD = "Dahua";
    private static final String UA_KEYWORD_UPPER = "DAHUA";

    @Override
    public String getManufacturer() {
        return MANUFACTURER_KEY;
    }

    @Override
    public boolean matches(String manufacturer, String userAgent) {
        if (StrUtil.isNotBlank(manufacturer)) {
            var lower = manufacturer.toLowerCase();
            if (lower.contains(MANUFACTURER_KEY) || lower.contains("大华")) {
                return true;
            }
        }
        if (StrUtil.isNotBlank(userAgent)) {
            return userAgent.contains(UA_KEYWORD) || userAgent.contains(UA_KEYWORD_UPPER);
        }
        return false;
    }

    @Override
    public int getPtzSpeedMax() {
        return 127;
    }

    @Override
    public int getDefaultHeartbeatInterval() {
        return 30;
    }

    @Override
    public int getCatalogQueryTimeout() {
        return 40;
    }
}
