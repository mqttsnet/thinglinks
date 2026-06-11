package com.mqttsnet.thinglinks.video.cache;

/**
 * Description:
 * 租户 SIP 配置 ThreadLocal 持有者。
 * <p>
 * SIP 请求处理线程内缓存租户配置，避免重复查 Redis。
 * handlerTenantId() 中 set，处理完成后 remove。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-10
 */
public final class TenantSipConfigHolder {

    private static final ThreadLocal<TenantSipConfig> HOLDER = new ThreadLocal<>();

    private TenantSipConfigHolder() {
    }

    public static void set(TenantSipConfig config) {
        HOLDER.set(config);
    }

    public static TenantSipConfig get() {
        return HOLDER.get();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
