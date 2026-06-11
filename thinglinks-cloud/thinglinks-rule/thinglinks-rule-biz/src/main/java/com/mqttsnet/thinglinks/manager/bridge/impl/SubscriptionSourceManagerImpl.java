package com.mqttsnet.thinglinks.manager.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.manager.bridge.SubscriptionSourceManager;
import com.mqttsnet.thinglinks.mapper.bridge.SubscriptionSourceMapper;
import com.mqttsnet.thinglinks.vo.query.bridge.SubscriptionSourcePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 数据桥接-订阅源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionSourceManagerImpl extends SuperManagerImpl<SubscriptionSourceMapper, SubscriptionSource> implements SubscriptionSourceManager {

    private final SubscriptionSourceMapper subscriptionSourceMapper;

    @Override
    public List<SubscriptionSource> getSubscriptionSourceList(SubscriptionSourcePageQuery query) {
        QueryWrap<SubscriptionSource> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(query.getId() != null, SubscriptionSource::getId, query.getId())
                .eq(StrUtil.isNotBlank(query.getAppId()), SubscriptionSource::getAppId, query.getAppId())
                .like(StrUtil.isNotBlank(query.getSourceName()), SubscriptionSource::getSourceName, query.getSourceName())
                .eq(StrUtil.isNotBlank(query.getSourceCode()), SubscriptionSource::getSourceCode, query.getSourceCode())
                .eq(query.getDataSourceId() != null, SubscriptionSource::getDataSourceId, query.getDataSourceId())
                .eq(StrUtil.isNotBlank(query.getTargetHandler()), SubscriptionSource::getTargetHandler, query.getTargetHandler())
                .eq(query.getEnable() != null, SubscriptionSource::getEnable, query.getEnable())
                .orderByDesc(SubscriptionSource::getCreatedTime);
        return subscriptionSourceMapper.selectList(wrap);
    }

    @Override
    public SubscriptionSource getByCode(String sourceCode) {
        if (StrUtil.isBlank(sourceCode)) {
            return null;
        }
        QueryWrap<SubscriptionSource> wrap = new QueryWrap<>();
        wrap.lambda().eq(SubscriptionSource::getSourceCode, sourceCode);
        return subscriptionSourceMapper.selectOne(wrap);
    }

    @Override
    public List<SubscriptionSource> getEnabledSources() {
        QueryWrap<SubscriptionSource> wrap = new QueryWrap<>();
        wrap.lambda().eq(SubscriptionSource::getEnable, Boolean.TRUE);
        return subscriptionSourceMapper.selectList(wrap);
    }
}
