package com.mqttsnet.thinglinks.video.manager.platform.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.platform.PlatformRegisterCacheKeyBuilder;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.PlatformRegisterCache;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;
import com.mqttsnet.thinglinks.video.manager.platform.VideoPlatformManager;
import com.mqttsnet.thinglinks.video.mapper.platform.VideoPlatformMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 级联平台 Manager 实现。
 * <p>同时承担 DB 与级联注册全局 Hash 缓存的原始操作。
 *
 * @author mqttsnet
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class VideoPlatformManagerImpl extends SuperManagerImpl<VideoPlatformMapper, VideoPlatform> implements VideoPlatformManager {

    private final CachePlusOps cachePlusOps;

    @Override
    public VideoPlatform getByServerGbId(String serverGbId) {
        QueryWrap<VideoPlatform> queryWrap = new QueryWrap<>();
        queryWrap.lambda().eq(VideoPlatform::getServerGbId, serverGbId);
        return getOne(queryWrap);
    }

    @Override
    public void putRegisterCache(Long platformId, PlatformRegisterCache cache) {
        if (platformId == null || cache == null) {
            return;
        }
        try {
            CacheHashKey fieldKey = PlatformRegisterCacheKeyBuilder.buildHash(platformId);
            cachePlusOps.hSet(fieldKey, cache);
        } catch (Exception e) {
            log.warn("[平台级联] Redis 写入失败: platformId={}", platformId, e);
        }
    }

    @Override
    public Optional<PlatformRegisterCache> getRegisterCache(Long platformId) {
        try {
            CacheHashKey fieldKey = PlatformRegisterCacheKeyBuilder.buildHash(platformId);
            CacheResult<Object> result = cachePlusOps.hGet(fieldKey);
            if (result != null && result.getRawValue() != null) {
                return Optional.ofNullable(BeanPlusUtil.toBeanIgnoreError(result.getRawValue(), PlatformRegisterCache.class));
            }
        } catch (Exception e) {
            log.warn("[平台级联] Redis 读取失败: platformId={}", platformId, e);
        }
        return Optional.empty();
    }

    @Override
    public void removeRegisterCache(Long platformId) {
        try {
            CacheHashKey fieldKey = PlatformRegisterCacheKeyBuilder.buildHash(platformId);
            cachePlusOps.hDel(fieldKey);
        } catch (Exception e) {
            log.warn("[平台级联] Redis 删除失败: platformId={}", platformId, e);
        }
    }

    @Override
    public List<PlatformRegisterCache> listAllRegisterCaches() {
        try {
            CacheKey hashKey = PlatformRegisterCacheKeyBuilder.buildKey();
            Map<Object, CacheResult<Object>> entries = cachePlusOps.hGetAll(hashKey);
            if (entries == null || entries.isEmpty()) {
                return List.of();
            }
            List<PlatformRegisterCache> list = new ArrayList<>(entries.size());
            for (CacheResult<Object> cacheResult : entries.values()) {
                if (cacheResult != null && cacheResult.getRawValue() != null) {
                    PlatformRegisterCache cache = BeanPlusUtil.toBeanIgnoreError(cacheResult.getRawValue(), PlatformRegisterCache.class);
                    if (cache != null) {
                        list.add(cache);
                    }
                }
            }
            return list;
        } catch (Exception e) {
            log.warn("[平台级联] Redis 查询全部失败", e);
            return List.of();
        }
    }
}
