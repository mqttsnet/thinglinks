package com.mqttsnet.thinglinks.mqs.bus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 协议总线模块装配入口 ── 扫 {@code com.mqttsnet.thinglinks.mqs.bus} 下所有 bean.
 *
 * @author mqttsnet
 * @since 2026-05-10
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.mqttsnet.thinglinks.mqs.bus")
@Slf4j
public class DeviceBusAutoConfiguration {

    public DeviceBusAutoConfiguration() {
        log.info("[bus] DeviceBusAutoConfiguration loaded.....");
    }
}
