package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * 厂商协议适配器工厂。
 *
 * <p>根据设备的 manufacturer（厂商）和 protocolVersion（版本）匹配最佳适配器，
 * 实现同一协议下不同厂商/版本的隔离与兼容。</p>
 *
 * <p>匹配策略（按优先级从高到低）：</p>
 * <ol>
 *     <li>精确匹配：manufacturer + version 完全匹配</li>
 *     <li>厂商匹配：manufacturer 匹配 + version 为通配 BizConstant.ALL</li>
 *     <li>版本匹配：manufacturer 为通配 BizConstant.ALL + version 匹配</li>
 *     <li>默认回退：manufacturer 和 version 均为 BizConstant.ALL 的通配适配器</li>
 * </ol>
 *
 * <p>同一匹配级别中，优先级值（{@link VendorProtocolAdapter#getPriority()}）最小的优先。</p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 * @see VendorProtocolAdapter
 */
@Slf4j
@Component
public class VendorProtocolAdapterFactory {

    private static final String WILDCARD = BizConstant.ALL;

    private final List<VendorProtocolAdapter> adapters;
    private final Map<String, VendorProtocolAdapter> cache = new ConcurrentHashMap<>();

    public VendorProtocolAdapterFactory(List<VendorProtocolAdapter> adapters) {
        this.adapters = adapters;
        log.info("注册厂商协议适配器 {} 个: {}", adapters.size(),
                adapters.stream()
                        .map(a -> a.getManufacturer() + ":" + a.getProtocolVersion())
                        .toList());
    }

    /**
     * 根据厂商和版本获取最佳匹配的适配器。
     *
     * @param manufacturer    厂商标识（可为 null，视为通配）
     * @param protocolVersion 协议版本（可为 null，视为通配）
     * @return 匹配的适配器，若无任何注册则返回 null
     */
    public VendorProtocolAdapter getAdapter(String manufacturer, String protocolVersion) {
        String key = normalizeKey(manufacturer, protocolVersion);
        return cache.computeIfAbsent(key, k -> findBestMatch(manufacturer, protocolVersion));
    }

    /**
     * 根据厂商和版本获取适配器，未匹配时返回默认适配器。
     *
     * @param manufacturer    厂商标识
     * @param protocolVersion 协议版本
     * @return 匹配的适配器或默认适配器
     */
    public VendorProtocolAdapter getAdapterOrDefault(String manufacturer, String protocolVersion) {
        var adapter = getAdapter(manufacturer, protocolVersion);
        if (adapter != null) {
            return adapter;
        }
        // 尝试获取全通配适配器
        return getAdapter(WILDCARD, WILDCARD);
    }

    /**
     * 查找最佳匹配的适配器
     */
    private VendorProtocolAdapter findBestMatch(String manufacturer, String protocolVersion) {
        String mfr = normalize(manufacturer);
        String ver = normalize(protocolVersion);

        return adapters.stream()
                .filter(a -> matches(a, mfr, ver))
                .min(Comparator.comparingInt(a -> matchScore(a, mfr, ver) * 1000 + a.getPriority()))
                .orElse(null);
    }

    /**
     * 判断适配器是否匹配
     */
    private boolean matches(VendorProtocolAdapter adapter, String manufacturer, String version) {
        String adapterMfr = normalize(adapter.getManufacturer());
        String adapterVer = normalize(adapter.getProtocolVersion());

        boolean mfrMatch = WILDCARD.equals(adapterMfr) || adapterMfr.equals(manufacturer) || WILDCARD.equals(manufacturer);
        boolean verMatch = WILDCARD.equals(adapterVer) || adapterVer.equals(version) || WILDCARD.equals(version);

        return mfrMatch && verMatch;
    }

    /**
     * 计算匹配精度得分（越低越精确）
     */
    private int matchScore(VendorProtocolAdapter adapter, String manufacturer, String version) {
        String adapterMfr = normalize(adapter.getManufacturer());
        String adapterVer = normalize(adapter.getProtocolVersion());

        int score = 0;
        // 厂商通配惩罚更重（厂商匹配优先级高于版本匹配）
        if (WILDCARD.equals(adapterMfr)) score += 20;
        if (WILDCARD.equals(adapterVer)) score += 10;
        return score;
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? WILDCARD : value.toLowerCase().trim();
    }

    private String normalizeKey(String manufacturer, String protocolVersion) {
        return normalize(manufacturer) + ":" + normalize(protocolVersion);
    }
}
