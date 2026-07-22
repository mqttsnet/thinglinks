package com.mqttsnet.thinglinks.device.service;

import java.util.List;

import com.mqttsnet.thinglinks.cache.vo.device.DeviceAclRuleCacheVO;

/**
 * 设备 ACL 规则只读查询 Service(leaf 业务层),仅持有 DeviceAclRuleManager、零下游 Service 依赖、类图为 DAG。
 * 专供 cache helper / 跨域 Service 反向查询,规避 DeviceAclRuleService ↔ LinkCacheDataHelper ↔
 * DeviceAclRuleCacheService 之间的构造期循环依赖。
 *
 * @author mqttsnet
 * @since 2026-05-28
 */
public interface DeviceAclRuleQueryService {

    /**
     * 拉指定设备生效的 ACL 规则(产品级 + 该设备级,enabled=true)。
     *
     * <p>截断契约:SQL 按"设备级在前 + priority 升序"取 TOP 200。规则数 ≥ 200 时丢弃低优先级产品级规则、
     * 决策语义会变,截断时输出 warn 告警,应及时收敛规则数。</p>
     *
     * @return 已排序的 List(最多 200);参数为空 / DB miss 返空 List
     */
    List<DeviceAclRuleCacheVO> listTopEnabledRulesByDevice(String productIdentification, String deviceIdentification);
}
