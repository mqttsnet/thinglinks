package com.mqttsnet.thinglinks.base.manager.user.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.manager.impl.SuperCacheManagerImpl;
import com.mqttsnet.basic.model.cache.CacheKeyBuilder;
import com.mqttsnet.basic.utils.CollHelper;
import com.mqttsnet.thinglinks.base.entity.user.BasePosition;
import com.mqttsnet.thinglinks.base.manager.user.BasePositionManager;
import com.mqttsnet.thinglinks.base.mapper.user.BasePositionMapper;
import com.mqttsnet.thinglinks.common.cache.base.user.PositionCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 岗位 Manager 实现。
 *
 * @author mqttsnet
 * @date 2021-10-18
 * @create [2021-10-18] [mqttsnet]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasePositionManagerImpl extends SuperCacheManagerImpl<BasePositionMapper, BasePosition> implements BasePositionManager {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new PositionCacheKeyBuilder();
    }

    /**
     * Echo 字典回显批量接口 ── 由 {@code basic-echo-starter} 框架的 {@code @EchoLoader} 反射调用,
     * Manager 层必须直接暴露(by design,框架契约,不能下沉 Service).
     *
     * <p>{@code @DS(BASE_TENANT)} 显式标 ── Echo 回显走租户库;
     * {@code @Transactional(readOnly = true)} 纯读优化,非业务事务.</p>
     */
    @Transactional(readOnly = true)
    @Override
    @DS(DsConstant.BASE_TENANT)
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        // ① null + 空集合兜底:Echo 框架可能传 null,父类 findByIds 对 null/空集合行为不确定
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        try {
            List<BasePosition> list = findByIds(ids, null).stream().filter(Objects::nonNull).toList();
            return CollHelper.uniqueIndex(list, BasePosition::getId, BasePosition::getName);
        } catch (Exception e) {
            // ② 异常降级:Echo 失败不能拖垮调用方接口,前端 echoMapText 会自动回退到原 ID
            log.warn("[Echo] BasePosition findByIds failed, ids={}, cause={}", ids, e.getMessage());
            return Collections.emptyMap();
        }
    }
}
