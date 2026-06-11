package com.mqttsnet.thinglinks.video.protocol;

import com.mqttsnet.thinglinks.common.constant.BizConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Description:
 * VendorProtocolAdapterFactory 厂商协议适配器工厂单元测试。
 * 覆盖 4 级匹配优先级、缓存、通配回退等核心场景。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@DisplayName("VendorProtocolAdapterFactory 适配器工厂测试")
class VendorProtocolAdapterFactoryTest {

    @Test
    @DisplayName("getAdapter_精确匹配_返回精确适配器")
    void getAdapter_exactMatch_returnsExactAdapter() {
        var hikvision2016 = createAdapter("hikvision", "gb2016", 100);
        var wildcardAdapter = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(hikvision2016, wildcardAdapter));

        var result = factory.getAdapter("hikvision", "gb2016");

        assertNotNull(result);
        assertEquals("hikvision", result.getManufacturer());
        assertEquals("gb2016", result.getProtocolVersion());
    }

    @Test
    @DisplayName("getAdapter_厂商通配版本精确_返回版本匹配适配器")
    void getAdapter_vendorWildcardVersionExact_returnsVersionMatch() {
        var wildcardVendor2016 = createAdapter(BizConstant.ALL, "gb2016", 100);
        var wildcardAll = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(wildcardVendor2016, wildcardAll));

        var result = factory.getAdapter("dahua", "gb2016");

        assertNotNull(result);
        assertEquals(BizConstant.ALL, result.getManufacturer());
        assertEquals("gb2016", result.getProtocolVersion());
    }

    @Test
    @DisplayName("getAdapter_厂商精确版本通配_返回厂商匹配适配器")
    void getAdapter_vendorExactVersionWildcard_returnsVendorMatch() {
        var hikvisionWildcard = createAdapter("hikvision", BizConstant.ALL, 100);
        var wildcardAll = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(hikvisionWildcard, wildcardAll));

        var result = factory.getAdapter("hikvision", "gb2022");

        assertNotNull(result);
        assertEquals("hikvision", result.getManufacturer());
        assertEquals(BizConstant.ALL, result.getProtocolVersion());
    }

    @Test
    @DisplayName("getAdapter_全通配回退_返回通配适配器")
    void getAdapter_wildcardFallback_returnsWildcardAdapter() {
        var wildcardAll = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(wildcardAll));

        var result = factory.getAdapter("unknownVendor", "unknownVersion");

        assertNotNull(result);
        assertEquals(BizConstant.ALL, result.getManufacturer());
        assertEquals(BizConstant.ALL, result.getProtocolVersion());
    }

    @Test
    @DisplayName("getAdapter_优先级精确高于厂商匹配_选精确")
    void getAdapter_exactHigherPriorityThanVendorMatch() {
        // 精确匹配 matchScore=0, 厂商匹配 matchScore=10
        var exact = createAdapter("hikvision", "gb2016", 100);
        var vendorOnly = createAdapter("hikvision", BizConstant.ALL, 50); // 更低 priority 值
        var factory = new VendorProtocolAdapterFactory(List.of(exact, vendorOnly));

        var result = factory.getAdapter("hikvision", "gb2016");

        assertNotNull(result);
        // 精确匹配 score = 0*1000 + 100 = 100
        // 厂商匹配 score = 10*1000 + 50 = 10050
        // 精确匹配胜出
        assertEquals("gb2016", result.getProtocolVersion());
    }

    @Test
    @DisplayName("getAdapter_厂商匹配优先于版本匹配_厂商通配惩罚更重")
    void getAdapter_vendorMatchPrioritizedOverVersionMatch() {
        // 厂商通配 matchScore=20, 版本通配 matchScore=10
        var vendorWildcard = createAdapter(BizConstant.ALL, "gb2016", 100); // score = 20*1000+100 = 20100
        var versionWildcard = createAdapter("hikvision", BizConstant.ALL, 100); // score = 10*1000+100 = 10100
        var factory = new VendorProtocolAdapterFactory(List.of(vendorWildcard, versionWildcard));

        var result = factory.getAdapter("hikvision", "gb2016");

        assertNotNull(result);
        // 版本通配（厂商精确）应该优先于厂商通配（版本精确）
        assertEquals("hikvision", result.getManufacturer());
        assertEquals(BizConstant.ALL, result.getProtocolVersion());
    }

    @Test
    @DisplayName("getAdapter_同级别优先级值小的优先")
    void getAdapter_sameLevelLowerPriorityValueWins() {
        var adapter1 = createAdapter(BizConstant.ALL, BizConstant.ALL, 200);
        var adapter2 = createAdapter(BizConstant.ALL, BizConstant.ALL, 50);
        var factory = new VendorProtocolAdapterFactory(List.of(adapter1, adapter2));

        var result = factory.getAdapter("any", "any");

        assertNotNull(result);
        assertEquals(50, result.getPriority());
    }

    @Test
    @DisplayName("getAdapter_无注册适配器_返回null")
    void getAdapter_noRegisteredAdapters_returnsNull() {
        var factory = new VendorProtocolAdapterFactory(List.of());

        var result = factory.getAdapter("hikvision", "gb2016");

        assertNull(result);
    }

    @Test
    @DisplayName("getAdapter_null参数视为通配_匹配全通配")
    void getAdapter_nullParams_treatedAsWildcard() {
        var wildcardAll = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(wildcardAll));

        var result = factory.getAdapter(null, null);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getAdapter_空字符串参数_视为通配")
    void getAdapter_emptyStringParams_treatedAsWildcard() {
        var wildcardAll = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(wildcardAll));

        var result = factory.getAdapter("", "  ");

        assertNotNull(result);
    }

    @Test
    @DisplayName("getAdapter_大小写不敏感_正确匹配")
    void getAdapter_caseInsensitive_matchesCorrectly() {
        var hikvision = createAdapter("Hikvision", "GB2016", 100);
        var factory = new VendorProtocolAdapterFactory(List.of(hikvision));

        var result = factory.getAdapter("hikvision", "gb2016");

        assertNotNull(result);
    }

    @Test
    @DisplayName("getAdapter_缓存命中_第二次直接返回")
    void getAdapter_cacheHit_secondCallReturnsCached() {
        var adapter = createAdapter("hikvision", "gb2016", 100);
        var factory = new VendorProtocolAdapterFactory(List.of(adapter));

        var result1 = factory.getAdapter("hikvision", "gb2016");
        var result2 = factory.getAdapter("hikvision", "gb2016");

        assertSame(result1, result2);
    }

    @Test
    @DisplayName("getAdapterOrDefault_匹配存在_返回匹配适配器")
    void getAdapterOrDefault_matchExists_returnsMatch() {
        var hikvision = createAdapter("hikvision", "gb2016", 100);
        var defaultAdapter = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(hikvision, defaultAdapter));

        var result = factory.getAdapterOrDefault("hikvision", "gb2016");

        assertNotNull(result);
        assertEquals("hikvision", result.getManufacturer());
    }

    @Test
    @DisplayName("getAdapterOrDefault_无匹配_返回通配默认适配器")
    void getAdapterOrDefault_noMatch_returnsWildcardDefault() {
        var defaultAdapter = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(defaultAdapter));

        var result = factory.getAdapterOrDefault("unknown", "unknown");

        assertNotNull(result);
        assertEquals(BizConstant.ALL, result.getManufacturer());
    }

    @Test
    @DisplayName("getAdapterOrDefault_无任何适配器_返回null")
    void getAdapterOrDefault_noAdaptersAtAll_returnsNull() {
        var factory = new VendorProtocolAdapterFactory(List.of());

        var result = factory.getAdapterOrDefault("any", "any");

        assertNull(result);
    }

    @Test
    @DisplayName("getAdapter_完整4级优先级验证_精确>厂商>版本>通配")
    void getAdapter_full4TierPriority_exactVendorVersionWildcard() {
        var exact = createAdapter("hikvision", "gb2016", 100);
        var vendorMatch = createAdapter("hikvision", BizConstant.ALL, 100);
        var versionMatch = createAdapter(BizConstant.ALL, "gb2016", 100);
        var wildcard = createAdapter(BizConstant.ALL, BizConstant.ALL, 100);
        var factory = new VendorProtocolAdapterFactory(List.of(wildcard, versionMatch, vendorMatch, exact));

        // 精确匹配
        var r1 = factory.getAdapter("hikvision", "gb2016");
        assertEquals("hikvision", r1.getManufacturer());
        assertEquals("gb2016", r1.getProtocolVersion());
    }

    /**
     * 创建测试用适配器实现
     */
    private VendorProtocolAdapter createAdapter(String manufacturer, String protocolVersion, int priority) {
        return new TestAdapter(manufacturer, protocolVersion, priority);
    }

    /**
     * 测试用适配器实现
     */
    private static class TestAdapter implements VendorProtocolAdapter {
        private final String manufacturer;
        private final String protocolVersion;
        private final int priority;

        TestAdapter(String manufacturer, String protocolVersion, int priority) {
            this.manufacturer = manufacturer;
            this.protocolVersion = protocolVersion;
            this.priority = priority;
        }

        @Override public String getManufacturer() { return manufacturer; }
        @Override public String getProtocolVersion() { return protocolVersion; }
        @Override public int getPriority() { return priority; }
    }
}
