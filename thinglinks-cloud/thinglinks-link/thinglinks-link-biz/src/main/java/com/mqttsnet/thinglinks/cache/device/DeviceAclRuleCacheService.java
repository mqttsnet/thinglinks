package com.mqttsnet.thinglinks.cache.device;

import java.util.List;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceAclRuleCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.service.DeviceAclRuleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ACL 规则缓存 DB 回源服务,仅供 LinkCacheDataHelper#getDeviceAclRules 的 read-through 调用。
 * 持有 leaf {@link DeviceAclRuleQueryService}(零下游依赖),类图 DAG,无构造期循环。
 *
 * @author ShiHuan Sun
 */
@DS(DsConstant.BASE_TENANT)
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceAclRuleCacheService {

    private final DeviceAclRuleQueryService deviceAclRuleQueryService;

    /**
     * 拉指定设备生效的全部 ACL 规则(产品级 + 该设备级,enabled=true),按"设备级在前 + priority 升序"排序。
     * 纯 DB 查询不读写缓存,写缓存由 LinkCacheDataHelper 的 read-through 链统一负责。
     */
    public List<DeviceAclRuleCacheVO> loadEnabledAclRulesFromDb(String productIdentification, String deviceIdentification) {
        return deviceAclRuleQueryService.listTopEnabledRulesByDevice(productIdentification, deviceIdentification);
    }
}
