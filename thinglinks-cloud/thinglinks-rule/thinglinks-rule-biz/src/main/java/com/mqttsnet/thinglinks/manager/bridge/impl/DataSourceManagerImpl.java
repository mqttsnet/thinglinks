package com.mqttsnet.thinglinks.manager.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.manager.bridge.DataSourceManager;
import com.mqttsnet.thinglinks.mapper.bridge.DataSourceMapper;
import com.mqttsnet.thinglinks.vo.query.bridge.DataSourcePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 数据桥接-数据源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataSourceManagerImpl extends SuperManagerImpl<DataSourceMapper, DataSource> implements DataSourceManager {

    private final DataSourceMapper dataSourceMapper;

    @Override
    public List<DataSource> getDataSourceList(DataSourcePageQuery query) {
        QueryWrap<DataSource> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(query.getId() != null, DataSource::getId, query.getId())
                .eq(StrUtil.isNotBlank(query.getAppId()), DataSource::getAppId, query.getAppId())
                .like(StrUtil.isNotBlank(query.getDataSourceName()), DataSource::getDataSourceName, query.getDataSourceName())
                .eq(StrUtil.isNotBlank(query.getDataSourceCode()), DataSource::getDataSourceCode, query.getDataSourceCode())
                .eq(StrUtil.isNotBlank(query.getDirection()), DataSource::getDirection, query.getDirection())
                .eq(StrUtil.isNotBlank(query.getSourceType()), DataSource::getSourceType, query.getSourceType())
                .eq(query.getEnable() != null, DataSource::getEnable, query.getEnable())
                .eq(StrUtil.isNotBlank(query.getHealthStatus()), DataSource::getHealthStatus, query.getHealthStatus())
                .orderByDesc(DataSource::getCreatedTime);
        return dataSourceMapper.selectList(wrap);
    }

    @Override
    public DataSource getByCode(String dataSourceCode) {
        if (StrUtil.isBlank(dataSourceCode)) {
            return null;
        }
        QueryWrap<DataSource> wrap = new QueryWrap<>();
        wrap.lambda().eq(DataSource::getDataSourceCode, dataSourceCode);
        return dataSourceMapper.selectOne(wrap);
    }
}
