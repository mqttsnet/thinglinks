package com.mqttsnet.thinglinks.device.manager;

import com.mqttsnet.basic.base.manager.SuperManager;
import com.mqttsnet.thinglinks.device.entity.DeviceAclRule;

/**
 * 设备访问控制(ACL)规则 ── 数据访问层。
 * 缓存逻辑全部下沉到 LinkCacheDataHelper + DeviceAclRuleCacheService。
 */
public interface DeviceAclRuleManager extends SuperManager<DeviceAclRule> {
}
