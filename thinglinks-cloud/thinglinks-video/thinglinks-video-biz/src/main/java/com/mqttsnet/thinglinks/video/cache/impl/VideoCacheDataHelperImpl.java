package com.mqttsnet.thinglinks.video.cache.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.cache.utils.CachePlusUtil;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.thinglinks.common.cache.video.device.ChannelInfoCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.video.device.DeviceInfoCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.video.sip.SipTenantConfigCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.video.cache.TenantSipConfigCacheVO;
import com.mqttsnet.thinglinks.video.cache.VideoCacheDataHelper;
import com.mqttsnet.thinglinks.video.cache.VideoChannelCacheVO;
import com.mqttsnet.thinglinks.video.cache.VideoDeviceCacheVO;
import com.mqttsnet.thinglinks.video.cache.device.VideoChannelCacheService;
import com.mqttsnet.thinglinks.video.cache.device.VideoDeviceCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Video 模块缓存聚合 / read-through 统一出口(对齐 LinkCacheDataHelper)。
 *
 * <p><b>调用层级</b>:Helper → 各域 CacheService → 域内 Service / QueryService → Manager。
 * 凡走缓存的查询都从本类入口;纯 DB 直查走 Service。</p>
 *
 * @author mqttsnet
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class VideoCacheDataHelperImpl implements VideoCacheDataHelper {

    private final CachePlusOps cachePlusOps;

    /**
     * read-through 统一入口 ── {@link CachePlusUtil#getOrLoad}.
     */
    private final CachePlusUtil cachePlusOpsUtil;

    /**
     * 设备缓存 DB 回源依赖 ── read-through fallback 时调
     * {@link VideoDeviceCacheService#loadDeviceFromDb(String)}.
     */
    private final VideoDeviceCacheService videoDeviceCacheService;

    /**
     * 通道缓存 DB 回源依赖 ── read-through fallback 时调
     * {@link VideoChannelCacheService#loadChannelFromDb(String)}.
     */
    private final VideoChannelCacheService videoChannelCacheService;

    /**
     * cache miss 时溯源 DB：SIP 配置查询
     */

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void setDeviceInfo(VideoDeviceCacheVO deviceCacheVO) {
        CacheKey cacheKey = DeviceInfoCacheKeyBuilder.build(deviceCacheVO.getDeviceIdentification());
        cachePlusOps.del(cacheKey);
        cachePlusOps.set(cacheKey, deviceCacheVO);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public VideoDeviceCacheVO getDeviceInfo(String deviceIdentification) {
        CacheKey cacheKey = DeviceInfoCacheKeyBuilder.build(deviceIdentification);
        // read-through:cache miss 时通过 CacheService 回源 DB + 自动 set,避免"缓存丢了就当设备不存在"业务异常.
        // cacheNullValues=false:DB 也没有时不缓存 null,下次还是会再溯源.
        return cachePlusOpsUtil.getOrLoad(
            cacheKey,
            (k) -> videoDeviceCacheService.loadDeviceFromDb(deviceIdentification),
            VideoDeviceCacheVO.class,
            false).orElse(null);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void removeDeviceInfo(String deviceIdentification) {
        CacheKey cacheKey = DeviceInfoCacheKeyBuilder.build(deviceIdentification);
        cachePlusOps.del(cacheKey);
        log.info("删除设备缓存: deviceIdentification={}", deviceIdentification);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void setChannelInfo(VideoChannelCacheVO channelCacheVO) {
        CacheKey cacheKey = ChannelInfoCacheKeyBuilder.build(channelCacheVO.getChannelIdentification());
        cachePlusOps.del(cacheKey);
        cachePlusOps.set(cacheKey, channelCacheVO);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public VideoChannelCacheVO getChannelInfo(String channelIdentification) {
        CacheKey cacheKey = ChannelInfoCacheKeyBuilder.build(channelIdentification);
        // read-through:cache miss 时通过 CacheService 回源 DB 拉通道数据并自动写回缓存
        return cachePlusOpsUtil.getOrLoad(
            cacheKey,
            (k) -> videoChannelCacheService.loadChannelFromDb(channelIdentification),
            VideoChannelCacheVO.class,
            false).orElse(null);
    }

    @DS(DsConstant.BASE_TENANT)
    @Override
    public void removeChannelInfo(String channelIdentification) {
        CacheKey cacheKey = ChannelInfoCacheKeyBuilder.build(channelIdentification);
        cachePlusOps.del(cacheKey);
        log.info("删除通道缓存: channelIdentification={}", channelIdentification);
    }

    @Override
    public Optional<TenantSipConfigCacheVO> getSipConfigBySipId(String sipId) {
        if (StrUtil.isBlank(sipId)) {
            return Optional.empty();
        }
        // 这是"sipId → tenant"的反查路径，**故意没有 @DS / 不做 DB 兜底**：
        //   - 入站 SIP（MESSAGE/REGISTER）线程在 setTenantId 之前调用本方法（参见
        //     SIPRequestProcessorParent.handlerTenantId 的 265/270/275 三步路由），
        //     tenantId 必然为 null，DB 回源会被 dynamic-datasource 兜底到 defaults 库，
        //     `video_sip_config` 是租户表，必然 SQLSyntaxError。
        //   - 全局 Hash 是 sipId→tenant 映射的唯一可信源，由 `refreshTenantCache(tenantId)`
        //     在有租户上下文的任务里写入；cache miss 由上层 fuzzy match 兜底，不该在这里穿透 DB。
        CacheResult<Object> result = cachePlusOps.hGet(SipTenantConfigCacheKeyBuilder.buildHash(sipId));
        if (result == null || result.getRawValue() == null) {
            return Optional.empty();
        }
        Object raw = result.getRawValue();
        if (raw instanceof TenantSipConfigCacheVO vo) {
            return Optional.of(vo);
        }
        return Optional.ofNullable(BeanPlusUtil.toBeanIgnoreError(raw, TenantSipConfigCacheVO.class));
    }

    @Override
    public Optional<TenantSipConfigCacheVO> resolveSipConfigByFuzzyMatch(String requestSipId, String deviceIdentification) {
        // 全部 SIP 配置走全局 Hash —— 这里无需 @DS，hGetAll 不查 DB
        CacheKey hashKey = SipTenantConfigCacheKeyBuilder.buildKey();
        Map<Object, CacheResult<Object>> allEntries = cachePlusOps.hGetAll(hashKey);
        if (allEntries == null || allEntries.isEmpty()) {
            return Optional.empty();
        }

        // 策略 1：SIP 域匹配（设备编号前 10 位 = sipDomain）
        if (StrUtil.isNotBlank(deviceIdentification) && deviceIdentification.length() >= 10) {
            String deviceDomain = deviceIdentification.substring(0, 10);
            for (CacheResult<Object> entry : allEntries.values()) {
                if (entry == null || entry.getRawValue() == null) {
                    continue;
                }
                TenantSipConfigCacheVO vo = BeanPlusUtil.toBeanIgnoreError(entry.getRawValue(), TenantSipConfigCacheVO.class);
                if (vo != null && deviceDomain.equals(vo.getSipDomain())) {
                    log.info("[租户路由-域匹配] requestSipId={}, 设备={}, 匹配到域={}, 租户={}",
                            requestSipId, deviceIdentification, vo.getSipDomain(), vo.getTenantId());
                    return Optional.of(vo);
                }
            }
        }

        // 策略 2：全局只有一个配置，直接命中
        if (allEntries.size() == 1) {
            CacheResult<Object> single = allEntries.values().iterator().next();
            if (single != null && single.getRawValue() != null) {
                TenantSipConfigCacheVO vo = BeanPlusUtil.toBeanIgnoreError(single.getRawValue(), TenantSipConfigCacheVO.class);
                if (vo != null) {
                    log.info("[租户路由-单配置] requestSipId={}, 设备={}, 唯一配置sipId={}, 租户={}",
                            requestSipId, deviceIdentification, vo.getSipId(), vo.getTenantId());
                    return Optional.of(vo);
                }
            }
        }

        return Optional.empty();
    }
}
