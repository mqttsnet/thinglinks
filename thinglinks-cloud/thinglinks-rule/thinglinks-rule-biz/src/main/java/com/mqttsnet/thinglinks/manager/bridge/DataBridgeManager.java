package com.mqttsnet.thinglinks.manager.bridge;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.vo.query.bridge.DataBridgePageQuery;

import java.util.List;

/**
 * <p>
 * 通用业务接口
 * 数据桥接-规则
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface DataBridgeManager extends SuperManager<DataBridge> {

    List<DataBridge> getDataBridgeList(DataBridgePageQuery query);

    DataBridge getByCode(String ruleCode);

    /**
     * 取启用中的桥接规则（matcher 缓存填充用）
     *
     * @param appId        应用 ID（可空 → 不限）
     * @param direction    方向（10/20）
     */
    List<DataBridge> getEnabledRules(String appId, String direction);
}
