package com.mqttsnet.thinglinks.system.manager.application.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.mqttsnet.basic.base.manager.impl.SuperCacheManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.thinglinks.common.cache.tenant.application.ResourceApiCacheKeyBuilder;
import com.mqttsnet.thinglinks.model.vo.result.ResourceApiVO;
import com.mqttsnet.thinglinks.system.entity.application.DefResourceApi;
import com.mqttsnet.thinglinks.system.manager.application.DefResourceApiManager;
import com.mqttsnet.thinglinks.system.mapper.application.DefResourceApiMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 应用管理
 *
 * @author mqttsnet
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [mqttsnet] [初始创建]
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class DefResourceApiManagerImpl extends SuperCacheManagerImpl<DefResourceApiMapper, DefResourceApi> implements DefResourceApiManager {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new ResourceApiCacheKeyBuilder();
    }

    @Override
    public List<ResourceApiVO> findAllApi() {
        return baseMapper.findAllApi();
    }

    /**
     * 按资源 ID 列表批量删除 API 关联 + 失效对应缓存.
     *
     * <p><b>事务边界由调用方 Service 控制</b> ── 不在 Manager 层再开事务:
     * 当前所有调用方 {@code DefResourceServiceImpl.deleteByResourceId / saveOrUpdateResourceApi}
     * 已用 {@code @Transactional(rollbackFor = Exception.class)} 包住外层,
     * Spring 默认 PROPAGATION.REQUIRED 自动加入外层事务.若 Manager 再标 @Transactional 是冗余嵌套.</p>
     *
     * <p>切库走 Service 层 {@code @DS} 注解(本类无 @DS,继承上下文).</p>
     *
     * @param resourceIdList 资源 ID 列表
     */
    @Override
    public void removeByResourceId(List<Long> resourceIdList) {
        LbQueryWrap<DefResourceApi> wrap = Wraps.<DefResourceApi>lbQ().select(DefResourceApi::getId).in(DefResourceApi::getResourceId, resourceIdList);
        List<Long> apiIds = listObjs(wrap, Convert::toLong);
        remove(wrap);

        CacheKey[] keys = apiIds.stream().map(ResourceApiCacheKeyBuilder::builder).toArray(CacheKey[]::new);
        cacheOps.del(keys);
    }

    @Override
    public List<DefResourceApi> findByResourceId(Long resourceId) {
        if (resourceId == null) {
            return Collections.emptyList();
        }
        return list(Wraps.<DefResourceApi>lbQ().eq(DefResourceApi::getResourceId, resourceId));
    }

    @Override
    public List<DefResourceApi> findByResourceId(List<Long> resourceIds) {
        if (CollUtil.isEmpty(resourceIds)) {
            return Collections.emptyList();
        }
        return list(Wraps.<DefResourceApi>lbQ().in(DefResourceApi::getResourceId, resourceIds));
    }


}
