package com.mqttsnet.thinglinks.device.manager.impl;

import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.thinglinks.device.entity.DeviceAclRule;
import com.mqttsnet.thinglinks.device.manager.DeviceAclRuleManager;
import com.mqttsnet.thinglinks.device.mapper.DeviceAclRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备访问控制(ACL)规则 ── 数据访问层实现。
 * 缓存逻辑全部下沉到 LinkCacheDataHelper + DeviceAclRuleCacheService。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DeviceAclRuleManagerImpl extends SuperManagerImpl<DeviceAclRuleMapper, DeviceAclRule>
        implements DeviceAclRuleManager {
}
