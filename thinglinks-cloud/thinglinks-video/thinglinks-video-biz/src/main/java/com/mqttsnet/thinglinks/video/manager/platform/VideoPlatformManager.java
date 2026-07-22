package com.mqttsnet.thinglinks.video.manager.platform;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.PlatformRegisterCache;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;

import java.util.List;
import java.util.Optional;

/**
 * 级联平台 Manager 接口（biz 数据持久化层 — DB + 注册缓存原始操作）。
 * <p>同时承担：
 * <ul>
 *   <li>DB CRUD（{@link SuperManager}）</li>
 *   <li>级联注册全局 Hash 缓存（注册状态 / Keepalive 续期定时任务遍历用）</li>
 * </ul>
 *
 * @author mqttsnet
 */
public interface VideoPlatformManager extends SuperManager<VideoPlatform> {

    VideoPlatform getByServerGbId(String serverGbId);

    /**
     * 写入或更新指定 platform 的注册缓存（全局 Hash，field=platformId）。
     */
    void putRegisterCache(Long platformId, PlatformRegisterCache cache);

    /**
     * 按 platformId 读取注册缓存。
     *
     * @param platformId 平台 ID
     * @return 命中返 {@code Optional}; 缓存中无返 {@link Optional#empty()}
     */
    Optional<PlatformRegisterCache> getRegisterCache(Long platformId);

    /**
     * 按 platformId 删除注册缓存。
     */
    void removeRegisterCache(Long platformId);

    /**
     * 列举全部已注册平台缓存（@Scheduled 续期 / Keepalive 遍历用）。
     */
    List<PlatformRegisterCache> listAllRegisterCaches();
}
