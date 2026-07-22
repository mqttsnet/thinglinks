package com.mqttsnet.thinglinks.video.gb28181.protocol;

import com.mqttsnet.thinglinks.video.enumeration.gb28181.GbProtocolVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Description:
 * GB/T 28181 协议版本适配器工厂。
 * 根据协议版本枚举自动选择对应的适配器实现，
 * 支持 2016 和 2022 两个版本，默认使用 2016 版本（向下兼容）。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Slf4j
@Component
public class GbProtocolAdapterFactory {

    private final Map<GbProtocolVersionEnum, GbProtocolAdapter> adapterMap;

    public GbProtocolAdapterFactory(List<GbProtocolAdapter> adapters) {
        this.adapterMap = new EnumMap<>(GbProtocolVersionEnum.class);
        for (GbProtocolAdapter adapter : adapters) {
            adapterMap.put(adapter.getVersion(), adapter);
            log.info("注册 GB28181 协议适配器: version={}, class={}", adapter.getVersion().getDesc(), adapter.getClass().getSimpleName());
        }
    }

    /**
     * 根据协议版本获取适配器
     *
     * @param version 协议版本枚举
     * @return 对应的适配器实例
     */
    public Optional<GbProtocolAdapter> getAdapter(GbProtocolVersionEnum version) {
        return Optional.ofNullable(adapterMap.get(version));
    }

    /**
     * 根据协议版本获取适配器，不存在时返回默认（2016）适配器
     *
     * @param version 协议版本枚举（可为 null）
     * @return 适配器实例，优先返回指定版本，否则返回默认版本
     */
    public GbProtocolAdapter getAdapterOrDefault(GbProtocolVersionEnum version) {
        if (version != null) {
            var adapter = adapterMap.get(version);
            if (adapter != null) {
                return adapter;
            }
            log.warn("未找到 GB28181 协议版本 {} 的适配器，回退到默认版本 {}", version.getDesc(), GbProtocolVersionEnum.DEFAULT.getDesc());
        }
        return adapterMap.get(GbProtocolVersionEnum.DEFAULT);
    }

    /**
     * 根据版本值字符串获取适配器
     *
     * @param versionValue 版本值（如 "2016"、"2022"）
     * @return 适配器实例，版本无效时返回默认适配器
     */
    public GbProtocolAdapter getAdapterByValue(String versionValue) {
        var version = GbProtocolVersionEnum.fromValueOrDefault(versionValue);
        return getAdapterOrDefault(version);
    }
}
