package com.mqttsnet.thinglinks.video.gb28181.device;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Description:
 * 宇视设备适配器。
 * 处理宇视设备的非标行为：
 * - 注册时 User-Agent 格式不规范，需要宽松匹配
 * - 部分型号不支持目录分包
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
@Order(3)
public class UniviewDeviceAdapter implements DeviceAdapter {

    private static final String MANUFACTURER_KEY = "uniview";
    private static final String UA_KEYWORD = "Uniview";
    private static final String UA_KEYWORD_UPPER = "UNIVIEW";
    private static final String UA_KEYWORD_UNV = "UNV";

    @Override
    public String getManufacturer() {
        return MANUFACTURER_KEY;
    }

    @Override
    public boolean matches(String manufacturer, String userAgent) {
        if (StrUtil.isNotBlank(manufacturer)) {
            var lower = manufacturer.toLowerCase();
            if (lower.contains(MANUFACTURER_KEY) || lower.contains("宇视") || lower.contains("unv")) {
                return true;
            }
        }
        if (StrUtil.isNotBlank(userAgent)) {
            return userAgent.contains(UA_KEYWORD) || userAgent.contains(UA_KEYWORD_UPPER) || userAgent.contains(UA_KEYWORD_UNV);
        }
        return false;
    }

    @Override
    public boolean supportsCatalogPartition() {
        return false;
    }

    @Override
    public int getCatalogQueryTimeout() {
        return 60;
    }
}
