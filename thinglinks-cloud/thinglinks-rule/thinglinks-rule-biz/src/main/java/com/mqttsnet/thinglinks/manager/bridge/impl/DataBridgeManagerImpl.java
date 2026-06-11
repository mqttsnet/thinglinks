package com.mqttsnet.thinglinks.manager.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.manager.bridge.DataBridgeManager;
import com.mqttsnet.thinglinks.mapper.bridge.DataBridgeMapper;
import com.mqttsnet.thinglinks.vo.query.bridge.DataBridgePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 通用业务实现类
 * 数据桥接-规则
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DataBridgeManagerImpl extends SuperManagerImpl<DataBridgeMapper, DataBridge> implements DataBridgeManager {

    private final DataBridgeMapper dataBridgeMapper;

    @Override
    public List<DataBridge> getDataBridgeList(DataBridgePageQuery query) {
        QueryWrap<DataBridge> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(query.getId() != null, DataBridge::getId, query.getId())
                .eq(StrUtil.isNotBlank(query.getAppId()), DataBridge::getAppId, query.getAppId())
                .like(StrUtil.isNotBlank(query.getRuleName()), DataBridge::getRuleName, query.getRuleName())
                .eq(StrUtil.isNotBlank(query.getRuleCode()), DataBridge::getRuleCode, query.getRuleCode())
                .eq(StrUtil.isNotBlank(query.getDirection()), DataBridge::getDirection, query.getDirection())
                .eq(query.getDataSourceId() != null, DataBridge::getDataSourceId, query.getDataSourceId())
                .eq(query.getEnable() != null, DataBridge::getEnable, query.getEnable())
                .orderByAsc(DataBridge::getPriority)
                .orderByDesc(DataBridge::getCreatedTime);
        return dataBridgeMapper.selectList(wrap);
    }

    @Override
    public DataBridge getByCode(String ruleCode) {
        if (StrUtil.isBlank(ruleCode)) {
            return null;
        }
        QueryWrap<DataBridge> wrap = new QueryWrap<>();
        wrap.lambda().eq(DataBridge::getRuleCode, ruleCode);
        return dataBridgeMapper.selectOne(wrap);
    }

    @Override
    public List<DataBridge> getEnabledRules(String appId, String direction) {
        QueryWrap<DataBridge> wrap = new QueryWrap<>();
        wrap.lambda()
                .eq(StrUtil.isNotBlank(appId), DataBridge::getAppId, appId)
                .eq(StrUtil.isNotBlank(direction), DataBridge::getDirection, direction)
                .eq(DataBridge::getEnable, Boolean.TRUE)
                .orderByAsc(DataBridge::getPriority);
        return dataBridgeMapper.selectList(wrap);
    }
}
